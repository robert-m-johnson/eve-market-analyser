(ns eve-market-analyser-clj.model
  (:require [eve-market-analyser-clj.util :refer [apply-or-default]]
   [clj-time.core :as t]))

(defn- extract [items f k]
  (apply-or-default f nil (filter some? (map k items))))

(defn highest-selling-price [items]
  (extract items max :sellingPrice))

(defn lowest-selling-price [items]
  (extract items min :sellingPrice))

(defn highest-buying-price [items]
  (extract items max :buyingPrice))

(defn lowest-buying-price [items]
  (extract items min :buyingPrice))

(defn mark-best-prices [items]
  (if (or (not items) (empty? items))
    items
    (let [highest-sp (highest-selling-price items)
          lowest-sp (lowest-selling-price items)
          highest-bp (highest-buying-price items)
          lowest-bp (lowest-buying-price items)
          mark-val (fn [item k v mark]
                     (if (and v (= (k item) v))
                       (assoc item mark true)
                       item))]
      (map
       (comp
        #(mark-val % :sellingPrice highest-sp :highestSellingPrice)
        #(mark-val % :sellingPrice lowest-sp :lowestSellingPrice)
        #(mark-val % :buyingPrice highest-bp :highestBuyingPrice)
        #(mark-val % :buyingPrice lowest-bp :lowestBuyingPrice))
       items))))

(defn hub-prices [item-name hub-items]
  (let [hub-items (mark-best-prices hub-items)
        earliest-generated (if (empty? hub-items) nil
                               (t/earliest (map :generatedTime hub-items)))]
    {:itemName item-name
     :hubItems hub-items
     :earliestGenerated earliest-generated}))
