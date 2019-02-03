(ns temci.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]
            [clojure.string :as str]

            [day8.re-frame.tracing :refer-macros [fn-traced]]))

;; The routes setup is inspired by J. Pablo Fernández 
;; source: https://pupeno.com/2015/08/26/no-hashes-bidirectional-routing-in-re-frame-with-bidi-and-pushy/

;; -- Routes ------------------------------------------------------------------
(def routes
  ["/temci/" {""               :home
        "accounts"       :accounts
        "transactions"   :transactions
        "schedule"       :schedule
        "tx/"            {[:txHash]    :tx}}])

(defn- parse-url
  [url]
  (bidi/match-route routes url))

(defn- dispatch-route
  [matched-route]
  (re-frame/dispatch [:views/view-page {:page (:handler matched-route)
                                        :txHash      (get-in matched-route [:route-params :txHash])}]))

;; -- Router Start ------------------------------------------------------------
;;
(defn app-routes
  []
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def url-for (partial bidi/path-for routes))

;; -- history -----------------------------------------------------------------
(def history (pushy/pushy dispatch-route (partial bidi/match-route routes)))

;; -- set-token! --------------------------------------------------------------
(defn set-token! [token]
  (pushy/set-token! history token))

(re-frame/reg-fx
 :set-url
 (fn [{:keys [:url]}]
   (set-token! url)))


;; event-fx/db
;;
(re-frame/reg-event-db
 :routes/complete-request
 (fn-traced [db [_ request-type]]
            (assoc-in db [:loading request-type] false)))

(re-frame/reg-event-fx
 :routes/api-request-error
 (fn-traced [{:keys [db]} [_ request-type response]]
            {:db (assoc-in db [:errors request-type] (str response))
             :dispatch [:routes/complete-request request-type]}))
