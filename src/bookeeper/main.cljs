(ns bookeeper.main
  (:require
    [goog.string :as gstring]
    [goog.string.format]
    [reagent.core :as r]
    [bookeeper.react-native :as rn]
    [bookeeper.expo :as expo]
    [bookeeper.button :refer [Button]]
    [bookeeper.text-input :refer [TextInput]]
    ))

(def google-api-key
  "AIzaSyBH8JzTm3enVJOqGDlx351cl8bxdNX0ce8")

(defonce panel      (r/atom :splash))
(defonce loading    (r/atom false))
(defonce entries    (r/atom []))
(defonce photo      (r/atom nil))
(defonce vendor     (r/atom ""))
(defonce cost       (r/atom ""))
(defonce words      (r/atom []))
(defonce editing    (r/atom :vendor))
(defonce total-cost (r/atom 0))

(defn init-add []
  (reset! words [])
  (reset! vendor "")
  (reset! cost "")
  (reset! editing :vendor))

(def db
  (-> expo/SQLite (.openDatabase "bookeeper")))

(defn q [sql args]
  (js/Promise.
    (fn [success failure]
      (-> db (.transaction
               (fn [tx]
                 (-> tx (.executeSql
                          sql (clj->js (or args []))
                          (fn [tx res] (success res))
                          (fn [tx err] (failure err))
                          ))))))))

(defn init-list []
  (-> js/Promise (.resolve true)
      (.then #(q "SELECT * FROM entry ORDER BY added DESC" nil))
      (.then (fn [result]
               (let [sql-rows   (-> result .-rows)
                     row-count  (-> sql-rows .-length)
                     rows       (->> (for [i (range row-count)]
                                       (-> sql-rows .-_array
                                           (aget i)
                                           (js->clj :keywordize-keys true)))
                                     (into []))]
                 ;(println rows)
                 (reset! entries    rows)
                 (reset! total-cost (->> rows (map :cost) (reduce + 0)))
                 (reset! panel      :list))))))

(defn init []
  (doto rn/StatusBar
    (.setBarStyle "light-content"))
  (-> (q (str ;"DROP TABLE IF EXISTS entry;"
              "CREATE TABLE IF NOT EXISTS entry ("
              "  id INTEGER PRIMARY KEY NOT NULL,"
              "  added DATETIME DEFAULT CURRENT_TIMESTAMP,"
              "  vendor TEXT, cost INTEGER, photo TEXT"
              ");") nil)
      (.then init-list)))

(defn perform-ocr [base64-img]
  (let [payload {:requests [{:image {:content base64-img}
                             :features [{:type "TEXT_DETECTION"}]}]}
        headers (doto (new js/Headers)
                  (.append "Content-Type" "application/json"))
        body    (-> js/JSON (.stringify (clj->js payload)))]
    (-> (js/fetch (str "https://vision.googleapis.com/v1/images:annotate?key="
                       google-api-key)
                #js{:method   "POST"
                    :headers  headers
                    :body     body})
        (.then (fn [resp] (-> resp .json)))
        (.then (fn [result]
                 (let [ocr-words (-> (js->clj result :keywordize-keys true)
                                     (get-in [:responses 0 :textAnnotations])
                                     (rest) ;; only keep individual words
                                     )]
                   (reset! loading false)
                   (reset! words ocr-words)
                   ;(cljs.pprint/pprint ocr-words)
                   ))))))

(defn take-photo []
  (reset! panel :splash)
  (reset! loading true)
  (-> expo/ImagePicker
      (.launchCameraAsync #js{:quality 0.7
                              :base64 true})
      (.then (fn [res]
               (let [{:keys [cancelled] :as result}
                     (js->clj res :keywordize-keys true)]
                 (if-not cancelled
                   (do
                     (init-add)
                     (reset! photo (select-keys result [:uri :width :height]))
                     (reset! panel :add)
                     (perform-ocr (:base64 result)))
                   (reset! panel :list)))))))

(defn add-entry []
  (reset! loading true)
  (-> (q "INSERT INTO entry (vendor, cost, photo) VALUES (?, ?, ?)"
         [@vendor (* 100 (js/parseFloat @cost)) (:uri @photo)])
      (.then (fn [_]
               (reset! panel :list)
               (reset! loading false)
               (init-list)))))

(defn clear-all []
  (-> rn/Alert
      (.alert
        "Clear Entries"
        "Are you sure you want to delete all entries?"
        (clj->js [{:text "Cancel"}
                  {:text "Clear All"
                   :style "destructive"
                   :onPress
                   (fn []
                     (reset! loading true)
                     (-> (q "DELETE FROM entry" [])
                         (.then #(init-list))))}]))))

;; ===============================================================
;; Views =========================================================

(defn LoadingOverlay []
  (when @loading
    [rn/View {:style {:flex             1
                      :alignItems       :center
                      :justifyContent   :center
                      :position         :absolute
                      :right            0
                      :left             0
                      :top              0
                      :bottom           0
                      :backgroundColor  "#0000008F"
                      }}
     [rn/ActivityIndicator {:size     :large
                            :color    "#C7C7C7"
                            :style    {:transform [{:scale 2}]}}]
     ]))

(defn EntryList []
  [rn/View {:style {:flex             1
                    :alignItems       :stretch
                    :justifyContent   :center
                    :backgroundColor  "#1C2936"}}
   [rn/View {:style {:flex              1
                     :flexDirection     :row
                     :alignItems        :center
                     :justifyContent    :space-between
                     :backgroundColor   "#131D24"
                     :padding           20
                     :paddingTop        40
                     :paddingHorizontal 0
                     :flexGrow          1}}
    [rn/View {:style {:width 50}}]
    [rn/Text {:style {:textAlign          :center
                      :paddingHorizontal  10
                      :fontWeight         :bold
                      :fontSize           25
                      :color              "#FFFFFF"
                      :margin             5}}
     "bookeeper"]
    [rn/TouchableOpacity {:onPress #(take-photo)
                          :style {:padding 10}}
     [expo/Entypo {:name  :add-to-list
                   :size  30
                   :color "#FFF"}]]]
   [rn/View {:style {:flex              1
                     :flexDirection     :row
                     :alignItems        :center
                     :justifyContent    :space-between
                     :backgroundColor   "#131D24"
                     :padding           10
                     :borderTopWidth    1
                     :borderTopColor    "#888888"
                     :flexGrow          1}}
    [rn/Text {:style {:color "#FFF"}
              :onPress #(clear-all)}
     "Clear All"]
    [rn/Text {:style {:color "#FFF"}}
     (str "Total: $" (gstring/format "%.2f" (/ @total-cost 100)))]]
   [rn/ScrollView {:style {:flexGrow  50
                           :padding   10}}
    (for [{:keys [id vendor cost photo added]} @entries]
      [rn/View {:key id
                :style {:flex 0
                        :flexDirection :row
                        :margin-bottom 10}}
       [rn/Image {:source {:uri photo}
                  :resizeMode :cover
                  :style {:height 80 :width 80}}]
       [rn/View {:style {:margin-left 10}}
        [rn/Text {:style {:color "#FFF"
                          :fontSize 16
                          :fontWeight :bold}}
         vendor]
        [rn/Text {:style {:color "#FFF"}}
         added]
        [rn/Text {:style {:color "#FFF"}}
         (str "$" (gstring/format "%.2f" (/ cost 100)))]]])]])

(defn EntryAdd []
  [rn/View {:style {:flex             1
                    :alignItems       :stretch
                    :justifyContent   :center
                    :backgroundColor  "#1C2936"}}
   [rn/View {:style {:flex              1
                     :flexDirection     :row
                     :alignItems        :center
                     :justifyContent    :space-between
                     :backgroundColor   "#131D24"
                     :padding           20
                     :paddingTop        40
                     :paddingHorizontal 0
                     :flexGrow          1}}
    [rn/TouchableOpacity {:onPress #(reset! panel :list)
                          :style {:padding 10}}
     [expo/Ionicons {:name  :ios-arrow-back
                     :size  30
                     :color "#FFF"}]]
    [rn/Text {:style {:textAlign          :center
                      :paddingHorizontal  10
                      :fontWeight         :bold
                      :fontSize           25
                      :color              "#FFFFFF"
                      :margin             5}}
     "New Item"]
    [rn/TouchableOpacity {:onPress #(add-entry)
                          :style {:padding 10}}
     [expo/Ionicons {:name  :md-checkmark
                     :size  30
                     :color "#FFF"}]]]
   [rn/View {:style {:flexGrow        50
                     :justifyContent  :center}}
    (when-not (nil? @photo)
      [rn/Image {:source #js{:uri (:uri @photo)}
                 :resizeMode :contain
                 :style {:position :absolute
                         :top 0 :bottom 0
                         :left 0 :right 0}}])
    (when-not (or (empty? @words) (nil? @photo))
      [expo/SVG {:viewBox (str "0 0 " (:width @photo) " " (:height @photo))
                 :style {:position :absolute
                         :top 0 :bottom 0
                         :left 0 :right 0}}
       [expo/Rect {:x 0 :y 0
                   :width (:width @photo)
                   :height (:height @photo)
                   :fillOpacity 0
                   :onPress #(-> rn/Keyboard .dismiss)}]
       (for [{:keys [description]
              {:keys [vertices]} :boundingPoly} @words]
         [expo/Polygon {:key (hash vertices)
                        :onPress
                        (fn [_]
                          (println description)
                          (case @editing
                            :vendor (swap! vendor
                                           #(if (empty? %)
                                              description
                                              (str % " " description)))
                            :cost   (swap! cost
                                           #(if (empty? %)
                                              description
                                              (str % "." description)))))
                        :points       (->> (for [{:keys [x y]} vertices]
                                             (str x "," y " "))
                                           (apply str))
                        :fillOpacity  0
                        :stroke       :lime
                        :strokeWidth  10}])])
    [rn/View {:style {:position :absolute
                      :top 0 :left 0 :right 0
                      :backgroundColor "#0000008F"
                      :padding-bottom 10}}
     [TextInput {:value @vendor
                 :onChangeText #(reset! vendor %)
                 :placeholder "Vendor"
                 :onFocus #(reset! editing :vendor)
                 :force-active (= @editing :vendor)
                 :style {:padding 5}}]
     [TextInput {:value @cost
                 :onChangeText #(reset! cost %)
                 :placeholder "Cost"
                 :keyboardType :numeric
                 :onFocus #(reset! editing :cost)
                 :force-active (= @editing :cost)
                 :style {:padding 5}}]]]
   [LoadingOverlay]])

(defn Splash []
  [rn/View {:style {:flex 1
                    :alignItems :center
                    :justifyContent :center
                    :backgroundColor "#333"}}
   [rn/Image {:style {:width 200 :height 200}
              :source (js/require "bookeeper/assets/icons/loading.png")}]
   [LoadingOverlay]])

(defn app []
  (fn []
    (case @panel
      :splash [Splash]
      :list   [EntryList]
      :add    [EntryAdd])))

