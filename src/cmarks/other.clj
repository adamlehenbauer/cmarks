(ns cmarks.other
  (:require [clojure.java.io :as jio]))

(defn touch 
  [file]
  (jio/make-parents file)
  (spit file ""))

(defn touch-cache
  []
  (touch (str *cache-dir* "1.txt"))
  (touch (str *cache-dir* "2.txt"))
  (touch (str *cache-dir* "3.txt")))