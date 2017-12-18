(ns bookeeper.re-frame
  (:require [re-frame.core :as re-frame]
            [day8.re-frame.http-fx] ;; register :http-xhrio fx
            ))

(defn reg-event-db

  ([event-id handler]
   (reg-event-db event-id [] handler))

  ([event-id interceptor-vec handler]
   (re-frame/reg-event-db
     event-id
     [re-frame/trim-v
      interceptor-vec]
     handler)))

(defn reg-event-fx

  ([event-id handler]
   (reg-event-fx event-id [] handler))

  ([event-id interceptor-vec handler]
   (re-frame/reg-event-fx
     event-id
     [re-frame/trim-v
      interceptor-vec]
     handler)))
