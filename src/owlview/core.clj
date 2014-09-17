(ns owlview.core
  (:import [java.util UUID])
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.util :refer [escape-html]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.set :refer [union]]
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
  (or (first (loaded-ontologies))
      (load-ontology uri)))

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

(def example-ontology "http://purl.org/pav/")

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

(defn sorted-items [items]
  (sort-by #(.getIRI %) items))

(defn list-items [items]
  [:ol (map (fn [item] [:li (show-item item)]) (sorted-items items))])

(def annotation-uri {
  "http://www.w3.org/2000/01/rdf-schema#isDefinedBy" :isDefinedBy
  "http://www.w3.org/2002/07/owl#versionInfo"    :versionInfo
  "http://purl.org/dc/terms/date"                :date
  "http://purl.org/dc/elements/1.1/date"         :date
  "http://www.w3.org/2000/01/rdf-schema#label"   :label
  "http://purl.org/dc/terms/title"               :title
  "http://purl.org/dc/elements/1.1/title"        :title
  "http://www.w3.org/2000/01/rdf-schema#comment" :comment
  "http://purl.org/dc/terms/description"         :description
  "http://purl.org/dc/elements/1.1/description"  :description
  "http://purl.org/dc/elements/1.1/creator"      :creator
  "http://purl.org/dc/terms/creator"             :creator
  "http://purl.org/dc/elements/1.1/contributor"  :contributor
  "http://purl.org/dc/terms/contributor"         :contributor
  "http://purl.org/dc/terms/rights"              :rights
  "http://www.w3.org/2000/01/rdf-schema#seeAlso" :seeAlso
  })


(defn annotation-map [item]
  (merge-with union
   (map (fn [ann] (hash-map (annotation-uri
                     (.. ann (getProperty) (getIRI))
                     (name-for-iri (.getProperty ann)))
                   (sorted-set (.getValue ann))))
        (annotations item))))

(defn annotation [ann]
  (list [:strong (name-for-iri (.. ann (getProperty) (getIRI)))]
    " " [:span (escape-html (.getValue ann))]))

(defn annotations-for [item]
  (map annotation (annotations item)))

(defn expand-item [item]
  ;(doall (map annotation-map item))
  [:div
    [:h3 {:id (item-id item)} (label-for-item item)]
    [:dl {:class :dl-horizontal}
      [:dt "URI"] [:dd (escape-html (.getIRI item))]
      (let [annotations (annotations-for item)]
        (if (not (empty? annotations))  (list
          [:dt "Annotations"]
          (map #(vector :dd %) annotations) )))
;      (doall (map (fn [[k,v]] [[:dt (escape-html "x")] [:dd (escape-html "f")]])
;        (map annotation-map item)))
    ]
  ])

(defn wrap-map [s]
  (if (map? s) [s] s))

(defn expand-items [items]
  [:div (map expand-item (sorted-items items))])

(defroutes app
  (ANY "/" [] (resource
    :available-media-types ["text/html" "application/xhtml+xml"]
                        :handle-ok (fn [ctx] (html ctx "owlview"
                              [:div {:class "jumbotron"}
                                  "Visualize an OWL/RDFS ontology:"
                                  [:form {:role "form" :method "POST" :action "ont" :enctype "multipart/form-data"}
                                     [:p [:input {:name "url" :type "url" :class "form-control" :placeholder example-ontology :autofocus :autofocus}]]
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
        (html ctx "Failed to load ontology" [:pre (escape-html (or (.getMessage err) (str err)))])
      )
      :handle-ok (fn [ctx] (html ctx "owlview: Known ontologies"
        [:div {:class :jumbotron}
          [:p "Namespaces:"]
          [:ul (map
                #(vector :li [:a {:href (str "ont/" (escape-html %))}  (escape-html %)])
                (filter #(.contains % ":") (sort (keys @known-ontologies))))]
          [:p "Alternatively, try to " [:a {:href "."} "visualize another ontology" ] "."]]
      ))
      :handle-created (fn [ctx]
          (println ctx)
          (let [uri (ctx :location)]
                (html ctx "Loaded ontology"
                                [:div {:class :jumbotron}
                                  "Loaded ontology: "
                                  [:a {:href uri} uri]
                                ]
                                [:script {:type "text/javascript"} (format "document.location='%s';" uri)]
                                )))
      :post! (fn [{ {multipart :multipart-params
                    params :params
                    :as request}
                    :request :as ctx}]
                (println params)
                (let [url (params "url")
                      files (params "file")]
                      (println files)
                      (println (first files))
                    (if (and url (not (.isEmpty url)))
                      { :location (format "ont/%s" url) }
                      (if (= 0 (get files :size)) ; no file uploaded - use example
                        {:location (format "ont/%s" example-ontology)}
                        (let [uuid (str (UUID/randomUUID))]
                          (println uuid)
                          (with-owl-manager (owl-manager-for uuid)
                            (doall (map
                              #(load-ontology (get % :tempfile))
                              (wrap-map files))))
                            { :location (format "ont/%s" uuid)}
                        )))))
  ))

  (ANY "/ont/*" [& {url :* }] (resource
    :available-media-types ["text/html" "application/xhtml+xml"]
    :handle-exception (fn [{err :exception :as ctx}]
      (print-stack-trace err)
      (forget-owl-manager-for url) ; force reload and unlisting
      (html ctx (str "Failed to ontology " url) [:pre (escape-html (or (.getMessage err) (str err)))])
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

                        )))))
  )
)


(def handler
  (-> app
      (wrap-params)
      (wrap-multipart-params)
  ))
