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
  (let [bytes-chan (chan (sliding-buffer 250))
        region-items-chan (chan 25)]
    (component/system-map
     :bytes-chan bytes-chan
     :region-items-chan region-items-chan
     :item-producer (feed/new-item-producer bytes-chan)
     :item-converter (feed/new-item-converter bytes-chan region-items-chan)
     :db (db/new-database)
     :item-consumer (component/using
                     (feed/new-item-consumer region-items-chan)
                     [:db])
     :http-server (component/using
                   (handler/new-web-server)
                   [:db]))))

(defonce system (atom nil))

(defn start-system []
  (swap! system (fn [s]
                  (if s
                    s
                    (component/start-system (create-system))))))

(defn stop-system []
  (swap! system (fn [s]
                  (when s (component/stop-system s))
                  nil)))

(defn -main [& args]
  (start-system)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (log/info "Shutting down...")
                               (stop-system)))))

