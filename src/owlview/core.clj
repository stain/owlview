(ns owlview.core
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html4 html5 xhtml include-css include-js]]
            [ring.adapter.jetty :refer [run-jetty]]
            [owlapi.core :refer [with-owl load-ontology loaded-ontologies classes
                                object-properties data-properties annotation-properties
                                ranges-of-property annotations]]
            [compojure.core :refer [defroutes ANY]]))

(defn xhtml? [ctx]
;  (= "application/xhtml+xml";
;     (print
;    (get-in ctx [:representation :media-type]))
    false)

(def known-ontologies {})

(defn get-ontology [uri]
  ;; TODO: Store in loaded-ontologies?
  (load-ontology uri))

(defroutes app
  (ANY "/" [] (resource
    :available-media-types ["text/html" "application/xhtml+xml"]

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
                          ]))))
  (ANY "/ont/" [] (resource
      :available-media-types ["text/html"]
      :allowed-methods [:post]
      :post! (fn [ctx] (str "OK. " ctx))
      :post-redirect? (fn [ctx] {:location (format "/ont/%s" "http://purl.org/pav/")})
    ))
  (ANY "/ont/*" [& {url :*}] (resource
    :available-media-types ["text/html" "application/xhtml+xml"]
    :handle-ok (fn [ctx]
        (let [ontology (get-ontology url)]
          (html5 {:xml? true} [:body [:div "OK then, " url]])
        ))))
)


(def handler
  (-> app
      (wrap-trace :header :ui)
      (wrap-params)))
