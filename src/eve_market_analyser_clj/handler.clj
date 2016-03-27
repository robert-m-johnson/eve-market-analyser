(ns eve-market-analyser-clj.handler
  (:require [eve-market-analyser-clj.db :as db]
            [compojure.core :as cc]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [selmer.parser :as parser]
            [clj-time.core :as t]))

(defn highest-selling-price [items]
  (apply max (map :sellingPrice items)))

(defn lowest-selling-price [items]
  (apply min (map :sellingPrice items)))

(defn highest-buying-price [items]
  (apply max (map :buyingPrice items)))

(defn lowest-buying-price [items]
  (apply min (map :buyingPrice items)))

(defn mark-best-prices [items]
  (let [highest-sp (highest-selling-price items)
        lowest-sp (lowest-selling-price items)
        highest-bp (highest-buying-price items)
        lowest-bp (lowest-buying-price items)
        mark-val (fn [item k v mark]
                   (if (= (k item) v)
                     (assoc item mark true)
                     item))]
    (map
     (comp
      #(mark-val % :sellingPrice highest-sp :highestSellingPrice)
      #(mark-val % :sellingPrice lowest-sp :lowestSellingPrice)
      #(mark-val % :buyingPrice highest-bp :highestBuyingPrice)
      #(mark-val % :buyingPrice lowest-bp :lowestBuyingPrice))
     items)))

(defn fetch-hub-prices-model [item-name]
  (let [hub-items (db/find-hub-prices-for-item-name
                 item-name :itemName :regionName :buyingPrice :sellingPrice :generatedTime)
        earliest-generated (if (empty? hub-items)
                             nil
                             (t/earliest (map :generatedTime hub-items)))]
    {:itemName item-name
     :hubItems hub-items
     :earliestGenerated earliest-generated}))

(cc/defroutes app-routes
  (cc/GET "/" [] (ring.util.response/resource-response "public/index.html"))
  (cc/GET "/hub-item" [itemName]
    (let [model (fetch-hub-prices-model itemName)]
      (parser/render-file "templates/hub-item.html" model)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
