(ns eve-market-analyser-clj.core-test
  (:require [clojure.test :refer :all]
            [eve-market-analyser-clj.core :refer :all]))


(def feed-item-with-one-region
  {:resultType "orders", :version "0.1", :uploadKeys [{:name "emk", :key "abc"} {:name "ec", :key "def"}],
   :generator {:name "Yapeal", :version "11.335.1737"}, :currentTime "2011-10-22T15:46:00+00:00",
   :columns ["price" "volRemaining" "range" "orderID" "volEntered" "minVolume" "bid" "issueDate" "duration" "stationID" "solarSystemID"],
   :rowsets [
             {:generatedAt "2011-10-22T15:43:00+00:00", :regionID 10000065, :typeID 11134,
              :rows [
                     [11500 48 32767 2363413004 50 1 false "2011-12-02T22:44:01+00:00" 90 60006967 30005039]
                     [8999 1 32767 2363806077 1 1 false "2011-12-03T08:10:59+00:00" 90 60008692 30005038]
                     [11499.99 10 32767 2363915657 10 1 false "2011-12-03T10:53:26+00:00" 90 60006970 nil]
                     [5000 48 32767 2363413004 50 1 true "2011-12-02T22:44:01+00:00" 90 60006967 30005039]
                     [6000 48 32767 2363413004 50 1 true "2011-12-02T22:44:01+00:00" 90 60006967 30005039]
                     [4000 48 32767 2363413004 50 1 true "2011-12-02T22:44:01+00:00" 90 60006967 30005039]]}]}
)

(deftest single-region
  (testing "Only one region"
    (is (= '({:generatedTime "2011-10-22T15:43:00+00:00"
              :typeID 11134
              :itemName "Amarr Shuttle"
              :regionID 10000065
              :regionName "Kor-Azor"
              :sellingPrice 8999
              :buyingPrice 6000
              :sellOrders [{:price 8999 :quantity 1} {:price 11499.99 :quantity 10} {:price 11500 :quantity 50}]
              :buyOrders [{:price 6000 :quantity 50} {:price 5000 :quantity 50} {:price 4000 :quantity 50}]})
           (feed->region-item feed-item-with-one-region)))))

(def feed-item-with-2-regions
  {:resultType "orders", :version "0.1", :uploadKeys [{:name "emk", :key "abc"} {:name "ec", :key "def"}],
   :generator {:name "Yapeal", :version "11.335.1737"}, :currentTime "2011-10-22T15:46:00+00:00",
   :columns ["price" "volRemaining" "range" "orderID" "volEntered" "minVolume" "bid" "issueDate" "duration" "stationID" "solarSystemID"],
   :rowsets [
             {:generatedAt "2011-10-22T15:43:00+00:00", :regionID 10000065, :typeID 11134,
              :rows [
                     [11500 48 32767 2363413004 50 1 false "2011-12-02T22:44:01+00:00" 90 60006967 30005039]
                     [4000 48 32767 2363413004 50 1 true "2011-12-02T22:44:01+00:00" 90 60006967 30005039]]}
             {:generatedAt "2011-10-22T15:42:00+00:00", :regionID 10000033, :typeID 11135,
              :rows [[8999 1 32767 2363806077 1 1 false "2011-12-03T08:10:59+00:00" 90 60008692 30005038]
                     [11500 48 32767 2363413004 50 1 true "2011-12-02T22:44:01+00:00" 90 60006967 30005039]]}]}
)

(deftest two-regions
  (testing "Two regions"
    (is (= '({:generatedTime "2011-10-22T15:43:00+00:00"
              :typeID 11134
              :itemName "Amarr Shuttle"
              :regionID 10000065
              :regionName "Kor-Azor"
              :sellingPrice 11500
              :buyingPrice 4000
              :sellOrders [{:price 11500 :quantity 50}]
              :buyOrders [{:price 4000 :quantity 50}]}
             {:generatedTime "2011-10-22T15:42:00+00:00"
              :typeID 11135
              :itemName "Amarr Shuttle Blueprint"
              :regionID 10000033
              :regionName "The Citadel"
              :sellingPrice 8999
              :buyingPrice 11500
              :sellOrders [{:price 8999 :quantity 1}]
              :buyOrders [{:price 11500 :quantity 50}]})
           (feed->region-item feed-item-with-2-regions)))))

(def feed-item-with-unknown-region
  {:resultType "orders", :version "0.1", :uploadKeys [{:name "emk", :key "abc"} {:name "ec", :key "def"}],
   :generator {:name "Yapeal", :version "11.335.1737"}, :currentTime "2011-10-22T15:46:00+00:00",
   :columns ["price" "volRemaining" "range" "orderID" "volEntered" "minVolume" "bid" "issueDate" "duration" "stationID" "solarSystemID"],
   :rowsets [
             {:generatedAt "2011-10-22T15:43:00+00:00", :regionID 10000065, :typeID 11134,
              :rows [
                     [11500 48 32767 2363413004 50 1 false "2011-12-02T22:44:01+00:00" 90 60006967 30005039]
                     [8999 1 32767 2363806077 1 1 false "2011-12-03T08:10:59+00:00" 90 60008692 30005038]
                     [11499.99 10 32767 2363915657 10 1 false "2011-12-03T10:53:26+00:00" 90 60006970 nil]
                     [5000 48 32767 2363413004 50 1 true "2011-12-02T22:44:01+00:00" 90 60006967 30005039]
                     [6000 48 32767 2363413004 50 1 true "2011-12-02T22:44:01+00:00" 90 60006967 30005039]
                     [4000 48 32767 2363413004 50 1 true "2011-12-02T22:44:01+00:00" 90 60006967 30005039]]}
             {:generatedAt "2011-10-22T15:42:00+00:00", :regionID nil, :typeID 11135,
              :rows [[8999 1 32767 2363806077 1 1 false "2011-12-03T08:10:59+00:00" 90 60008692 30005038]
                     [11499.99 10 32767 2363915657 10 1 false "2011-12-03T10:53:26+00:00" 90 60006970 nil]
                     [11500 48 32767 2363413004 50 1 false "2011-12-02T22:44:01+00:00" 90 60006967 30005039]]}]}
)

(deftest unknown-region-excluded
  (testing "Unknown region excluded"
    (is (= '({:generatedTime "2011-10-22T15:43:00+00:00"
              :typeID 11134
              :itemName "Amarr Shuttle"
              :regionID 10000065
              :regionName "Kor-Azor"
              :sellingPrice 8999
              :buyingPrice 6000
              :sellOrders [{:price 8999 :quantity 1} {:price 11499.99 :quantity 10} {:price 11500 :quantity 50}]
              :buyOrders [{:price 6000 :quantity 50} {:price 5000 :quantity 50} {:price 4000 :quantity 50}]})
           (feed->region-item feed-item-with-unknown-region)))))
