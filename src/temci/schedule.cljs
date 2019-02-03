(ns temci.schedule
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [clojure.string :refer [blank?]]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [temci.utils :as utils]
            [temci.errors :refer [errors-list]]
            [temci.eac]
             ;; web3
            [cljs-web3.core :as web3-core]

            [moment :as moment]))

; subs
(re-frame/reg-sub
 :schedule/loading
 (fn [db]
   (get-in db [:loading :schedule])))

(re-frame/reg-sub
 :schedule/errors
 (fn [db]
   (get-in db [:errors :schedule])))

(re-frame/reg-event-fx
 :schedule/tx-scheduled

 (fn-traced [{:keys [db]} [_ tx]]
            {:db (assoc-in db [:txs tx :txHash] tx)
             :dispatch-n (list [:routes/complete-request :schedule]
                               [:views/view-page {:page :accounts}])}))

(re-frame/reg-event-fx
 :schedule/schedule-tx
 (fn-traced [{:keys [db]} [_ scheduling]]
            (let [from (:from scheduling)
                  toAddress (:toAddress scheduling)
                  windowSize (:windowSize scheduling)
                  windowStart (.unix (.add (new moment) (:windowStart scheduling) "hour"))
                  callData (:callData scheduling)
                  callGas (:callGas scheduling)
                  callValue (web3-core/to-wei (:callValue scheduling) "ether")
                  gasPrice (web3-core/to-wei (:gasPrice scheduling) "gwei")
                  fee (web3-core/to-wei (:fee scheduling) "finney")
                  bounty (web3-core/to-wei (:bounty scheduling) "finney")
                  requiredDeposit (web3-core/to-wei (:requiredDeposit scheduling) "ether")]
              {:db         (assoc-in db [:loading :schedule] true)
               :schedule-tx {:w3 (:web3 db)
                             :from from
                             :schedule-details {:toAddress toAddress
                                                :windowSize windowSize
                                                :windowStart windowStart
                                                :callData callData
                                                :callGas callGas
                                                :callValue callValue
                                                :gasPrice gasPrice
                                                :fee fee
                                                :bounty bounty
                                                :requiredDeposit requiredDeposit}
                             :on-success [:schedule/tx-scheduled]
                             :on-failure [:routes/api-request-error :schedule]}})))

(defn not-blank [m s]
  (and (contains? m s)
       (not (blank? (get m s)))))

(defn is-valid [m vals]
  (every? (fn [v] (not-blank m v)) vals))

(defn validate [schedule-data]
    (is-valid schedule-data [:from :toAddress :windowSize :windowStart :callData :callGas :callValue :gasPrice :fee :bounty :requiredDeposit]))

(defn schedule-tx [event schedule-details]
  (.preventDefault event)
  ;; validation
  (if (validate schedule-details)
    (re-frame/dispatch [:schedule/schedule-tx schedule-details])
    (re-frame/dispatch [:routes/api-request-error :schedule "Some fields are empty or invalid"])))

(defn schedule []
  (let [w3 @(re-frame/subscribe [:db/web3])
        accounts @(re-frame/subscribe [:db/accounts])
        default-account (second (first accounts))
        default {:from (:id default-account)
                 :toAddress ""
                 :callData ""
                 :callValue ""
                 :windowStart 10
                 :windowSize 5
                 :callGas 30000
                 :gasPrice 20
                 :fee 10
                 :bounty 25
                 :requiredDeposit 0.1}
        s-data (reagent/atom default)]
    (fn []
      (let [{:keys [from toAddress callData callValue windowStart windowSize callGas gasPrice fee bounty requiredDeposit  ]} @s-data
            loading?    @(re-frame/subscribe [:schedule/loading])
            errors      @(re-frame/subscribe [:schedule/errors])]
        [:div.schedule-page
         [:div.container.page
          [:div.row
           [:div.col-md-6.offset-md-3.col-xs-12.card
            [:div.card-header [:h3.card-title "Schedule Tx"]]
            (when errors
              [errors-list (str errors)])
            [:div.card-body
             [:h3.card-title.form-label "Send from:"]
             [:form
              [:fieldset.form-label-group
               [:select.form-control.form-control-lg {:field :list
                                                      :id :many.options
                                                      :on-change #(swap! s-data assoc :from (-> % .-target .-value))}

                (for [account accounts]
                  (let [acc (second account)
                        id (:id acc)
                        balance (:balance acc)]
                    [:option {:key (keyword id)} (str id " (" (utils/eth balance) ")")]))]]

              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "text"
                                                     :placeholder "Destination Address"
                                                     :value toAddress
                                                     :on-change #(swap! s-data assoc :toAddress (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "toAddress"} "Destination Address"]]
              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "text"
                                                     :placeholder "Tx Data"
                                                     :value callData
                                                     :on-change #(swap! s-data assoc :callData (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "callData"} "Tx Data"]]

              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "number"
                                                     :step "any"
                                                     :min "0"
                                                     :placeholder "Amount to send (ETH)"
                                                     :value callValue
                                                     :on-change #(swap! s-data assoc :callValue (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "callValue"} "Amount to send (ETH)"]]

              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "number"
                                                     :placeholder "Gas Amount"
                                                     :step "any"
                                                     :min "0"
                                                     :value callGas
                                                     :on-change #(swap! s-data assoc :callGas (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "callGas"} "Gas Amount"]]


              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "number"
                                                     :placeholder "Gas Price (gwei)"
                                                     :value gasPrice
                                                     :step "any"
                                                     :min "0"
                                                     :on-change #(swap! s-data assoc :gasPrice (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "gasPrice"} "Gas Price (gwei)"]]

              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "number"
                                                     :placeholder "Fee (finney)"
                                                     :value fee
                                                     :step "any"
                                                     :min "0"
                                                     :on-change #(swap! s-data assoc :fee (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "fee"} "Fee (finney)"]]

              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "number"
                                                     :placeholder "Time Bounty (finney)"
                                                     :value bounty
                                                     :step "any"
                                                     :min "0"
                                                     :on-change #(swap! s-data assoc :bounty (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "bounty"} "Time Bounty (finney)"]]

              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "number"
                                                     :placeholder "Deposit"
                                                     :step "any"
                                                     :min "0"
                                                     :value requiredDeposit
                                                     :on-change #(swap! s-data assoc :requiredDeposit (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "requiredDeposit"} "Deposit"]]

              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "number"
                                                     :placeholder "Time (hours from now)"
                                                     :min "0"
                                                     :value windowStart
                                                     :on-change #(swap! s-data assoc :windowStart (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "windowStart"} "Time (hours from now)"]]

              [:fieldset.form-label-group
               [:input.form-control.form-control-lg {:type "number"
                                                     :placeholder "Window Size"
                                                     :step "any"
                                                     :min "5"
                                                     :value windowSize
                                                     :on-change #(swap! s-data assoc :windowSize (-> % .-target .-value))
                                                     :disabled loading?}]
               [:label {:for "windowSize"} "Window Size (in min)"]]
              [:div.card-footer
               [:button.btn.btn-lg.btn-primary.pull-xs-right {:on-click #(schedule-tx % @s-data)
                                                              :class (when loading? "disabled")} "Schedule"]]
              ]]
           ]]]]))))
