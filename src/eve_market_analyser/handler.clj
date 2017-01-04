(ns eve-market-analyser.handler
  (:require [eve-market-analyser.db :as db]
            [eve-market-analyser.model :as model]
            [eve-market-analyser.views :as views]
            [compojure.core :as cc]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]]
            [selmer.parser :as parser]

            [clojure.tools.logging :as log]))

(defn fetch-hub-prices-model [db item-name]
  (let [hub-items
        (db/find-hub-prices-for-item-name
         db
                   item-name
                   [:regionName :buyingPrice :sellingPrice :generatedTime])]
    (model/hub-prices item-name hub-items)))

(defn app-routes [db]
  (cc/routes
   (cc/GET "/" [] (views/render (views/index)))
    (cc/GET "/hub-item" [itemName]
      (let [model (fetch-hub-prices-model db itemName)]
        (views/render (views/hub-item model))))
    (route/resources "/")
    (route/not-found "Not Found")))

(defn app [db]
  (handler/site (app-routes db)))

(defrecord WebServer [http-server db]
  component/Lifecycle
  (start [this]
    (if http-server
      this
      (do
        (log/debug "Starting web server")
        (assoc this :http-server
               (run-jetty (app db) {:port 8080 :join? false})))))
  (stop [this]
    (if http-server
      (do (log/debug "Stopping web server")
          (.stop http-server)
          (assoc this :http-server nil))
      this)))

(defn new-web-server []
  (->WebServer nil nil))
