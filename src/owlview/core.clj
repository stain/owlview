(ns owlview.core
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.util :refer [escape-html]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.stacktrace :refer [print-stack-trace]]
            [owlapi.core :refer [with-owl load-ontology loaded-ontologies classes
                                owl-manager with-owl-manager prefixes
                                prefix-for-iri
                                object-properties data-properties annotation-properties
                                ranges-of-property annotations]]
            [compojure.core :refer [defroutes ANY]]))

(defn xhtml? [ctx]
;  (= "application/xhtml+xml";
;     (print
;    (get-in ctx [:representation :media-type]))
    false)

; Map from uri to owl-managers
(defonce known-ontologies (atom {}))

(defn owl-manager-for [uri]
  (or (get @known-ontologies uri)
      (get (swap! known-ontologies assoc uri (owl-manager)) uri)))

(defn forget-owl-manager-for [uri]
  (swap! known-ontologies dissoc uri))

(defn get-ontology [uri]
  (load-ontology uri))

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

(def ^:dynamic *ontology* nil)

(defn name-for-iri [iri]
  (if-let [prefix-iri (prefix-for-iri iri *ontology*)]
    prefix-iri
    (str iri)))

(defn name-for-named [named]
  (name-for-iri (.getIRI named)))

(defn label-for-item [item]
  (escape-html (name-for-named item)))

(defn item-id [item]
  (escape-html (.getIRI item)))

(defn show-item [item]
  [:a {:href (str "#" (item-id item))} (label-for-item item)])
/
(defn sorted-items [items]
  (sort-by #(.getIRI %) items))

(defn list-items [items]
  [:ol (map (fn [item] [:li (show-item item)]) (sorted-items items))])

(defn expand-items [items]
  [:div (map (fn [item]
    [:h3 {:id (item-id item)} (label-for-item item)])
    (sorted-items items))])


(defroutes app
  (ANY "/" [] (resource
    :available-media-types ["text/html" "application/xhtml+xml"]
                        :handle-ok (fn [ctx] (html ctx "owlview"
                              [:div {:class "jumbotron"}
                                  "Visualize an OWL/RDFS ontology:"
                                  [:form {:role "form" :method "POST" :action "ont" :enctype "multipart/form-data"}
                                     [:p [:input {:name "url" :type "url" :class "form-control" :placeholder "http://purl.org/pav/" :autofocus :autofocus}]]
                                     "or:"
                                     [:p [:input {:name "file" :type "file" :class "form-control" :multiple "multiple"
                                                    :accept "application/rdf+xml,text/turtle,application/owl+xml,.owl,.rdf,.ttl,.owx"}]]
                                     [:p [:input {:type "submit" :class "btn btn-primary btn-lg" :value "Visualize"}]]
                                  ]
                              [:p "Alternatively, view any of the " [:a {:href "ont"} "known ontologies" ] "."]
                              ]
                          ))))
  (ANY "/ont" [] (resource
      :available-media-types ["text/html" "application/xhtml+xml"]
      :allowed-methods [:post :get]
      :handle-exception (fn [{err :exception :as ctx}]
        (print-stack-trace err)
        (html ctx (str "Failed to load ontology") [:pre (escape-html (or (.getMessage err) (str err)))])
      )
      :handle-ok (fn [ctx] (html ctx "owlview: Known ontologies"
        [:div {:class :jumbotron}
          [:p "Namespaces:"]
          [:ul (map
                #(vector :li [:a {:href (str "ont/" (escape-html %))}  (escape-html %)])
                (filter #(.contains % ":") (sort (keys @known-ontologies))))]
          [:p "Alternatively, try to " [:a {:href "."} "visualize another ontology" ] "."]]
      ))
      :post! (fn [{ {multipart :multipart-params
                    params :params
                    :as request}
                    :request :as ctx}]
                (println multipart)
                (println params)
                (if-let [url (params "url")])
                  { :location (format "/ont/%s" url) }
                  ;; tODO: Check for empty string
                (if-let [file (params "file")]
                ;; TODO: Check for empty file
                  { :location (format "/ont/%s" 127812) }
                )

      ;;
;   {:request {:ssl-client-cert nil, :remote-addr 127.0.0.1, :params {file pav.owl, url }, :route-params {},
;     :headers {host localhost:3000, user-agent Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0, content-type application/x-www-form-urlencoded, content-length 17,
;       referer http://localhost:3000/, connection keep-alive, accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8, accept-language en-gb,en;q=0.5, accept-encoding gzip, deflate, cache-control max-age=0},
;       :server-port 3000, :content-length 17, :form-params {file pav.owl, url }, :query-params {}, :content-type application/x-www-form-urlencoded, :character-encoding nil,
;          :uri /ont, :server-name localhost, :query-string nil,
;         :body #<HttpInput org.eclipse.jetty.server.HttpInput@469c3554>, :scheme :http,
;
;          :request-method :post},
;          :resource {:existed? #<core$constantly$fn__4085 clojure.core$constantly$fn__4085@22a41ee1>, :conflict? #<core$constantly$fn__4085 clojure.core$constantly$fn__4085@44e7578b>,
;           ;; ;;;...
;              :representation {:encoding identity, :media-type application/xhtml+xml}}
      ;;

      ;:post-redirect? (fn [ctx] {:location (format "/ont/%s" "http://purl.org/pav/")})
    ))
  (ANY "/ont/*" [& {url :* }] (resource
    :available-media-types ["text/html" "application/xhtml+xml"]
    :handle-exception (fn [{err :exception :as ctx}]
      (print-stack-trace err)
      (forget-owl-manager-for url) ; force reload and unlisting
      (html ctx (str "Failed to load ontology " url) [:pre (escape-html (or (.getMessage err) (str err)))])
    )
    :handle-ok (fn [ctx]
        (with-owl-manager (owl-manager-for url)
          (binding [*ontology* (get-ontology url)]
            (html ctx (str "Ontology " (escape-html url))
                       ;[:div "Ontology: " (escape-html *ontology*)]
                        [:div [:h2 "Content"] [:ol
                                            [:li [:a {:href "#Classes"} "Classes"]
                                              (list-items (classes *ontology*))]
                                            [:li [:a {:href "#ObjectProperties"} "Object properties"]
                                              (list-items (object-properties *ontology*))]
                                            [:li [:a {:href "#DataProperties"} "Data properties"]
                                              (list-items (data-properties *ontology*))]
                                              ;; TODO: Annotation properties, named individuals, etc.
                        ]]
                        [:div {:id "Classes"} [:h2 "Classes"]
                           (expand-items (classes *ontology*))
                        ]
                        [:div {:id "ObjectProperties"} [:h2 "Object properties"]
                           (expand-items (object-properties *ontology*))
                        ]
                        [:div {:id "DataProperties"} [:h2 "Data properties"]
                           (expand-items (data-properties *ontology*))
                        ]

                        ))))))
)


(def handler
  (-> app
      (wrap-params)
      (wrap-multipart-params)
      ))
