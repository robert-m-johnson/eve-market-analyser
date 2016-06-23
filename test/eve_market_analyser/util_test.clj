(ns eve-market-analyser.util-test
  (:require [eve-market-analyser.util :refer :all]
            [clojure.test :refer :all]))

(deftest whole?-test
  (is (= (whole? 1.0) true))
  (is (= (whole? 1.1) false)))

(deftest reduce-multi-test
  (is (= (reduce-multi [max min] [4 3 6 7 0 1 8 2 5 9])
         [9 0]))
  (is (= (reduce-multi [max min] [1])
         [1 1])))

