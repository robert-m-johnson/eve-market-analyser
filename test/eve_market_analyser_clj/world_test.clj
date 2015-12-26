(ns eve-market-analyser-clj.world-test
  (:require [clojure.test :refer :all]
            [eve-market-analyser-clj.world :refer :all]))

(deftest a-test
  (testing "Types"
    (is (= "Mexallon" (types 36)))))
