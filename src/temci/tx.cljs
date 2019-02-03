(ns temci.tx
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced]]

             ;; web3
            [cljs-web3.eth :as web3-eth]))

(re-frame/reg-event-fx
 :tx/receipt-received
 (fn-traced [{:keys [db]} [_ txHash receipt]]
            {:db (assoc-in db [:txs txHash :receipt] receipt)}))

(re-frame/reg-event-fx
 :tx/details-received
 (fn-traced [{:keys [db]} [_ txHash details]]
            {:db (assoc-in db [:txs txHash :details] details)}))

(re-frame/reg-event-fx
 :tx/get-tx-data
 (fn-traced [{:keys [db]} [_ txHash]]
            {:get-tx-details {:w3 (:web3 db)
                              :txHash txHash
                              :has-details? (get-in db [:txs txHash :details])}
             :get-receipt {:w3 (:web3 db)
                           :txHash txHash
                           :receipt? (get-in db [:txs txHash :receipt])}}))
(re-frame/reg-fx
 :get-receipt
 (fn [{:keys [:w3 :txHash :receipt?]}]
   (when-not receipt?
     (web3-eth/get-transaction-receipt w3
                                       txHash
                                       (fn [error result]
                                         (when result
                                           (re-frame/dispatch [:tx/receipt-received txHash result])))))))

(re-frame/reg-fx
 :get-tx-details
 (fn [{:keys [:w3 :txHash :on-success :has-details?]}]
   (when-not has-details?
     (web3-eth/get-transaction w3
                               txHash
                               (fn [error result]
                                 (when result
                                   (re-frame/dispatch [:tx/details-received txHash result])))))))

(defn tx []
  (fn []
    (let [w3 @(re-frame/subscribe [:db/web3])
          txs @(re-frame/subscribe [:db/txs])
          txHash @(re-frame/subscribe [:db/current-txHash])
          tx (get txs txHash)
          details (:details tx)
          receipt (:receipt tx)]

      [:div.home-page
       [:div {:class "px-3 py-3 pt-md-5 pb-md-4 mx-auto" :style {:max-width "925px"}}
        [:div {:class "card"}
         [:div.card-status.bg-blue]
         [:div {:class "card-header"}
          [:h2 {:class "card-title"} "Tx Receipt"]]

         (if receipt
           [:div
            [:table {:class "table card-table"}
             [:thead
              [:tr
               [:th {:scope "col"} "#"]
               [:th {:scope "col"} "Value"]]]
             [:tbody
              [:tr
               [:td "Block Hash"]
               [:td (str (:blockHash receipt))]]
              [:tr
               [:td "Gas Used"]
               [:td (str (:gasUsed receipt))]]
              [:tr
               [:td "Contract Address"]
               [:td (str (:contractAddress receipt))]]]]]
           [:div {:class "card-alert alert alert-warning mb-0"}
            "Receipt is not yet available "])]

        [:div {:class "card"}
         [:div.card-status.bg-blue]
         [:div {:class "card-header"}
          [:h2 {:class "card-title"} "Tx Data"]]

         (if details
           [:div
            [:table {:class "table card-table"}
             [:thead
              [:tr
               [:th {:scope "col"} "#"]
               [:th {:scope "col"} "Value"]]]
             [:tbody
              [:tr
               [:td "Tx Hash"]
               [:td (str txHash)]]
              [:tr
               [:td "From"]
               [:td (str (:from details))]]
              [:tr
               [:td "To"]
               [:td (str (:to details))]]
              [:tr
               [:td "Value"]
               [:td (str (:value details) " (wei)")]]
              [:tr
               [:td "Chain"]
               [:td (:chain-id details)]]
              [:tr
               [:td "..."]
               [:td "..."]]]]]
           [:div {:class "card-alert alert alert-warning mb-0"}
            "Sorry, unable to locate this Tx Hash, check back later. (Not yet published?)"])]]])))