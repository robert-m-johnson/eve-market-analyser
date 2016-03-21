(ns eve-market-analyser-clj.handler
  (:require [eve-market-analyser-clj.db :as db]
            [compojure.core :as cc]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [selmer.parser :as parser]
            [clj-time.core :as t]))

(defn- fetch-hub-prices-model [itemName]
  (let [results (db/find-hub-prices-for-item-name
                 itemName :itemName :regionName :buyingPrice :sellingPrice :generatedTime)
        earliest-generated (if (empty? results)
                             nil
                             (t/earliest (map :generatedTime results)))]
    {:itemName itemName
     :results results
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
