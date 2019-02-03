(ns temci.eac
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            
            ;; temci
            [temci.routes :refer [url-for]]
            [temci.utils :as utils]
            [temci.constants :as constants]

             ;; web3
            [cljs-web3.core :as web3-core]
            [cljs-web3.eth :as web3-eth]))

(re-frame/reg-fx
 :schedule-tx
 (fn [{:keys [:w3 :from :on-success :on-failure :schedule-details]}]
   (let [sd schedule-details ;; all data is in wei
         endowment (utils/totalAmount (:callGas sd)
                                      (:callValue sd)
                                      (:gasPrice sd)
                                      (:fee sd)
                                      (:bounty sd))
         timestampSchedulerAbi constants/timestampAbi
         tSchedulerAddress (constants/tSchedulerAddress (web3-core/version-network w3))
         timestampScheduler (web3-eth/contract-at w3 timestampSchedulerAbi tSchedulerAddress)]
     ;; values in wei
     (.schedule timestampScheduler
                (:toAddress sd)
                (:callData sd)
                (array (:callGas sd)
                       (:callValue sd)
                       (:windowSize sd)
                       (:windowStart sd)
                       (:gasPrice sd)
                       (:fee sd)
                       (:bounty sd)
                       (:requiredDeposit sd))
                (js-obj "from" from
                        "gas" 7000000
                        "gasPrice" nil
                        "value" endowment)
                (fn [err receipt]
                  (if err
                    ;; ingoring error details for now
                    (do
                      ((utils/dispatch-error on-failure) err))
                    ((utils/dispatch-success on-success) receipt)))))))
