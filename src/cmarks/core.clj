(ns cmarks.core
  (:require [cmarks.data :as data])
  (:require [net.cgrand.enlive-html :as html])
  (:require [clojure.string :as string])
  (:require [clojure.java.io :as jio])
  (:require [clojure.contrib.logging :as logger]))

(defn print-records
  []
  (let [records (data/select-from-db)]
    (map #(println (str (:url %) " " (:title %))) records))) 

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
      (jio/input-stream (jio/as-url url)) 
      (jio/as-file (url-to-cachename url)))
    (catch  java.net.MalformedURLException e
      (println (str "MalformedURL for [" url "]")))
    (catch java.io.IOException e
      (println (.getMessage e)))))

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
      (do
        (logger/info "found url in cache")
        from-cache)
      (do
        (logger/info "fetching and caching url") 
        (cache-and-get url)))))

(defn get-titles [url]
  (map html/text (html/select (find-resource url) [:h1])))

(defn find-title
  [url]
  (let [title (first (get-titles url))]
    (if title
      (string/trim title)
      nil)))

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
  (let [results []]
    (for [r (data/select-from-db)]
      (let [rtitle (:title r)
            gtitle (find-title (:url r))]
        (if (not= rtitle gtitle)
          (assoc r :success false :found gtitle)
          (assoc r :success true))))))

(defn print-records
  []
  (for [r (data/select-from-db)]
    (str "id=" (:id r) " url=" (:url r))))

(defn report
  []
  (loop [rs (match-records)
         rp []]
    (if (:success r)
      (recur (next 
      (conj rp (str "good: " (:title r)))
      (conj rp (str "bad for: " (:url r) "\n\texpected: " (:title r) "\n\tfound: " (:found r)))))
    (into [] rp))  


