
(def project 'babeloff/aql)
(def version "0.1.0-SNAPSHOT")

(set-env!
    :source-paths #{"src/antlr4"}
    :dependencies '[[org.clojure/clojure "1.9.0-beta1"]
                    [boot/core "RELEASE" :scope "test"]
                    [babeloff/boot-antlr4 "0.1.0-SNAPSHOT"]
                    [org.antlr/antlr4 "4.7"]])

(task-options!
 pom {:project     project
      :version     version
      :packaging   "jar"
      :description "help in generating the java based parser for AQL"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/CategoricalData/fql"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}
      :developers  {"Fred Eisele" ""}})


;; (import '(org.antlr.v4.gui TestRig))
(require '[babeloff.boot-antlr4 :as antlr :refer [antlr4]])
(import '(org.antlr.v4 Tool))


(deftask build
  [s show bool "show the arguments"]
  (comp
    (watch)
    (antlr4 :grammar "AqlLexerRules.g4" :show show)
    (antlr4 :grammar "Aql.g4" :show show)
    (target :dir #{"target"})))
