(ns eve-market-analyser.feed
  (:gen-class)
  (:require [clojure.core.async :as async :refer
             [alt!! chan thread sliding-buffer <!! >!!]]
            [clojure.algo.generic.functor :refer [fmap]]
            [eve-market-analyser.world :as world]
            [eve-market-analyser.db :as db]
            [eve-market-analyser.util :refer :all]
            [zeromq.zmq :as zmq]
            [cheshire.core :as chesh]
            [clojure.tools.logging :as log]
            [clj-time.format])
  (:import java.util.zip.Inflater
           java.nio.charset.Charset))

(defn- decompress [byte-arr]
  (let [inflater (Inflater.)
        out-byte-arr (byte-array (-> (count byte-arr) (* 16)))]
    (.setInput inflater byte-arr)
    (let [length (.inflate inflater out-byte-arr)]
      (byte-array length out-byte-arr))))

(defn- to-string [^bytes x]
  (String. x (Charset/forName "UTF-8")))

(defn- vector-extractor*
  "Given a vector of column names, and a map of keys and corresponding column
  names, returns a function that, given a vector will return a map of the keys
  and the corresponding values taken from the vector"
  [col-names name-map]
  (let [key-index-map
        ;; Find the index of each col name in the vector;
        ;; gives e.g. {:price 0, :quantity 1, :isBid 6}
        (fmap #(.indexOf col-names %) name-map)]
    (fn [v]
      ;; Replace the each index in key-index-map with the
      ;; corresponding value in the vector v
      (fmap #(nth v %) key-index-map))))

(defn feed->region-item [feed-item]
  (let [order-vec->order
        (vector-extractor* (:columns feed-item) {:price "price" :quantity "volRemaining" :isBid "bid"})
        rowsets (->> (:rowsets feed-item)
                     ;; Filter out items with no region ID
                     (filter :regionID)
                     ;; Filter out items with no type ID
                     (filter :typeID)
                     ;; Filter out null-sec regions
                     (filter #(world/empire-region? (:regionID %))))]
    (map (fn [rowset]
           (let [orders (->> (:rows rowset) (map order-vec->order))
                 buyOrders (->>  (filter :isBid orders) (map #(dissoc % :isBid)) (sort-by :price >))
                 sellOrders (->> (filter #(not (:isBid %)) orders) (map #(dissoc % :isBid)) (sort-by :price))
                 buyingPrice (->> (map :price buyOrders) (apply-or-default max nil))
                 sellingPrice (->> (map :price sellOrders) (apply-or-default min nil))]
             {:generatedTime (clj-time.format/parse (:generatedAt rowset))
              :typeId (:typeID rowset)
              :itemName (world/types (:typeID rowset))
              :regionId (:regionID rowset)
              :regionName (world/regions (:regionID rowset))
              :sellingPrice sellingPrice
              :buyingPrice buyingPrice
              :sellOrders sellOrders
              :buyOrders buyOrders}))
         rowsets)))

(defn bytes->region-items [bytes]
  (log/debug "Converting bytes to region items...")
  (let [region-items
        (try
          (let [feed-item (some-> bytes decompress to-string (chesh/parse-string true))]
            (let [{resultType :resultType} feed-item]
              (if (= "orders" resultType)
                (let [region-items (feed->region-item feed-item)]
                  region-items)
                (log/debugf "Ignoring feed item of type '%s'" resultType))))
          (catch Exception ex
            (log/error ex)))]
    (log/debug "Converted bytes to region items")
    region-items))

;; Full list of servers:
;; "tcp://relay-eu-germany-1.eve-emdr.com:8050"
;; "tcp://relay-eu-germany-2.eve-emdr.com:8050"
;; "tcp://relay-eu-germany-3.eve-emdr.com:8050"
;; "tcp://relay-eu-germany-4.eve-emdr.com:8050"
;; "tcp://relay-eu-denmark-1.eve-emdr.com:8050"
;; "tcp://relay-us-west-1.eve-emdr.com:8050"
;; "tcp://relay-us-central-1.eve-emdr.com:8050"

(def servers (atom (cycle
                    ["tcp://relay-eu-germany-1.eve-emdr.com:8050"
                     "tcp://relay-us-west-1.eve-emdr.com:8050"
                     "tcp://relay-us-central-1.eve-emdr.com:8050"])))

(defn next-server! []
  (let [svs @servers]
    (swap! servers rest)
    (first svs)))

(defn- open?!! [stop-chan]
  (alt!! stop-chan false :default :keep-going))

(defn wrap-log-err [f]
  (fn [stop-chan]
    (try
      (f stop-chan)
      (catch Exception e
        (log/error e "Enhandled exception in thread worker")))))

(defn create-thread-looper [f]
  (let [stop-chan (chan)
        worker-fn (wrap-log-err f)]
    (thread
      (while (open?!! stop-chan)
        (worker-fn stop-chan)))
    stop-chan))

(defonce bytes-chan (chan (sliding-buffer 500)))
(defonce region-items-chan (chan 50))

(defn listen* []
  (let [context (zmq/context 1)]
    (fn [stop-chan]
      (let [server (next-server!)]
        (log/infof "Connecting to EMDR server %s..." server)
        (with-open [subscriber (doto (zmq/socket context :sub)
                                 (zmq/connect server)
                                 (zmq/set-receive-timeout 300000)
                                 (zmq/subscribe ""))]
          (loop []
              (log/debug "Receiving item...")
              (let [bytes (zmq/receive subscriber)]
                (if (and bytes (open?!! stop-chan))
                  (do
                    (log/debug "Item received")
                    (>!! bytes-chan bytes)
                    ;; Only continue loop if we received a message; else retry connection
                    (recur))
                  (log/info "Socket timed out")))))))))

(defn listen []
  (create-thread-looper (listen*)))

(defn convert []
  (create-thread-looper
   (fn [_]
     (let [bytes (<!! bytes-chan)
           region-items (bytes->region-items bytes)]
       (if region-items
         (>!! region-items-chan region-items))))))

(defn consume []
  (create-thread-looper
   (fn [_]
     (let [region-items (<!! region-items-chan)]
       (if region-items
         (log/debug "Pretending to insert items")
         ;; TODO: insert items again
         ;;(db/insert-items region-items)
         )))))

