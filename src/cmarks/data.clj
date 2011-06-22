(ns cmarks.data
  (:use clojure.contrib.sql)
  (:require
    [net.cgrand.enlive-html :as html]
    [clojure.string :as string]
    [clojure.java.io :as jio]
    [clj-http.client :as client]))

; database access

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
     (.setLogAbandoned false)
     (.setLogWriter (java.io.PrintWriter. "/dev/null")))})

(defn select-from-db
  []
  (with-connection ds
    (with-query-results rs ["select * from articles_read_article"]
      (into [] rs))))

(defn find-article-by-id
  [id]
  (with-connection ds
    (with-query-results rs ["select * from articles_read_article where id = ?" id]
      (first rs))))

; these just muddy up the data
(def bad-ids
  #{
   54 ;gone from the web
   24 ;bad https
   27 ;weird 403 i can't repro outside of java.net.URL
   1 ; new yorker with tricky title
 })

(defn filtered-from-db
  []
  (filter #(not (contains? bad-ids (:id %))) (select-from-db)))

; URL & caching

(def *cache-dir*
  ; create dir and return dir name
  (do
    (doto (java.io.File. "cache/") (.mkdir))
    "cache/"))

(defn strip-protocol
  [url]
  (cond 
    (.startsWith url "https://") (string/replace url "https://" "")
    (.startsWith url "http://") (string/replace url "http://" "")))

(defn strip-slash
  [url]
  ;(if (.endsWith url "/") (.substring url 0 (dec (.length url)))))
  (string/replace url "/" "."))

(defn url-to-cachename
  [url] ;string
  (str *cache-dir*
    (strip-slash (strip-protocol url))))

(defn find-in-cache
  [url] ;str
  (let [f (jio/as-file (url-to-cachename url))]
    (if (.exists f)
      (html/html-resource f)
      nil)))

(defn cache-url
  [url] ;string
  (try
    (jio/copy 
      (:body (client/get url)) 
      (jio/as-file (url-to-cachename url)))
    (catch  java.net.MalformedURLException e
      (println (str "MalformedURL for [" url "]")))
    (catch java.io.IOException e
      (println (str "IO error for [" url "] " (.getMessage e))))))

(defn cache-and-get
  [url] ;str
  (do
    (cache-url url)
    ;saves to disk, then reads back in
    ;a little extra work, but no biggie
    (find-in-cache url)))

(defn find-resource
  [url]
  (let [from-cache (find-in-cache url)]
    (if from-cache 
      from-cache
      (cache-and-get url))))

