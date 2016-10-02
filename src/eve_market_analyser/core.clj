(ns eve-market-analyser.core
  (:gen-class)
  (:require [eve-market-analyser.db :as db]
            [eve-market-analyser.feed :as feed]
            [eve-market-analyser.handler :as handler]
            [clojure.core.async :as async :refer [chan sliding-buffer]]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.tools.logging :as log]))

(defn create-system []
  (let [bytes-chan (chan (sliding-buffer 500))
        region-items-chan (chan 50)]
    (component/system-map
     :item-producer (feed/new-item-producer bytes-chan)
     :item-converter (feed/new-item-converter bytes-chan region-items-chan)
     :db (db/new-database)
     :item-consumer (component/using
                     (feed/new-item-consumer region-items-chan)
                     [:db])
     :http-server (component/using
                   (handler/new-web-server)
                   [:db]))))

(defonce system (atom (create-system)))

(defn start-system []
  (swap! system component/start))

(defn stop-system []
  (swap! system component/stop-system))

(defn -main [& args]
  (start-system)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (log/info "Shutting down...")
                               (stop-system)))))

