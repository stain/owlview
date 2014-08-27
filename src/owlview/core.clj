(ns owlview.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html4 html5 xhtml include-css include-js]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes ANY GET POST PUT DELETE]]))

(defn xhtml? [ctx]
;  (= "application/xhtml+xml";
;     (print
;    (get-in ctx [:representation :media-type]))
    false)

(defroutes app
  (ANY "/" [] (resource :available-media-types ["text/html" "application/xhtml+xml"]
                        :handle-ok (fn [ctx] (html5 {:xml? (xhtml? ctx)}
                            [:head
                              [:title "owlview"]
                              (include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.css"
                                "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap-theme.css"
                                )]
                            [:body
                             [:div {:class "container"}
                              [:h1 "owlview"]
                              [:div {:class "jumbotron"}
                                  "Visualize an OWL/RDFS ontology:"
                                  [:form {:role "form" :method "POST" :action "ont/"}
                                     [:p [:input {:name "url" :type "url" :class "form-control" :placeholder "http://purl.org/pav/" :autofocus :autofocus}]]
                                     "or:"
                                     [:p [:input {:name "file" :type "file" :class "form-control"
                                                    :accept "application/rdf+xml,text/turtle,application/owl+xml,.owl,.rdf,.ttl,.owx"}]]
                                     [:p [:input {:type "submit" :class "btn btn-primary btn-lg" :value "Visualize"}]]
                                  ]
                              ]]
                              [:address {:class "footer"}
                                [:a {:href "https://github.com/stain/owlview"} "owlview"]
                                " by "
                                [:a {:href "http://orcid.org/0000-0001-9842-9718"}
                                "Stian Soiland-Reyes"]]
                              (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.js"
                                          "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.js")
                          ])))))

(def handler
  (-> app
      (wrap-params)))
