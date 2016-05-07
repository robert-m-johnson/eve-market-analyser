(ns eve-market-analyser-clj.model-test
  (:require [clojure.test :refer :all]
            [eve-market-analyser-clj.model :refer :all]
            [clj-time.core :as t]))

(deftest hub-prices-test
  (is
   (= (hub-prices "Crane" []) {:itemName "Crane" :earliestGenerated nil :hubItems []}))
  (is
   (= (hub-prices
       "Iron Charge M"
       [{:regionName "The Forge"
         :generatedTime (t/date-time 2016 5 6 12 30 0)
         :sellingPrice 100
         :buyingPrice 40},
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

