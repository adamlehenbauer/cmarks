(ns cmarks.core
  (:require 
    [cmarks.data :as data]
    [cmarks.titles :as titles]
    [net.cgrand.enlive-html :as html]
    [clojure.string :as string]
    [clojure.java.io :as jio]
    [clojure.contrib.logging :as logger]
    [clojure.contrib.pprint :as pprint]))

(defn print-records
  []
  (let [records (data/filtered-from-db)]
    (map #(println (str (:url %) " " (:title %))) records))) 

(defn get-titles [url]
  (map html/text (html/select (data/find-resource url) [:h1])))

(defn find-title
  [url]
  (let [title (first (get-titles url))]
    (if title
      (string/trim title)
      nil)))

(defn loop-match-records
  []
  (loop [rs (data/filtered-from-db)
         r (first rs)
         ret []]
    (if (seq rs)
     (let [rtitle (:title r)
            tcs (titles/title-candidates (:url r))]
       (if (some #(= rtitle %) tcs)
          (recur 
            (next rs)
            (second rs)
            (conj ret (assoc r :success true)))
          (recur
            (next rs)
            (second rs)
            (conj ret (assoc r :success false :found tcs)))))
     ret)))

(defn match-records
  []
  (let [results []]
    (for [r (data/filtered-from-db)]
      (let [rtitle (:title r)
            title-candidates (titles/title-candidates (:url r))]
        (if (some #(= rtitle %) title-candidates)
          (assoc r :success true)
          (assoc r :success false :found title-candidates))))
    results))

(defn print-records
  []
  (for [r (data/filtered-from-db)]
    (str "id=" (:id r) " url=" (:url r))))

(defn good [r] (str "good: " (:title r)))
(defn bad [r]  (str "bad for: " (:url r) "\n\texpected: " (:title r) "\n\tfound: " (:found r)))

(defn good-or-bad
  [m r]
  (if (:success r)
    (assoc m :good (inc (:good m)))
    (assoc m :bad (inc (:bad m)))))

(defn report
  []
  (reduce good-or-bad {:good 0 :bad 0} (loop-match-records)))

(defn test-matches
  []
  (loop [rs (loop-match-records)
         r (first rs)
         good 0
         bad 0]
    (if (seq rs)
      (if (:success r)
        (recur (next rs) (second rs) (inc good) bad)
        (recur (next rs) (second rs) good (inc bad)))
      {:good good :bad bad})))

(defn save-misses
  "save a formatted map of urls that failed to match a candidate title"
  []
  (with-open [w (jio/writer "output.txt")]
    (pprint/pprint (filter #(not (:success %)) (loop-match-records)) w)))
    