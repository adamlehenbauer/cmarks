(defproject cmarks "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [mysql/mysql-connector-java "5.1.7"]
                 [commons-dbcp/commons-dbcp "1.2.1"]
                 [enlive "1.0.0-SNAPSHOT"]
                 [clj-http "0.1.2"]
                 [ring/ring-core "0.2.0"]
                 [ring/ring-jetty-adapter "0.2.0"]]
  :dev-dependencies 
  [[jline/jline "0.9.94"]
   [ring/ring-devel "0.2.0"]])
