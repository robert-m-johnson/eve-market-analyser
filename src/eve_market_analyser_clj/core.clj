(ns eve-market-analyser-clj.core
  (:gen-class)
  (:require [eve-market-analyser-clj.feed :as feed]))

(defn -main []
  (feed/listen))
