(ns bookeeper.react-native
  (:require-macros [bookeeper.utils :refer [def-components def-apis]])
  (:require
    [reagent.core :refer [adapt-react-class]]
    [oops.core :refer [oget]]
    ))

(def react-native
  (js/require "react-native"))

(def-apis react-native
  AppRegistry
  StatusBar
  AsyncStorage
  Keyboard
  Alert
  )

(def-components react-native
  Text
  View
  Image
  TouchableOpacity
  ActivityIndicator
  TextInput
  ScrollView
  )

