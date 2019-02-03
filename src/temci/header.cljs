(ns temci.header
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [temci.routes :refer [url-for]]))

(defn header []
  (let [w3        @(re-frame/subscribe [:db/web3])
        active-page @(re-frame/subscribe [:views/active-page])]
    [:header
     [:nav.navbar.navbar-expand-md.navbar-light.border-bottom.box-shadow.header
      {:class "d-flex flex-column flex-md-row align-items-center p-3 px-md-4 mb-3 bg-white" }
      [:div.container
       [:a.navbar-brand {:href (url-for :home)} "Temci"]
       [:ul.nav.navbar-nav.pull-xs-right
        [:li.nav-item
         [:a.nav-link {:href (url-for :home)
                       :class (when (= active-page :home) "active")} "🏠Home"]]
        
        (when w3
          [:li.nav-item
           [:a.nav-link {:href (url-for :schedule)
                         :class (when (= active-page :schedule) "active")} "⏰Schedule"]])
        (when w3
          [:li.nav-item
           [:a.nav-link {:href (url-for :accounts) :class (when (= active-page :accounts) "active")} "🔐Accounts"]])]]]]))
