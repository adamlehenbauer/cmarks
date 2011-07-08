(ns cmarks.titles
  (:require 
    [net.cgrand.enlive-html :as html]
    [clojure.string :as string]
    [cmarks.data :as data]))

; like flatten but leaves vectors as is
(defn vflat [f x]
  (filter (complement f)
    (rest (tree-seq f seq x))))

(defn hx [res h]
  "gets seq of headers in a resource"
  (map html/text (html/select res [h])))

(defn clean-title
  [t]
  (-> t 
    (string/replace ,, #"\n\t*" "")
    (string/replace ,, #" +" " ")
    string/trim))

(defn entry [title score]
  (let [title (clean-title title)]
    (if (string/blank? title)
      nil
      {:score score :title title}))) 

(defn hs [res]
  (filter identity ;drop nils
    (flatten
      (for [[h score] [[:h1 4] [:h2 3] [:h3 2] [:h4 1]]]
        (for [title (hx res h)]
          (entry title score))))))

;(defn hs [res]
;  "headers h1-h4"
;  (let [score {:h1 4, :h2 3, :h3 2, :h4 1}]
;    (filter (complement string/blank?)
;      (for [hx [:h1 :h2 :h3 :h4]]
;        (map #(hash-map :score (hx score) :title (html/text %)) (html/select res [hx]))))))

(defn page-title
  [res]
  "head/title element"
  (entry
    (first
      (map html/text (html/select res [:head/title]))) 4))

(defn- raw-candidates
  [res] ;html-resource
  ;helpd: http://groups.google.com/group/clojure/browse_thread/thread/db2cdef20d7e6245?pli=1
  (concat
    (hs res)
    (page-title res)))

;(defn clean-candidates 
;  [res]
;  (map clean-title (raw-candidates res)))

(defn title-candidates
  [url] ;str
  (raw-candidates (data/find-resource url)))
