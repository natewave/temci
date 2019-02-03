(ns temci.views
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [temci.db :refer [default-db]]

   ;; components
   [temci.home :refer [home]]
   [temci.header :refer [header]]
   [temci.footer :refer [footer]]
   [temci.accounts :refer [accounts]]
   [temci.schedule :refer [schedule]]
   [temci.tx :refer [tx]]
   [temci.routes :refer [url-for]]))

;; -- Subscriptions  -------------------------------------------------
;;
(re-frame/reg-sub
  :views/active-page
  (fn [db]
    (:active-page db)))

;;
;; -- Event handlers -------------------------------------------------
;;
(re-frame/reg-event-fx 
 :views/load
  
 (fn-traced [{:keys [db]} _]
            {:db (-> default-db)}))

(re-frame/reg-event-fx                                                              
 :views/view-page
 (fn-traced [{:keys [db]} [_ {:keys [:page :txHash]}]]
            (let [new-db (-> db
                             ;; cleanup errors and loadings
                             (update :errors empty)
                             (update :loading empty))]
              (case page
                :home {:db (assoc new-db :active-page :home)
                       
                       :set-url {:url (url-for :home)}}
                
                
                ;; -- URL @ "/accounts" | "/schedule" -----------------------
                (:accounts :schedule) (if (:web3 db)
                                        {:db (assoc new-db :active-page page)
                                         :set-url {:url (url-for page)}}
                                        {:db (assoc new-db :active-page :home)
                                         :set-url {:url (url-for :home)}})
                ;; -- URL @ "/tx/:txHash"  ----------------------------------
                (:tx) (if (:web3 db)
                        {:db (assoc new-db :active-page page :current-txHash txHash)
                         :set-url {:url (url-for page :txHash txHash)}
                         :dispatch [:tx/get-tx-data txHash]}
                        {:db (assoc new-db :active-page :home)
                         :set-url {:url (url-for :home)}})))))

;; -- Views ----------------------------------------------------------
;;

(defn pages [page-name]
  (case page-name
    :home [home]
    :accounts [accounts]
    :schedule [schedule]
    :tx [tx]
    [home]))

(defn app []
  (let [active-page @(re-frame/subscribe [:views/active-page])]
    [:div#app.app-container
     [header]
     [pages active-page]
     [footer]]))
