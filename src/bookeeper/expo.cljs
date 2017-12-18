(ns bookeeper.expo
  (:require-macros [bookeeper.utils :refer [def-components def-apis]])
  (:require
    [reagent.core :refer [adapt-react-class]]
    [oops.core :refer [oget]]
    ))

(def expo
  (js/require "expo"))

(def-apis expo
  SQLite
  ImagePicker
  Svg
  )

(def SVG
  (adapt-react-class Svg))

(def-components Svg
  Polygon
  Rect
  )

(def-components (js/require "@expo/vector-icons")
  FontAwesome
  Ionicons
  Entypo
  )
