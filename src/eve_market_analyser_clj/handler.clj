(ns eve-market-analyser-clj.handler
  (:require [eve-market-analyser-clj.db :as db]
            [compojure.core :as cc]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [selmer.parser :as parser]))

(cc/defroutes app-routes
  (cc/GET "/" [] (ring.util.response/resource-response "public/index.html"))
  (cc/GET "/hub-item" [itemName]
    (let [results (db/find-hub-prices-for-item-name itemName)]
      (parser/render-file "templates/hub-item.html" {:itemName itemName :results results})))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
