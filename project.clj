(defproject bookeeper "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.12"]]
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.854"]
                 [reagent "0.7.0" :exclusions [cljsjs/react
                                               cljsjs/react-dom
                                               cljsjs/react-dom-server
                                               cljsjs/create-react-class]]
                 [re-frame "0.9.3"]
                 [day8.re-frame/http-fx "0.1.4"]
                 [react-native-externs "0.1.0"]
                 [binaryage/oops "0.5.6"]
                 ]
  :clean-targets ["target/" "main.js"]
  :aliases {"figwheel" ["run" "-m" "user" "--figwheel"]
            "externs" ["do" "clean"
                       ["run" "-m" "externs"]]
            "rebuild-modules" ["run" "-m" "user" "--rebuild-modules"]
            "prod-build" ^{:doc "Recompile code with prod profile."}
            ["externs"
             ["with-profile" "prod" "cljsbuild" "once" "main"]]}
  :profiles {:dev {:dependencies  [[figwheel-sidecar "0.5.12"]
                                   [com.cemerick/piggieback "0.2.2"]
                                   [re-frisk-sidecar "0.5.1"]
                                   [re-frisk-remote "0.5.1"]]
                   :plugins       [[lein-re-frisk "0.5.1"]]
                   :source-paths  ["src" "env/dev"]
                   :cljsbuild     {:builds [{:id "main"
                                             :source-paths ["src" "env/dev"]
                                             :figwheel     true
                                             :compiler     {:output-to     "target/not-used.js"
                                                            :main          "env.main"
                                                            :output-dir    "target"
                                                            :optimizations :none}}]}
                   :repl-options  {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
             :prod {:cljsbuild    {:builds [{:id "main"
                                             :source-paths ["src" "env/prod"]
                                             :compiler     {:output-to     "main.js"
                                                            :main          "env.main"
                                                            :output-dir    "target"
                                                            :static-fns    true
                                                            :externs       ["js/externs.js"]
                                                            :parallel-build     true
                                                            :optimize-constants true
                                                            :optimizations :advanced
                                                            :closure-defines {"goog.DEBUG" false}}}]}}})
