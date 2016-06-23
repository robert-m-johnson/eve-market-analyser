(ns eve-market-analyser.handler
  (:require [eve-market-analyser.db :as db]
            [eve-market-analyser.model :as model]
            [eve-market-analyser.views :as views]
            [compojure.core :as cc]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [selmer.parser :as parser]
            ))

(defn fetch-hub-prices-model [item-name]
  (let [hub-items (db/find-hub-prices-for-item-name
                   item-name
                   :regionName :buyingPrice :sellingPrice :generatedTime)]
    (model/hub-prices item-name hub-items)))

(cc/defroutes app-routes
  (cc/GET "/" [] (views/render (views/index)))
  (cc/GET "/hub-item" [itemName]
    (let [model (fetch-hub-prices-model itemName)]
      (views/render (views/hub-item model))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
