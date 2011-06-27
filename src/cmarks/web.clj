(ns cmarks.web
  (:use ring.adapter.jetty)
  (:use ring.middleware.reload)
  (:require
    [cmarks.titles :as titles]))

(defn handler [{qs :query-string uri :uri :as req}]
  (when (= uri "/")
	  (let [cs (titles/title-candidates qs)]
		  {:status 200
		   :headers {"Content-Type" "text/html"}
		   :body (apply str "Hello Word from String!\n<br/>\n" (interpose "\n<br/>\n" cs))})))

(def app
  (wrap-reload #'handler '(cmarks.web)))

(defn boot []
  (run-jetty #'app {:port 8000}))
