(ns temci.accounts
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [temci.db]
            [temci.routes :refer [url-for]]
            [temci.utils :as utils]))

(defn accounts []
  (let [w3 @(re-frame/subscribe [:db/web3])
        accounts @(re-frame/subscribe [:db/accounts])
        accountsCount (count accounts)
        txs @(re-frame/subscribe [:db/txs])
        txsCount (count txs)
        default-account (second (first accounts))]

    [:div.home-page
     [:div {:class "px-3 py-3 pt-md-5 pb-md-4 mx-auto" :style {:max-width "925px"}}
      

      [:div {:class "card"}
       [:div.card-status.bg-blue]
       [:div {:class "card-header"}
        [:h2 {:class "card-title"} (str "Accounts (" accountsCount ")")]]
       [:div
        [:table {:class "table card-table"}
         [:tbody
          (for [account accounts]
            (let [acc (second account)
                  id (:id acc)
                  balance (:balance acc)]

              [:tr {:key id}
               [:td (str  id)]
               [:td (str (utils/eth balance))]]))]]]]


      [:div {:class "card"}
       [:div.card-status.bg-yellow]
       [:div {:class "card-header"}
        [:h2 {:class "card-title"} (str "Transactions " (when (> txsCount 0) (str "(" txsCount ")")))]]
       [:div
        (if (> txsCount 0)
          [:table {:class "table card-table"}
           [:tbody
            (for [tx txs]
              (let [txHash (:txHash (second tx))]
                [:tr {:key txHash}
                 [:td (str txHash)]
                 [:td {:class "text-right"}
                  [:a {:class "btn btn-secondary btn-sm"
                       :href (url-for :tx :txHash txHash)} "View details"]]]))]]
          [:div {:class "card-alert alert-warning"
                 :style {:height "100%"
                         :margin "0"}}
          [:div {:style {:padding "20px"
                         :text-align "center"}}
           [:h4 "🔔You have no transactions"]
           [:p "You can schedule one using the button below."]]])]
       [:div.card-footer
        [:button {:class "btn btn-lg btn-primary"
                  :on-click (fn [e]
                              (.preventDefault e)
                              (re-frame/dispatch [:views/view-page {:page :schedule}]))}
         "New ⏰ Tx"]]]]]))
