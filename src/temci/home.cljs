(ns temci.home
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]))

(defn home []
  (let [w3 @(re-frame/subscribe [:db/web3])
        accounts @(re-frame/subscribe [:db/accounts])
        default-account (second (first accounts))]
    
        [:div.home-page
         [:div {:class "px-3 py-3 pt-md-5 pb-md-4 mx-auto text-center"
                :style {:max-width "700px"}}
          [:h1 {:class "banner display-4 logo-font"} "Temci"]
          [:p {:class "lead"} "Temci let's you schedule Ethereum Transactions for a delayed execution, in a trustless way. It uses the " [:a {:href "https://www.ethereum-alarm-clock.com/" :target "_blank"} "Ethereum Alarm Clock"] " project and is entirely written in " [:a {:href "https://clojurescript.org/" :target "_blank"} "ClojureScript"] "."]]

         [:div {:class "px-3 py-3 pt-md-5 pb-md-4 mx-auto text-center" :style {:max-width "700px"}}
          [:div {:class "container"}
           [:h2 "Wanna Try?"]
           (if (exists? js/web3)
             [:div
              [:button {:class "btn btn-lg btn-primary"
                        :on-click (fn [e]
                                    (.preventDefault e)
                                    (re-frame/dispatch [:network/enable-web3]))}
               "Continue with Browser Extention"]]
             [:div
              [:p "Oops. 🙊 It looks like you don't have a browser extension to connect to the blockchain."]
              [:a {:href "https://metamask.io"} "Get one here."]])
           ;; todo
           [:div {:style { :margin-top "10px" }}
            [:h3 {:style {:margin-bottom "5px"}} "or"]
            [:p "Connect your own/remote node (soon)"]
            [:div.input-group
             [:input.form-control.form-control-lg {:style {:margin-right "5px"}
                                                   :type "text"
                                                   :placeholder "Node Address"
                                                   :value nurl
                                                   :on-change #(swap! node-url (.. % -target -value))
                                                   :disabled true}]
             [:button.input-group-btn {:class "btn btn-lg btn-warning"
                                       :disabled true
                                       :on-click (fn [e]
                                                   (.preventDefault e)
                                                   (re-frame/dispatch [:network/enable-web3 node-url]))}
              "Go!"]]]]]]))
