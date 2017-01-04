(ns eve-market-analyser.worker
  (:require [clojure.core.async :as async
             :refer [alt!! chan <!! >!!]]
            [clojure.tools.logging :as log]))

(defprotocol Worker
  (start! [this])
  (stop! [this])
  (running? [this]))

(defn- open?!! [stop-chan]
  (alt!! stop-chan false :default true))

(defn- wrap-log-err [f]
  (fn [stop-chan]
    (try
      (f stop-chan)
      (catch Exception e
        (log/error e "Unhandled exception in thread worker")))))

(defn thread-worker [work-fn]
  (let [work-fn (wrap-log-err work-fn)
        stop-chan (atom nil)
        run? (fn [] (some? (and @stop-chan
                                (open?!! @stop-chan))))]
    (reify Worker
      (start! [this]
        (when-not @stop-chan
          (reset! stop-chan (chan))
          (async/thread
            (while (run?)
              (work-fn run?))))
        this)
      (stop! [_]
        (when @stop-chan
          (async/close! @stop-chan)
          (reset! stop-chan nil)))
      (running? [_]
        (run?)))))

