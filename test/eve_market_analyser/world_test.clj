(ns eve-market-analyser.world-test
  (:require [clojure.test :refer :all]
            [eve-market-analyser.world :refer :all]))

(deftest types-test
  (testing "Types"
    (is (= "Mexallon" (types 36)))
    (is (= "Tritanium" (types 34)))
    (is (= "Condor" (types 583)))))

(deftest regions-test
  (testing "Regions"
    (is (= "Devoid" (regions 10000036)))))

(deftest empire-region-test
  (testing "Empire region checker"
    (is (empire-region? 10000033))
    (is (empire-region? "The Citadel"))
    (is (not (empire-region? 10000022)))
    (is (not (empire-region? "Stain")))))
