(defproject owlview "0.1.0-SNAPSHOT"
  :description "OWL viewer"
  :url "https://github.com/stain/owlview"
  :scm {:connection "scm:git:https://github.com/stain/owlview.git"
        :url "https://github.com/stain/owlview"}
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler owlview.core/handler}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [liberator "0.12.0"]
                 [clj-owlapi "0.3.0"]
                 [hiccup "1.0.5"]
  ])
