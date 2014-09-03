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
                 [liberator "0.12.1"]
                 [compojure "1.1.8"]
                 [ring "1.3.1"]
                 [clj-owlapi "0.3.1-SNAPSHOT"]
                 [hiccup "1.0.5"]
                 [org.slf4j/slf4j-jdk14 "1.7.7"]
  ])
