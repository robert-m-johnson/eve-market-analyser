(ns eve-market-analyser.model-test
  (:require [clojure.test :refer :all]
            [eve-market-analyser.model :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

(def items
  [{:generatedTime (tc/from-string "2016-03-26T17:59:09.000Z"),
    :buyingPrice 7.01,
    :sellingPrice 14.17,
    :regionName "The Forge",
    :itemName "Iron Charge M"}
   {:generatedTime (tc/from-string "2016-03-26T17:23:55.000Z"),
    :buyingPrice 11.38,
    :sellingPrice 20.0,
    :regionName "Domain",
    :itemName "Iron Charge M"}
   {:generatedTime (tc/from-string "2016-03-26T17:00:01.000Z"),
    :buyingPrice 14.47,
    :sellingPrice 18.93,
    :regionName "Heimatar",
    :itemName "Iron Charge M"}
   {:generatedTime (tc/from-string "2016-03-27T18:07:27.000+01:00"),
    :buyingPrice 10.49,
    :sellingPrice 16.58,
    :regionName "Sinq Laison",
    :itemName "Iron Charge M"}
   ])

(deftest highest-selling-price-test
  (is (= (highest-selling-price items) 20.0)))

(deftest lowest-selling-price-test
  (is (= (lowest-selling-price items) 14.17)))

(deftest highest-buying-price-test
  (is (= (highest-buying-price items) 14.47)))

(deftest lowest-buying-price-test
  (is (= (lowest-buying-price items) 7.01)))

(def items-with-prices-marked
  (-> items
      (assoc-in [1 :highestSellingPrice] true)
      (assoc-in [0 :lowestSellingPrice] true)
      (assoc-in [2 :highestBuyingPrice] true)
      (assoc-in [0 :lowestBuyingPrice] true)))

(deftest mark-best-prices-test
  (is (= (mark-best-prices items) items-with-prices-marked)))

(def gen-time-default (t/date-time 2016 5 6 12 0 0))

(deftest hub-prices-test
  (is
   (= (hub-prices "Crane" []) {:itemName "Crane" :earliestGenerated nil :hubItems []}))
  (is (= (hub-prices
          "Iron Charge M"
          [{:regionName "The Forge"
            :generatedTime gen-time-default
            :sellingPrice nil
            :buyingPrice nil}])

         {:itemName "Iron Charge M"
          :earliestGenerated gen-time-default
          :hubItems
          [{:regionName "The Forge"
            :generatedTime gen-time-default
            :sellingPrice nil
            :buyingPrice nil}]}))
  (is (= (hub-prices
          "Iron Charge M"
          [{:regionName "The Forge"
            :generatedTime gen-time-default
            :sellingPrice 100
            :buyingPrice 50}
           {:regionName "Sinq Laison"
            :generatedTime gen-time-default
            :sellingPrice nil
            :buyingPrice nil}])

         {:itemName "Iron Charge M"
          :earliestGenerated gen-time-default
          :hubItems
          [{:regionName "The Forge"
            :generatedTime gen-time-default
            :sellingPrice 100
            :buyingPrice 50
            :highestSellingPrice true
            :lowestSellingPrice true
            :highestBuyingPrice true
            :lowestBuyingPrice true}
           {:regionName "Sinq Laison"
            :generatedTime gen-time-default
            :sellingPrice nil
            :buyingPrice nil}]}))
  (is
   (= (hub-prices
       "Iron Charge M"
       [{:regionName "The Forge"
         :generatedTime (t/date-time 2016 5 6 12 30 0)
         :sellingPrice 100
         :buyingPrice 40}
        {:regionName "Sinq Laison"
         :generatedTime (t/date-time 2016 5 6 13 0 0)
         :sellingPrice 110
         :buyingPrice 30}])

      {:itemName "Iron Charge M"
       :earliestGenerated (t/date-time 2016 5 6 12 30 0)
       :hubItems [{:regionName "The Forge"
                    :generatedTime (t/date-time 2016 5 6 12 30 0)
                    :sellingPrice 100
                    :buyingPrice 40
                    :lowestSellingPrice true
                    :highestBuyingPrice true},
                   {:regionName "Sinq Laison"
                    :generatedTime (t/date-time 2016 5 6 13 0 0)
                    :sellingPrice 110
                    :buyingPrice 30
                    :highestSellingPrice true
                    :lowestBuyingPrice true}]})))

