
(def project 'babeloff/aql)
(def version "0.1.0-SNAPSHOT")

(set-env!
    :source-paths #{"src/antlr4"}
    :dependencies '[[org.clojure/clojure "1.9.0-beta1"]
                    [boot/core "RELEASE" :scope "test"]
                    [babeloff/boot-antlr4 "0.1.0"]
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
(require '[babeloff.boot-antlr4 :as antlr :refer [antlr4 test-rig]])
(import '(org.antlr.v4 Tool))


(deftask build
  [s show bool "show the arguments"]
  (comp
    (watch)
    (antlr4 :grammar "AqlLexerRules.g4" :show true)
    (antlr4 :grammar "Aql.g4" :show true)
    (javac)
    (test-rig :parser "AqlParser"
              :lexer "AqlLexerRules"
              :start-rule "program"
              :tree true)
    (target :dir #{"target"})))

(deftask rerepl
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

(deftask store
  [s show bool "show the arguments"]
  (comp
    (target :dir #{"target"})))

(deftask build
  [s show bool "show the arguments"]
  (comp
    (antlr4 :grammar "ANTLRv4Lexer.g4"
            :package "org.antlr.parser.antlr4"
            :show true)
    (antlr4 :grammar "ANTLRv4Parser.g4"
            :package "org.antlr.parser.antlr4"
            :show true)
    (javac)))

(deftask exercise
  [s show bool "show the arguments"]
  (comp 
    (test-rig :parser "org.antlr.parser.antlr4.ANTLRv4Parser"
              :lexer "org.antlr.parser.antlr4.ANTLRv4Lexer"
              :start-rule "grammarSpec"
              :input ["src/antlr4/ANTLRv4Lexer.g4"
                      "src/antlr4/ANTLRv4Parser.g4"]
              :tree true
              :postscript true
              :tokens true
              :show true)))

(deftask my-repl
  []
  (comp (repl) (build) (store)))

(deftask live 
  []
  (comp ;(watch) 
    (build)
    (exercise) 
    ;; (show :fileset true) 
    (store)))
 