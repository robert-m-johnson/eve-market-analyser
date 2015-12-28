(ns eve-market-analyser-clj.core
  (:gen-class)
  (:require [eve-market-analyser-clj.world :as world]
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


(defn feed->region-item [feed-item]
  (let [col-names (:columns feed-item)
        price-index (.indexOf  col-names "price")
        bid-index (.indexOf  col-names "bid")
        quantity-index (.indexOf col-names "volEntered")
        order-vec->order (fn [order-vec]
                           {:price (nth order-vec price-index)
                            :isBid (nth order-vec bid-index)
                            :quantity (nth order-vec quantity-index)})
        rowsets (->> (:rowsets feed-item) (filter :regionID))]
    (map (fn [rowset]
           (let [orders (->> (:rows rowset) (map order-vec->order))
                 buyOrders (->>  (filter #(:isBid %) orders) (map #(dissoc % :isBid)) (sort-by :price >))
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
