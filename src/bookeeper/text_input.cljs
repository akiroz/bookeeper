(ns bookeeper.text-input
  (:require
    [reagent.core :as r]
    [bookeeper.react-native :as rn]
    ))

(defn TextInput [props]
  (let [active? (r/atom false)]
    (fn [props]
      [rn/View {:style (merge {:padding 10
                               :margin  5
                               :borderBottomWidth 1
                               :borderBottomColor (if (or @active?
                                                          (:force-active props))
                                                    "#2196F3"
                                                    "#888888")}
                              (:style props))}
       [rn/TextInput (merge
                       ;; Style props =========================
                       {:style (merge {:color "#FFFFFF"}
                                      (select-keys (:style props) [:color]))
                        :placeholderTextColor  "#FFFFFF5F"
                        :underlineColorAndroid :transparent}
                       ;; External props ======================
                       (-> props
                           (dissoc :style)
                           (dissoc :force-active))
                       ;; Wrap focus handlers =================
                       {:onFocus (fn [& args]
                                   (reset! active? true)
                                   (when (:onFocus props)
                                     (apply (:onFocus props) args)))
                        :onBlur  (fn [& args]
                                   (reset! active? false)
                                   (when (:onBlur props)
                                     (apply (:onBlur props) args)))
                        })]])))
