(ns temci.network
  (:require [cljs.reader]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced]]

            [temci.routes :as routes]
            [temci.utils :as utils]
            
            ;; web3
            [cljs-web3.core :as web3-core]
            [cljs-web3.eth :as web3-eth]
            [district0x.re-frame.web3-fx]

            [promesa.core :as p]))

(re-frame/reg-event-fx
 :network/enable-web3
 (fn-traced [{:keys [db]} _]
            {:enable-web3 {:node-url nil
                           :on-success [:network/web3-enabled]
                           :on-failure [:network/error-enabling-web3]}}))

(re-frame/reg-fx
 :enable-web3
 (fn [{:keys [:node-url :on-success :on-error]}]
   (let [ethereum? (exists? (.-ethereum js/window))
         w3? (exists? (.-web3 js/window))
         Web3 (exists? (.-Web3 js/window))]
     (cond
       (and node-url Web3) (do
                  (set! js/web3 (new js/Web3 node-url))
                  (web3-eth/accounts js/web3
                                     (utils/dispach-fn on-success on-error)))))

       (and ethereum? Web3) (p/branch (p/promise (.enable (.-ethereum js/window)))
                                      (fn [res] (do
                                                  (set! (.-web3 js/window) (new js/Web3 (web3-core/current-provider js/web3)))
                                                  ((utils/dispatch-success on-success) res)))

                                      (fn [err]
                                        ((utils/dispatch-error on-error) err)))
      (and w3? Web3) (do
             (set! js/web3 (new js/Web3 (web3-core/current-provider js/web3)))
             (web3-eth/accounts js/web3
                                (utils/dispach-fn on-success on-error)))
       :else ((utils/dispatch-error [:routes/complete-request :home]) "No web3 provider could be found")))

(re-frame/reg-event-fx
 :network/web3-enabled
 (fn-traced [{:keys [db]} _]
            (let [w3 (.-web3 js/window)
                  network-id (web3-core/version-network w3)
                  web3-details {:network-id network-id
                                :web3-version (web3-core/version-api w3)}]
            {:db (assoc db :web3 w3 :web3-details web3-details)
             :dispatch [:network/load-accounts]})))

(re-frame/reg-event-fx
 :network/load-accounts
 (fn-traced [{:keys [db]} _]
   {:web3/call {:web3 (:web3 db)
                :fns [{:fn web3-eth/accounts
                       :args []
                       :on-success [:network/accounts-loaded]
                       :on-error   [:network/accounts-loading-errors]}]}}))

(re-frame/reg-event-fx
 :network/accounts-loaded
 (fn-traced [{:keys [:db]} [_ accounts]]
            {:db (assoc db :accounts (zipmap accounts (map (fn [v] {:id v}) accounts)))
             :dispatch [:network/load-balances accounts]}))

(re-frame/reg-event-fx
 :network/load-balances
 (fn [{:keys [:db]} [_ accounts]]
   {:web3/get-balances {:web3 (:web3 db)
                        :addresses (for [account accounts]
                                     {:id (str "balance-" account) ;; If you watch?, pass :id so you can stop watching later
                                      :address account
                                      :watch? true
                                      :on-success [:network/balance-loaded account]
                                      :on-error [:network/balanced-loading-errors]})}
    :set-url {:url (routes/url-for :accounts)}
    :dispatch [:views/view-page {:page :accounts}]}))

(re-frame/reg-event-fx
 :network/balance-loaded
 (fn-traced [{:keys [:db]} [_ account balance]]
            {:db (assoc-in db [:accounts account :balance] balance)}))

(defn network-name [name]
  (case name
    "1" "Mainnet or local"
    "2" "Morden test network (deprecated)"
    "3" "Ropsten test network"
    "4" "Rinkeby test network"
    "42" "Kovan test network"
    "Unknown"))
