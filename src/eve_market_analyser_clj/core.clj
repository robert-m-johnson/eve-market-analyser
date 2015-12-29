(ns eve-market-analyser-clj.core
  (:gen-class)
  (:require [clojure.algo.generic.functor :refer [fmap]]
            [eve-market-analyser-clj.world :as world]
            [zeromq.zmq :as zmq]
            [cheshire.core :as chesh])
  (:import java.util.zip.Inflater
           java.nio.charset.Charset))

(defn decompress [byte-arr]
  (let [inflater (Inflater.)
        out-byte-arr (byte-array (-> (count byte-arr) (* 16)))]
    (.setInput inflater byte-arr)
    (let [length (.inflate inflater out-byte-arr)]
      (byte-array length out-byte-arr))))

(defn to-string [^bytes x]
  (String. x (Charset/forName "UTF-8")))

(defn- apply-or-default [default f s]
  (if (empty? s)
    default
    (apply f s)))

(defn- vector-extractor*
  "Given a vector of column names, and a map of keys and corresponding column
  names, returns a function that will return a map of the keys and the corresponding
  values stored in the vector"
  [col-names name-map]
  (let [key-index-map (fmap #(.indexOf col-names %) name-map)]
    (fn [v] (fmap #(nth v %) key-index-map))))

(defn feed->region-item [feed-item]
  (let [order-vec->order
        (vector-extractor* (:columns feed-item) {:price "price" :quantity "volRemaining" :isBid "bid"})
        rowsets (->> (:rowsets feed-item)
                     (filter :regionID) ;; Filter items with no region ID
                     (filter :typeID) ;; Filter items with no type ID
                     )]
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

(defn -main []
  (let [context (zmq/context 1)]
    (println "Connecting to EMDR server…")
    (with-open [subscriber (doto (zmq/socket context :sub)
                             (zmq/connect "tcp://relay-eu-germany-1.eve-emdr.com:8050")
                             (zmq/set-receive-timeout 10000)
                             (zmq/subscribe ""))]
      (dotimes [i 1]
        (println "Receiving item...")
        (let [bytes (zmq/receive subscriber)]
          (println "Received :" (-> bytes decompress to-string)))))))
