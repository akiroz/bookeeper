(ns ^:figwheel-no-load env.main
  (:require
    [reagent.core :as r]
    [re-frame.core :refer [clear-subscription-cache!]]
    [figwheel.client :as figwheel :include-macros true]
    [re-frisk-remote.core :refer [enable-re-frisk-remote!]]
    [bookeeper.core :as core]
    [env.dev]
    ))

(enable-console-print!)
(set! (-> js/console .-disableYellowBox) true)

(def cnt (r/atom 0))
(defn reloader []
  @cnt
  (clear-subscription-cache!)
  [core/app-root])
(def root-el (r/as-element [reloader]))

(figwheel/watch-and-reload
 :websocket-url (str "ws://" env.dev/ip ":3449/figwheel-ws")
 :heads-up-display false
 :jsload-callback #(swap! cnt inc))

(enable-re-frisk-remote! {:host (str env.dev/ip ":4567")})
(core/init)
