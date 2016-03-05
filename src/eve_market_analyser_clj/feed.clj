(ns eve-market-analyser-clj.feed
  (:gen-class)
  (:require [clojure.algo.generic.functor :refer [fmap]]
            [eve-market-analyser-clj.world :as world]
            [eve-market-analyser-clj.db :as db]
            [zeromq.zmq :as zmq]
            [cheshire.core :as chesh]
            [clojure.tools.logging :as log])
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

(defn- apply-or-default [default f s]
  (if (empty? s)
    default
    (apply f s)))

(defn- vector-extractor*
  "Given a vector of column names, and a map of keys and corresponding column
  names, returns a function that, given a vector will return a map of the keys
  and the corresponding values taken from the vector"
  [col-names name-map]
  (let [key-index-map (fmap #(.indexOf col-names %) name-map)]
    (fn [v] (fmap #(nth v %) key-index-map))))

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
                 buyingPrice (->> (map :price buyOrders) (apply-or-default nil max))
                 sellingPrice (->> (map :price sellOrders) (apply-or-default nil min))]
             {:generatedTime (:generatedAt rowset)
              :typeID (:typeID rowset)
              :itemName (world/types (:typeID rowset))
              :regionID (:regionID rowset)
              :regionName (world/regions (:regionID rowset))
              :sellingPrice sellingPrice
              :buyingPrice buyingPrice
              :sellOrders sellOrders
              :buyOrders buyOrders}))
         rowsets)))

(defn listen []
  (let [context (zmq/context 1)]
    (while true ; Retry connection if we timed out
      (log/info "Connecting to EMDR server…")
      (with-open [subscriber (doto (zmq/socket context :sub)
                               (zmq/connect "tcp://relay-eu-germany-1.eve-emdr.com:8050")
                               (zmq/set-receive-timeout 30000)
                               (zmq/subscribe ""))]
        (loop []
          (log/debug "Receiving item...")
          (let [bytes (zmq/receive subscriber)]
            (if bytes
              (do
                (try
                  (let [feed-item (some-> bytes decompress to-string (chesh/parse-string true))]
                    (if (= "orders" (:resultType feed-item))
                      (let [region-items (feed->region-item feed-item)]
                        (log/debug "Valid item received")
                        (db/insert-items region-items))))
                  (catch Exception ex
                    (log/error ex)))
                ;; Only continue loop if we received a message; else retry connection
                (recur))
              (log/info "Socket timed out"))))))))
