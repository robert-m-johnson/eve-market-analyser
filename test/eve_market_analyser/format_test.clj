(ns eve-market-analyser.format-test
  (:require [clojure.test :refer :all]
            [eve-market-analyser.format :refer :all]))

(deftest price-test
  (is (= (price nil) ""))
  (is (= (price 0) "0"))
  (is (= (price 0.5) "0.5"))
  (is (= (price 1) "1"))
  (is (= (price 1.234) "1.23"))
  (is (= (price 12.34) "12.3"))
  (is (= (price 6.79) "6.79"))
  (is (= (price 123.456) "123"))
  (is (= (price 123.5) "124"))
  (is (= (price 1000) "1k"))
  (is (= (price 1234) "1.23k"))
  (is (= (price 12345) "12.3k"))
  (is (= (price 123456) "123k"))
  (is (= (price 1234567) "1.23m"))
  (is (= (price 123456789) "123m"))
  (is (= (price 1234567890) "1.23b")))
