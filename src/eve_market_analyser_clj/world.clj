(ns eve-market-analyser-clj.world
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def types (with-open [rdr (io/reader (io/resource "types.csv"))]
             (let [lines (line-seq rdr)
                   pairs (map
                          (fn [line]
                            ;; Split the CSV lines by the pipe | symbol
                            (let [[id-str name] (str/split line #"\|")]
                              ;; Parse the ID string into an int
                              [(java.lang.Integer/parseInt id-str) name]))
                          lines)]
               ;; Create a map of the ID-name pairs
               (->> pairs flatten (apply hash-map)))))
