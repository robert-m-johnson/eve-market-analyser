(ns eve-market-analyser-clj.handler
  (:require [compojure.core :as cc]
            [compojure.handler :as handler]
            [compojure.route :as route]))

(cc/defroutes app-routes
  (cc/GET "/" [] (ring.util.response/resource-response "public/index.html"))
  (cc/GET "/hub-item" [itemName] (ring.util.response/response itemName))
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
