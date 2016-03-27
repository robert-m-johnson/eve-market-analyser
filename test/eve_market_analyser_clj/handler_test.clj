(ns eve-market-analyser-clj.handler-test
  (:require [eve-market-analyser-clj.handler :refer :all]
            [clojure.test :refer :all]
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
