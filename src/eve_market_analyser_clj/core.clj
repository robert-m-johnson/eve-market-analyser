(ns eve-market-analyser-clj.core
  (:gen-class)
  (:require [zeromq.zmq :as zmq]))

(defn -main []
  (let [context (zmq/context 1)]
    (println "Connecting to hello world server…")
    (with-open [socket (doto (zmq/socket context :req)
                         (zmq/connect "tcp://127.0.0.1:5555"))]
      (dotimes [i 10]
        (let [request "Hello"]
          (println "Sending hello " i "…")
          (zmq/send-str socket request)
          (zmq/receive socket)
          (println "Received world " i))))))
