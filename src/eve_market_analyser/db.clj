(ns eve-market-analyser.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [clojure.tools.logging :as log]
            [eve-market-analyser.world :as world]
            [clj-time.coerce]
            [com.stuartsierra.component :as component]
            ;; Enable joda integration
            [monger.joda-time])
  (:import [com.mongodb
            BasicDBObject MongoOptions ServerAddress WriteConcern]))

;; Use fastest write concern so that pi can keep up
;; TODO: Use bulk writes so that a safer setting can be used
(mg/set-default-write-concern! WriteConcern/UNACKNOWLEDGED)

(defn- create-conn []
  (let [^ServerAddress server
        (mg/server-address "127.0.0.1" 27017)
        ^MongoOptions opts
        (mg/mongo-options {:connections-per-host 2})]
    (mg/connect server opts)))

(def ^:private market-item-coll "marketItem")

(defn- get-db [conn] (mg/get-db conn "eve"))

(defn- orderItem->doc [orderItem]
  (doto (BasicDBObject.)
    (.put "price" (:price orderItem))
    (.put "quantity" (:quantity orderItem))))

(defn- marketItem->doc [marketItem]
  (doto (BasicDBObject.)
    (.put "typeId" (:typeId marketItem))
    (.put "itemName" (:itemName marketItem))
    (.put "regionId" (:regionId marketItem))
    (.put "regionName" (:regionName marketItem))
    (.put "sellingPrice" (:sellingPrice marketItem))
    (.put "buyingPrice" (:buyingPrice marketItem))
    (.put "generatedTime" (clj-time.coerce/to-date (:generatedTime marketItem)))
    (.put "sellOrders" (map orderItem->doc (:sellOrders marketItem)))
    (.put "buyOrders" (map orderItem->doc (:buyOrders marketItem)))))

(defn- insert-items* [conn items]
  (log/debug "Inserting items into DB...")
  (doseq [item items]
    (let [doc (marketItem->doc item)
          update-query {"typeId" (:typeId item)
                       "regionId" (:regionId item)
                       "generatedTime" {"$lt" (:generatedTime item)}}]
      (try
        (mc/update (get-db conn) market-item-coll update-query doc {:upsert true})
        (catch com.mongodb.DuplicateKeyException e
          (log/debug "Item older than current; ignoring")))))
  (log/debug "Inserted items into DB"))

(def ^:private hub-ordering
  (let [names world/trade-hub-region-names]
    (->>
     (map #(vector % (.indexOf names %)) names)
     flatten
     (apply hash-map))))

(defn- find-hub-prices-for-item-name*
  ([conn item-name]
   (find-hub-prices-for-item-name* conn item-name nil))
  ([conn item-name fields]
   (let [results
         (mc/find-maps (get-db conn) market-item-coll
                       {:itemName item-name
                        :regionName {$in world/trade-hub-region-names}}
                       (if fields fields {}))]
     ;; If region name was included in the search, then sort according to
     ;; the trade hub priority order
     (if (some #{:regionName} fields)
       (sort-by #(hub-ordering (:regionName %)) results)
       results))))

;;; Components

(defrecord MongoDatabase [connection]
  component/Lifecycle
  (start [{connection :connection :as component}]
    (log/debug "Starting database")
    (if connection
      component
      (let [conn (create-conn)]
        (assoc component :connection conn))))
  (stop [{connection :connection :as component}]
    (log/debug "Stopping database")
    (let [component (if connection
                      (do (.close connection)
                          (assoc component :connection nil))
                      component)]
      (log/debug "Stopped database")
      component)))

(defprotocol ItemDatabase
  (insert-items [database items])
  (find-hub-prices-for-item-name
    [database item-name]
    [database item-name fields]))

(extend-type MongoDatabase
  ItemDatabase
  (insert-items [{connection :connection} items]
    (insert-items* connection items))
  (find-hub-prices-for-item-name
    ([{connection :connection} item-name]
     (find-hub-prices-for-item-name* connection item-name))
    ([{connection :connection} item-name fields]
     (find-hub-prices-for-item-name* connection item-name fields))))

(defn new-database []
  (->MongoDatabase nil))