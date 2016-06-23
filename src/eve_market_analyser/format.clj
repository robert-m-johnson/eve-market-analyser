(ns eve-market-analyser.format
  (:require [eve-market-analyser.util :as util])
  (:import java.math.MathContext
           com.github.kevinsawicki.timeago.TimeAgo))

(defn- int-if-whole [n]
  (if (util/whole? n)
    (int n)
    n))

(defn- abbreviate-num [n magnitude letter]
  (str
   (int-if-whole (/ n (double magnitude)))
   letter))

(defn price [n]
  (if n
    (let [rounded (-> (bigdec n) (.round (MathContext. 3)))
          normed (int-if-whole rounded)]
      (cond
        (< normed 1000)
        (str normed)

        (< normed 1000000)
        (abbreviate-num normed 1000 "k")

        (< normed 1000000000)
        (abbreviate-num normed 1000000 "m")

        :else
        (abbreviate-num normed 1000000000 "b")))

    ;; n is nil
    ""))

(defn time-ago [date]
  (.timeAgo (TimeAgo.) date))
