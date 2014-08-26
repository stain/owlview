(ns owlview.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes ANY GET POST PUT DELETE]]))


(defroutes app
  (ANY "/" [] (resource :available-media-types ["text/html" "application/xhtml+xml"]
                        :handle-ok "<html xmlns='http://www.w3.org/1999/xhtml'><body>Welcome</body></html>"))
  (ANY "/foo" [] (resource :available-media-types ["text/html"]
                           :handle-ok (fn [ctx]
                                        (format "<html>It's %d milliseconds since the beginning of the epoch."
                                                (System/currentTimeMillis))))))

(def handler
  (-> app
      (wrap-params)))
