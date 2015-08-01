(defproject eve-market-analyser-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.zeromq/cljzmq "0.1.4"]
                 ;; JSON parsing
                 [cheshire "5.5.0"]]
  :jvm-opts ["-Djava.library.path=/usr/lib:/usr/local/lib"]
  :main ^:skip-aot eve-market-analyser-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
