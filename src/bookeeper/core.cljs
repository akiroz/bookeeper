(ns bookeeper.core
    (:require
      [reagent.core :refer [reactify-component]]
      [bookeeper.react-native :refer [AppRegistry]]
      [bookeeper.main :as main]
      ))

(def app-root
  "Used for hot reload during development.
   required from env.main"
  main/app)

(defn init []
  (main/init)
  (doto AppRegistry
    (.registerComponent "main" #(reactify-component app-root))))
