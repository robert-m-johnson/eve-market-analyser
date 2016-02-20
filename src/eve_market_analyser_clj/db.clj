(ns eve-market-analyser-clj.db
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import com.mongodb.BasicDBObject))

(defonce ^:private conn (mg/connect))

(defonce ^:private db (mg/get-db conn "eve"))

(def ^:private marketItemColl "marketItem")

(defn- orderItem->doc [orderItem]
  (doto (BasicDBObject.)
    (.put "price" (:price orderItem))
    (.put "quantity" (:quantity orderItem))))

(defn- marketItem->doc [marketItem]
  (doto (BasicDBObject.)
    (.put "typeID" (:typeID marketItem))
    (.put "itemName" (:itemName marketItem))
    (.put "regionID" (:regionID marketItem))
    (.put "regionName" (:regionName marketItem))
    (.put "sellingPrice" (:sellingPrice marketItem))
    (.put "buyingPrice" (:buyingPrice marketItem))
    (.put "generatedTime" (:generatedTime marketItem))
    (.put "sellOrders" (map orderItem->doc (:sellOrders marketItem)))
    (.put "buyOrders" (map orderItem->doc (:buyOrders marketItem)))))

(defn insert-items [items]
  (doseq [item items]
    (let [doc (marketItem->doc item)
          updateQuery {"typeID" (:typeID item)
                       "regionID" (:regionID item)
                       "generatedTime" {"$lt" (:generatedTime item)}}]
      (try
        (mc/update db marketItemColl updateQuery doc {:upsert true})
        (catch com.mongodb.DuplicateKeyException e
          (prn "Item older than current; ignoring"))))))

