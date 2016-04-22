(ns eve-market-analyser-clj.util-test
  (:require [eve-market-analyser-clj.util :refer :all]
            [clojure.test :refer :all]))

(deftest whole?-test
  (is (= (whole? 1.0) true))
  (is (= (whole? 1.1) false)))

(deftest round-sig-fig-test
  (is (== (round-sig-fig 123456 2) 120000))
  (is (== (round-sig-fig 123456 3) 123000))
  (is (== (round-sig-fig 123556 3) 124000))
  (is (== (round-sig-fig 0 1) 0)))

(deftest reduce-multi-test
  (is (= (reduce-multi [max min] [4 3 6 7 0 1 8 2 5 9])
         [9 0]))
  (is (= (reduce-multi [max min] [1])
         [1 1])))

