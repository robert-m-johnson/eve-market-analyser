(ns eve-market-analyser-clj.world
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def types (with-open [rdr (io/reader (io/resource "types.csv"))]
                 (doall (apply hash-map (flatten
                                                 (map
                                                  (fn [[x y]]
                                                    [(java.lang.Integer/parseInt x) y])
                                                  (map
                                                   #(str/split % #"\|")
                                                   (line-seq rdr))))))))
