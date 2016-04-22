(ns eve-market-analyser-clj.format
  (:require [eve-market-analyser-clj.util :as util]))

(defn- int-if-whole [n]
  (if (util/whole? n)
    (int n)
    n))

(defn abbreviate-num [n magnitude letter]
  (str
   (int-if-whole (/ n (double magnitude)))
   letter))

(defn price [n]
  (let [rounded (util/round-sig-fig n 3)
        normed (int-if-whole rounded)]
    (cond
      (< normed 1000)
      (str normed)

      (< normed 1000000)
      (abbreviate-num normed 1000 "k")

      (< normed 1000000000)
      (abbreviate-num normed 1000000 "m")

      :else
      (abbreviate-num normed 1000000000 "b"))))
