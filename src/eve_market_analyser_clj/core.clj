(ns eve-market-analyser-clj.core
  (:gen-class)
  (:require [eve-market-analyser-clj.feed :as feed]
            [eve-market-analyser-clj.handler :as handler]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn -main [& args]
  (feed/listen)
  (feed/consume)
  (defonce server (run-jetty #'handler/app {:port 8080 :join? false})))

