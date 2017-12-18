(ns bookeeper.button
  (:require
    [bookeeper.react-native :as rn]))

(defn Button [{:keys [text          ;; display text
                      onPress       ;; press event handler
                      button-style  ;; button style override
                      text-style    ;; text style override
                      ]}]
  [rn/TouchableOpacity {:style  (merge {:backgroundColor   "#2196F3"
                                        :marginHorizontal  20
                                        :padding           10
                                        :paddingLeft       20
                                        :paddingRight      20
                                        :borderRadius      9
                                        :alignItems        :center
                                        }
                                       button-style)
                        :onPress onPress
                        }
   [rn/Text {:style (merge {:color      "#FFF"
                            :fontSize   20
                            :fontWeight :bold
                            }
                           text-style)}
    text]])
