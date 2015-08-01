(ns eve-market-analyser-clj.core
  (:gen-class)
  (:require [zeromq.zmq :as zmq])
  (:import java.util.zip.Inflater
           java.nio.charset.Charset))

(defn decompress [byte-arr]
  (let [inflater (Inflater.)
        out-byte-arr (byte-array (-> (count byte-arr) (* 16)))]
    (.setInput inflater byte-arr)
    (let [length (.inflate inflater out-byte-arr)]
      (byte-array length out-byte-arr))))

(defn byte-arr-to-string [byte-arr]
  (String. byte-arr (Charset/forName "UTF-8")))

(defn -main []
  (let [context (zmq/context 1)]
    (println "Connecting to EMDR server…")
    (with-open [subscriber (doto (zmq/socket context :sub)
                             (zmq/connect "tcp://relay-eu-germany-1.eve-emdr.com:8050")
                             (zmq/set-receive-timeout 10000)
                             (zmq/subscribe ""))]
      (dotimes [i 10]
        (println "Receiving item...")
        (let [bytes (zmq/receive subscriber)]
          (println "Received :" (vec bytes)))))))
