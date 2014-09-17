# owlview

OWL Viewer

Present a simple listing of the classes and properties an OWL ontology, specification-style.

Inspired by [LODE](http://www.essepuntato.it/lode), the goals of owlview are:
 * Simple (X)HTML 5 results that can be saved and included elsewhere
     * Light-weight [Bootstrap 3](http://getbootstrap.com/) styling
 * Predictable and [Cool URIs](http://www.w3.org/TR/cooluris/) for ontology/term
 * Load from ontology URL or uploaded file(s)
 * Markdown support

The project is still at an early, experimental stage.

## TODO

 * Menu overlay of Table of Content
 * Better navigation of large ontologies like [schema.org](http://owl.s11.no/view/ont/http://topbraid.org/schema/schema.ttl)
 * Alternative view with page per property
 * Nicer rendering of annotations
 * Auto-detect Markdown vs HTML? Optional?
 * Options - read from OWL?
 * Content-negotation and download button for different formats (RDF/XML, Turtle, JSON-LD, etc)
 * Indentation of HTML source?
 * Option to not publically list an ontology loaded from URI 
 * "Follow your nose"-loading of undefined annotation properties and superclasses
 * Loading directly from an ontology term, e.g. http://owl.s11.no/view/term/http://purl.org/pav/createdOn

## Live example

There should be an instance of owlview running at http://owl.s11.no/view/ - contact [@soilandreyes](http://twitter.com/soilandreyes) or create a 
[Github issue](https://github.com/stain/owlview/issues) if you have any problems.

Examples:
 * http://owl.s11.no/view/ont/http://purl.org/pav/
 * http://owl.s11.no/view/ont/http://purl.org/dc/terms/#http://purl.org/dc/terms/format
 * http://owl.s11.no/view/ont/http://www.w3.org/ns/prov-o#


## Usage

Requires [Clojure](http://clojure.org/) and [Leiningen](http://leiningen.org/).

(For now) install SNAPSHOT of [clj-owlapi](https://github.com/stain/clj-owlapi):
    
    git clone https://github.com/stain/clj-owlapi.git
    cd clj-owlapi
    lein install
    cd ..

Then, for owlview:

    git clone https://github.com/stain/owlview.git
    cd owlview
    lein ring server

## License

Copyright © 2014 [Stian Soiland-Reyes](http://orcid.org/0000-0001-9842-9718)

Distributed under the [Eclipse Public License](LICENSE) either version 1.0 or (at
your option) any later version.
