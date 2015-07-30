(ns eve-market-analyser-clj.core
  (:gen-class)
  (:require [zeromq.zmq :as zmq]))

(defn -main []
  (let [context (zmq/context 1)]
    (println "Connecting to hello world server…")
    (with-open [subscriber (doto (zmq/socket context :sub)
                             (zmq/connect "tcp://relay-eu-germany-1.eve-emdr.com:8050")
                             (zmq/set-receive-timeout 10))]
      (dotimes [i 10]
        (println "Receiving item...")
        (zmq/receive subscriber)
        (println "Received item ")))))
