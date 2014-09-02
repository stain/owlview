(ns owlview.core
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.util :refer [escape-html]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.stacktrace :refer [print-stack-trace]]
            [owlapi.core :refer [with-owl load-ontology loaded-ontologies classes
                                owl-manager with-owl-manager
                                object-properties data-properties annotation-properties
                                ranges-of-property annotations]]
            [compojure.core :refer [defroutes ANY]]))

(defn xhtml? [ctx]
;  (= "application/xhtml+xml";
;     (print
;    (get-in ctx [:representation :media-type]))
    false)

; Map from uri to owl-managers
(def known-ontologies (atom {}))

(defn get-ontology [uri]
  (let [owl-manager (or (get @known-ontologies uri)
                        (get (swap! known-ontologies assoc uri (owl-manager)) uri))]
    (with-owl-manager owl-manager
      (load-ontology uri))))

(defn html [ctx title & body]
              (html5 {:xml? (xhtml? ctx)}
                  [:head
                    [:title (escape-html title)]
                    (include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.css"
                      "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap-theme.css"
                      )]
                  [:body
                   [:div {:class "container"}
                    [:h1 (escape-html title)]
                    body
                    [:address {:class "footer"}
                      [:a {:href "https://github.com/stain/owlview"} "owlview"]
                      " by "
                      [:a {:href "http://orcid.org/0000-0001-9842-9718"}
                      "Stian Soiland-Reyes"]]
                    (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.js"
                                "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.js")
                  ]]))

(defroutes app
  (ANY "/" [] (resource
    :available-media-types ["text/html" "application/xhtml+xml"]
                        :handle-ok (fn [ctx] (html ctx "owlview"
                              [:div {:class "jumbotron"}
                                  "Visualize an OWL/RDFS ontology:"
                                  [:form {:role "form" :method "POST" :action "ont"}
                                     [:p [:input {:name "url" :type "url" :class "form-control" :placeholder "http://purl.org/pav/" :autofocus :autofocus}]]
                                     "or:"
                                     [:p [:input {:name "file" :type "file" :class "form-control"
                                                    :accept "application/rdf+xml,text/turtle,application/owl+xml,.owl,.rdf,.ttl,.owx"}]]
                                     [:p [:input {:type "submit" :class "btn btn-primary btn-lg" :value "Visualize"}]]
                                  ]
                              ]
                          ))))
  (ANY "/ont" [] (resource
      :available-media-types ["text/html" "application/xhtml+xml"]
      :allowed-methods [:post :get]
      :handle-ok (fn [ctx] (html ctx "owlview: Known ontologies" ["Known:" (keys @known-ontologies) "OK?"]))
      :post! (fn [ctx] (str "OK. " ctx))
      :post-redirect? (fn [ctx] {:location (format "/ont/%s" "http://purl.org/pav/")})
    ))
  (ANY "/ont/*" [& {url :*}] (resource
    :available-media-types ["text/html" "application/xhtml+xml"]
    :handle-exception (fn [{err :exception :as ctx}]
      (print-stack-trace err)
      (html ctx (str "Failed to load ontology " url) [:pre (escape-html (or (.getMessage err) (str err)))])
    )
    :handle-ok (fn [ctx]
        (with-owl
          (let [ontology (get-ontology url)]
            (html ctx (str "Ontology " (escape-html url)) [:div "OK that is, " (str ontology)]
                                            [:ul
                                              (map #([:li str (escape-html %)]) (classes ontology))])
        )))))
)


(def handler
  (-> app
      (wrap-params)))
