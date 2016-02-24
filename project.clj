(defproject org.clojars.kokos/storage "0.1.1"
  :description "Lucene simple storage. With rest server and CRUD capabilities."
  :url "https://github.com/kokosro/storage"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo/"
                                    :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [digest "1.4.4"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojars.kostafey/clucy "0.5.4.1d"]
                 [http-kit "2.1.16"]
                 [ring "1.3.0-RC1"]
                 [compojure "1.1.8"]
                 [javax.servlet/servlet-api "2.5"]]
  :main ^:skip-aot kokos.storage.server
  :uberjar-merge-with {#"org\.apache\.lucene\.codecs\.*" [slurp str spit]}
  :target-path "target/%s"
  :profiles {:1.6  {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7  {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8  {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :uberjar {:aot :all}})
