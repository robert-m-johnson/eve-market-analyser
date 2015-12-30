(defproject eve-market-analyser-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/algo.generic "0.1.2"]
                 [org.zeromq/cljzmq "0.1.4"]
                 ;; JSON parsing
                 [cheshire "5.5.0"]
                 ;; MongoDB
                 [com.novemberain/monger "3.0.0-rc2"]]
  :jvm-opts ["-Djava.library.path=/usr/lib:/usr/local/lib"]
  :main ^:skip-aot eve-market-analyser-clj.core
  ;; :global-vars {*warn-on-reflection* true}
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[midje "1.7.0" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.1.3"]]}
             :uberjar {:aot :all}})
