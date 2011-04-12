(ns cmarks.core
  (:require [cmarks.data :as data])
  (:require [net.cgrand.enlive-html :as html])
  (:require [clojure.string :as string])
  (:require [clojure.java.io :as jio]))

(defn print-records
  []
  (let [records (data/select-from-db)]
    (map #(println (str (:url %) " " (:title %))) records))) 

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn get-titles [url]
  (map html/text (html/select (fetch-url url) [:h1])))

(defn find-title [url]
  (try
    (first (get-titles url))
    (catch  java.net.MalformedURLException e
      (println (str "MalformedURL for [" url "]")))
    (catch java.io.IOException e
      (println (.getMessage e)))))

(defn find-in-cache
  [url] ;str
  

(defn find-doc
  [url]
  (let [from-cache (find-in-cache url)]
    (if from-cache from-cache
      (cache-and-get url))))

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

(defn cache-url
  [url] ;string
  (jio/copy 
    (jio/input-stream (jio/as-url url)) 
    (jio/as-file (url-to-cachename url))))

(defn loop-match-records
  []
  (loop [records (data/select-from-db)]
    (let [r (first records)
          rtitle (:title r)
          gtitle (find-title (:url r))]
      (if (not= rtitle gtitle)
        (println (str "id# " (:id r) " is bad\n\treal: " rtitle "\n\tfound: " gtitle))
        (println (str "id# " (:id r) " is good")))
      (recur (rest records)))))

(defn match-records
  []
  (for [r (data/select-from-db)]
    (let [rtitle (:title r)
          gtitle (find-title (:url r))]
      (if (not= rtitle gtitle)
        (println (str "id# " (:id r) " is bad\n\treal: " rtitle "\n\tfound: " gtitle))
        (println (str "id# " (:id r) " is good"))))))

(defn print-records
  []
  (for [r (data/select-from-db)]
    (str "id=" (:id r) " url=" (:url r))))


