(ns temci.errors
  (:require [reagent.core :as reagent]))

(defn errors-list [err]
  [:div.alert-danger {:style {:text-align "center"
                              :padding-top "15px"}}
   [:p err]])