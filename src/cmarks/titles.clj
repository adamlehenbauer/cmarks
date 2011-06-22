(ns cmarks.titles
  (:require 
    [net.cgrand.enlive-html :as html]
    [clojure.string :as string]
    [cmarks.data :as data]))

(defn hs [res]
  "headers h1-h4"
  (vec (flatten 
         (for [hx [:h1 :h2 :h3 :h4]]
           (map html/text (html/select res [hx]))))))

(defn page-title
  [res]
  "head/title element"
  (map html/text (html/select res [:head/title])))

(defn- raw-candidates
  [res] ;html-resource
  ;helpd: http://groups.google.com/group/clojure/browse_thread/thread/db2cdef20d7e6245?pli=1
  (concat
    (hs res)
    (page-title res)))

(defn clean-title
  [t]
  (-> t 
    (string/replace ,, #"\n\t*" "")
    (string/replace ,, #" +" " ")
    string/trim))

(defn clean-candidates 
  [res]
  (map clean-title (raw-candidates res)))

(defn title-candidates
  [url] ;str
  (clean-candidates (data/find-resource url)))

(defn b
  [i]
  (Integer/toBinaryString i))

(defn h
  [i]
  (Integer/toHexString i))