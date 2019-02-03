(ns temci.utils
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced]]

             ;; web3
            [cljs-web3.core :as web3-core]

            ["bignumber.js" :refer (BigNumber)]
            [oops.core :refer [ocall]]))

(defn now []
  (js/Date.))

(defn eth [big-num]
  (str (web3-core/from-wei big-num :ether) " ETH"))

(defn dispatch-success [on-success & args]
  (fn [res]
    (re-frame/dispatch (vec (concat on-success (cons res args))))))

(defn dispatch-error [on-error & args]
  (fn [err]
    (re-frame/dispatch (vec (concat on-error (cons err args))))))

(defn dispach-fn [on-success on-error & args]
  (fn [err res]
    (if err
      (dispatch-error on-error args)
      (dispatch-success on-success args))))

(defn totalAmount [callGas callValue gasPrice fee bounty]
  (let [callGasBN (BigNumber. callGas)
        callValueBN (BigNumber. callValue)
        gasPriceBN (BigNumber. gasPrice)
        feeBN (BigNumber. fee)
        bountyBN (BigNumber. bounty)]

        (-> bountyBN
            (ocall :plus feeBN)
            (ocall :plus (ocall callGasBN :times gasPrice))
            (ocall :plus (ocall gasPriceBN :times 180000))
            (ocall :plus callValueBN)
            (ocall :toNumber))))
