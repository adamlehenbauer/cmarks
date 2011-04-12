(ns cmarks.data
  (:use clojure.contrib.sql))

(def db
  (let [db-host "localhost"
	      db-port 3306
	      db-name "redmarks"]
  {:classname "com.mysql.jdbc.Driver"
	           :subprotocol "mysql"
	           :subname (str "//" db-host ":" db-port "/" db-name)
	           :user "flyer"
	           :password "daytona"}))

;https://github.com/swannodette/second-post/blob/master/src/second_post/mysql.clj
(def ds
  {:datasource
   (doto
     (org.apache.commons.dbcp.BasicDataSource.)
     (.setDriverClassName (:classname db))
     (.setUrl (format "jdbc:%s:%s" (:subprotocol db) (:subname db)))
     (.setUsername (:user db))
     (.setPassword (:password db))
     (.setMaxIdle 8)
     (.setMaxActive 30)
     (.setValidationQuery "SELECT 1")
     (.setTestOnBorrow true)
     (.setTestWhileIdle true)
     (.setTimeBetweenEvictionRunsMillis 20000)
     (.setMinEvictableIdleTimeMillis 20000)
     (.setRemoveAbandoned true)
     (.setRemoveAbandonedTimeout 10)
     (.setLogAbandoned true))})

(defn select-from-db
  []
  (with-connection ds
    (with-query-results rs ["select * from articles_read_article"]
      (into [] rs))))