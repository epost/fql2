
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

(defn tool
  []
  (let [args ["-o" "/home/fred/.boot/cache/tmp/home/fred/projects/fql/src/catdata/aql/grammar/oc9/qphubc"
              "-lib" "/home/fred/.boot/cache/tmp/home/fred/projects/fql/src/catdata/aql/grammar/oc9/-ueasqk"
              "-no-listener" "-no-visitor"
              "/home/fred/.boot/cache/tmp/home/fred/projects/fql/src/catdata/aql/grammar/oc9/uanrg/AqlLexerRules.g4"]
        args' (into-array args)]
    (antlr/Tool. args')))

(deftask build
  [s show bool "show the arguments"]
  (comp
    (antlr4 :grammar "AqlLexerRules.g4" :show show)
    ;;(show)
    (antlr4 :grammar "AqlCommentTest.g4" :show show)
    ;;(show)
    (target :dir #{"target"})))
