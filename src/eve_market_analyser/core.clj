(ns eve-market-analyser.core
  (:gen-class)
  (:require [eve-market-analyser.db :as db]
            [eve-market-analyser.feed :as feed]
            [eve-market-analyser.handler :as handler]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn system []
  (component/system-map
   :db (db/new-database)
   :http-server (component/using
                 (handler/new-web-server)
                 [:db])))

(defn -main [& args]
  (feed/consume)
  (feed/convert)
  (feed/listen)
  (component/start (system)))

