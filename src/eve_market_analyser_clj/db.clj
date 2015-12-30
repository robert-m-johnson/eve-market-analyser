(ns eve-market-analyser-clj.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

(defonce ^:private conn (mg/connect))

(defonce ^:private db (mg/get-db conn "eve"))

(def ^:private marketItems "marketItems")

(defn insert-items [items]
  (doseq [item items]
    (mc/insert db marketItems item)))
