(defproject eve-market-analyser "0.1.0-SNAPSHOT"
  :description "Aggegrates Eve Online market data using EMDR"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/algo.generic "0.1.2"]
                 [org.zeromq/cljzmq "0.1.4"]
                 [clj-time "0.13.0"]
                 [com.github.kevinsawicki/timeago "1.0.1"]
                 [com.stuartsierra/component "0.3.2"]
                 ;; JSON parsing
                 [cheshire "5.6.3"]
                 ;; MongoDB
                 [com.novemberain/monger "3.0.1"]
                 ;; Web
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [compojure "1.5.1"]
                 [hiccup "1.0.5"]
                 ;; Logging
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17"]
                 ]
  :jvm-opts ["-Djava.library.path=/usr/lib:/usr/local/lib"]
  :main ^:skip-aot eve-market-analyser.core
  ;; :global-vars {*warn-on-reflection* true}
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[midje "1.7.0" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.1.3"]]}
             :uberjar {:aot :all}})
