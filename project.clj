(defproject numbers-cljs "0.1.0-SNAPSHOT"
  :description "Play with numbers via ClojureScript and Angular.js"
  :url "" ;https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-18.md
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :pom-addition [:developers [:developer
                              [:id "magomimmo"]
                              [:name "Mimmo Cosenza"]
                              [:url "https://github.com/magomimmo"]
                              [:email "mimmo.cosenza@gmail.com"]
                              [:timezone "+2"]]]

  :min-lein-version "2.2.0"

  ;; clojure source code path
  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2069"]
                 [compojure "1.1.6"]
                 ]

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :plugins [[lein-cljsbuild "1.0.0"]
            [lein-ring "0.8.8"] ]

  :hooks [leiningen.cljsbuild]

  ;; ring tasks configuration
  :ring {:handler numbers-cljs.core/app}

  ;; cljsbuild tasks configuration
  :cljsbuild {:builds
              {
               :dev
               { ;; clojurescript source code path
                :source-paths ["src/cljs"]

                ;; Google Closure Compiler options
                :compiler { ;; the name of emitted JS script file
                           :output-to "resources/public/js/main.js"
                           ;; minimum optimization
                           :optimizations :whitespace
                           ;; prettyfying emitted JS
                           :pretty-print true}}
               }}
   )
