(ns temci.db
  (:require [cljs.reader]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            
            ;; web3
            [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [district0x.re-frame.web3-fx]))

(re-frame/reg-sub
 :db/web3
 (fn [db]
   (:web3 db)))

(re-frame/reg-sub
 :db
 (fn [db]
   db))

(re-frame/reg-sub
 :db/web3-details
 (fn [db]
   (:web3-details db)))

(re-frame/reg-sub
 :db/accounts
 (fn [db]
   (:accounts db)))

(re-frame/reg-sub
 :db/txs
 (fn [db]
   (:txs db)))

(re-frame/reg-sub
 :db/current-txHash
 (fn [db]
   (:current-txHash db)))


;; -- Default app-db Value  ---------------------------------------------------
(def default-db {:active-page :home
                 :accounts nil
                 :web3 nil
                 :web3-details nil
                 :loading {}
                 :txs {}
                 :errors {}})

;; store receipts in localStorage ---------------------------------------------
;; Not used: original plan was to save transaction hashes into localStore
;; code left here for learning purposes, keeping tsx in :db for now, which means
;; unless we try to fetch the list of transactions for a given account directly from the network
;; without storing them, we'll lose the transactions list in the :accounts page.
(def txs-key "temci-txs")  ;; localstore key

(defn set-txs-ls
  [txs]
  (.setItem js/localStorage txs-key (str txs)))  ;; sorted-map written as an EDN map

(defn remove-txs-ls
  []
  (.removeItem js/localStorage txs-key))

;; -- cofx Registrations  -----------------------------------------------------
;;
(re-frame/reg-cofx
 :local-store-tsx
 (fn [cofx _]
   (assoc cofx :local-store-tsx
          (js->clj (some->> (.getItem js/localStorage txs-key)
                            (cljs.reader/read-string)
                            (.parse js/JSON))))))


;; -- Interceptors --------------------------------------------------------------
;;
(def set-tsx-interceptor [(re-frame/path :tsx)
                           (re-frame/after :local-store-tsx)
                           re-frame/trim-v])

(def remove-tsx-interceptor [(re-frame/after remove-tsx-ls)])
