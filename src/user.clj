(require '(clojure.contrib [repl-utils :as rutils]))
(require '(net.cgrand [enlive-html :as html]))
;(use 'clojure.contrib.pprint)

(def classpath (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader))))

; http://www.learningclojure.com/2010/03/conditioning-repl.html
; can't get these to work, but (pprint classpath) is good enough for now
;(defn print-cp [] (pp classpath))
;(defn print-cp [] (clojure.contrib.pprint/pprint classpath))

;works well when given an instance of some class,
; but when given a class, just gives the type heirarchy of java.lang.Class
(defn
  all-types
  "returns the type heirarchy (interfaces then superclasses) of an object"
  [arg]
  (into
    (vec (.getInterfaces (class arg)))
    (loop [next-super (class arg) acc []]
      (if-not (.getSuperclass next-super)
        acc
        (recur (.getSuperclass next-super) (conj acc next-super))))))