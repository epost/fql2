
(def project 'babeloff/aql)
(def version "0.1.0-SNAPSHOT")

(set-env!
    :source-paths #{"src/antlr4" "resource/sample"}
    :dependencies '[[org.clojure/clojure "1.9.0-beta2"]
                    [boot/core "RELEASE" :scope "test"]
                    [babeloff/boot-antlr4 "2017.10.19"]
                    [org.antlr/antlr4 "4.7"]
                    [clj-jgit "0.8.10"]
                    [byte-streams "0.2.3"]
                    [me.raynes/fs "1.4.6"]])

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
(require '[babeloff.boot-antlr4 :as antlr :refer [antlr4 test-rig]])

(deftask store
  [s show bool "show the arguments"]
  (comp
    (target :dir #{"target"})))


(deftask build
  [s show bool "show the arguments"]
  (comp
    (antlr4 :grammar "AqlLexerRules.g4"
            :package "org.aql")
    (antlr4 :grammar "AqlParser.g4"
            :package "org.aql"
            :visitor true
            :listener true)
    (javac)))


(deftask run-tests
  []
  (test-rig :parser "org.aql.AqlParser"
                 :lexer "org.aql.AqlLexerRules"
                 :start-rule "file"
                 :input ["resource/sample/cp2_1_db.aql"
                         "../../../../resources/examples/aql/All_Syntax.aql"
                         "../../../../resources/examples/aql/Tutorial TSP.aql"]
                 :tree true
                 :postscript false
                 :tokens true))

(deftask make-rdf
  []
  ())

(deftask my-repl
  [s show bool "show the arguments"]
  (comp (repl) (build)))

(defn poll
  "https://adzerk.com/blog/2017/02/faster-clojure-metadevelopment-with-boot/"
  [task]
  (let [f (java.io.File. "build.boot")]
    (loop [mtime (.lastModified f)]
      (let [new-mtime (.lastModified f)]
        (when (> new-mtime mtime)
          (load-file "build.boot")
          (boot (task)))
        (Thread/sleep 1000)
        (recur new-mtime)))))

(require '[babeloff.boot-antlr4 :as antlr :refer [antlr4 test-rig]]
         '(boot [core :as boot :refer [deftask]]
                [util :as util]
                [task-helpers :as helper]))


(deftask live
  []
  (comp
    (watch)
    (build)
    (run-tests)
    ;; (show :fileset true)
    (store)))
