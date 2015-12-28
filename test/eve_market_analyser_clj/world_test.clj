(ns eve-market-analyser-clj.world-test
  (:require [clojure.test :refer :all]
            [eve-market-analyser-clj.world :refer :all]))

(deftest types-test
  (testing "Types"
    (is (= "Mexallon" (types 36)))
    (is (= "Tritanium" (types 34)))
    (is (= "Condor" (types 583)))))
