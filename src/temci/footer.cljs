(ns temci.footer
  (:require [reagent.core :as reagent]
            [temci.routes :refer [url-for]]
            [re-frame.core :as re-frame]
            
            ;; web3
            [cljs-web3.core :as web3-core]
            [temci.network :refer [network-name]]))

(defn footer []
  (let [w3 @(re-frame/subscribe [:db/web3])]
    [:footer {:class "footer"}
     [:div.container
      [:div {:class "row align-items-center flex-row-reverse"} 
       [:div {:class "col-auto ml-lg-auto"}
        [:p
         [:span.logo-font "Temci"]
         [:span " © 2019 "[:a {:href "https://twitter.com/natewave" :target "_blank"} "@natewave"]]]]
       
       (when w3
         [:div {:class "col-12 col-lg-auto mt-3 mt-lg-0"}
          [:span (str "⚡️Connected: " (network-name (web3-core/version-network w3)))]
          [:p (str "🛠API: " (web3-core/version-api w3))]])]]]))



