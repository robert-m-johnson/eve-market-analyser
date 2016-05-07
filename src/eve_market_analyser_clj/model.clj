(ns eve-market-analyser-clj.model
  (:require [clj-time.core :as t]))

(defn highest-selling-price [items]
  (apply max (map :sellingPrice items)))

(defn lowest-selling-price [items]
  (apply min (map :sellingPrice items)))

(defn highest-buying-price [items]
  (apply max (map :buyingPrice items)))

(defn lowest-buying-price [items]
  (apply min (map :buyingPrice items)))

(defn mark-best-prices [items]
  (if (or (not items) (empty? items))
    items
    (let [highest-sp (highest-selling-price items)
          lowest-sp (lowest-selling-price items)
          highest-bp (highest-buying-price items)
          lowest-bp (lowest-buying-price items)
          mark-val (fn [item k v mark]
                     (if (= (k item) v)
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
