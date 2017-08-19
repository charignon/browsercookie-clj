(defproject browsercookie "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [crypto-password "0.2.0"]
                 [org.clojure/java.jdbc  "0.7.0-alpha1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.xerial/sqlite-jdbc "3.16.1"]]
  :main ^:skip-aot browsercookie.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
