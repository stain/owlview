# owlview

OWL Viewer

Present a simple listing of the classes and properties an OWL ontology, specification-style.

Inspired by [LODE](http://www.essepuntato.it/lode), the goals of owlview are:
 * Simple HTML results that can be saved and included elsewhere
 * Predictable and pretty [Cool URIs](http://www.w3.org/TR/cooluris/)

## Example

There should be an instance of owlview running at http://owl.s11.no/view/

Examples:
 * http://owl.s11.no/view/http://purl.org/pav/
 * http://owl.s11.no/view/http://purl.org/dc/terms/
 * http://owl.s11.no/view/http://www.w3.org/ns/prov-o#


## Usage

Requires [Clojure](http://clojure.org/) and [Leiningen](http://leiningen.org/): 

    lein ring server

## License

Copyright © 2014 Stian Soiland-Reyes

Distributed under the [Eclipse Public License](LICENSE) either version 1.0 or (at
your option) any later version.
