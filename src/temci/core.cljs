(ns temci.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [temci.views :as views]
   [temci.config :as config]
   [temci.routes :as routes]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/app]
                  (.getElementById js/document "body")))

(defn ^:export main []
  []
  ;; Hooking up the router on start
  (routes/app-routes)
  
  (re-frame/dispatch-sync [:views/load])

  (dev-setup)
  (mount-root))
