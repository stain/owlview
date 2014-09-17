# owlview

OWL Viewer

Present a simple listing of the classes and properties an OWL ontology, specification-style.

Inspired by [LODE](http://www.essepuntato.it/lode), the goals of owlview are:
 * Simple (X)HTML 5 results that can be saved and included elsewhere
   * TODO: Indentation?
 * Predictable and [Cool URIs](http://www.w3.org/TR/cooluris/)

The project is still at an early, experimental stage.

## TODO

 * Menu overlay of Table of Content
 * Better navigation of large ontologies like [schema.org](http://owl.s11.no/view/ont/http://topbraid.org/schema/schema.ttl)
 * Alternative view with page per property
 * Nicer rendering of annotations
   * Markdown (optional)
 * Ontology metadata
 * Options - read from OWL?
 * Content-negotation and download button for different formats (RDF/XML, Turtle, JSON-LD, etc)
 
## Live example

There should be an instance of owlview running at http://owl.s11.no/view/ - contact [@soilandreyes](http://twitter.com/soilandreyes) or create a 
[Github issue](https://github.com/stain/owlview/issues) if you have any problems.

Examples:
 * http://owl.s11.no/view/ont/http://purl.org/pav/
 * http://owl.s11.no/view/ont/http://purl.org/dc/terms/#http://purl.org/dc/terms/format
 * http://owl.s11.no/view/ont/http://www.w3.org/ns/prov-o#


## Usage

Requires [Clojure](http://clojure.org/) and [Leiningen](http://leiningen.org/): 

    lein ring server

## License

Copyright © 2014 [Stian Soiland-Reyes](http://orcid.org/0000-0001-9842-9718)

Distributed under the [Eclipse Public License](LICENSE) either version 1.0 or (at
your option) any later version.
