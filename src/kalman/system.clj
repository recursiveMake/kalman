(ns kalman.system
  (:require [clojure.core.matrix :as m])
  (:gen-class))

(m/set-current-implementation :vectorz)

(defn set-system-parameter
  "Updates system map with values from supplied map"
  [system kv]
  (reduce #(assoc %1 (first %2) (second %2)) system kv))

(defn create-system
  "Create an empty system of given size
  :state            x, vector of system state
  :covariance       P
  :sensor-variance  H, Jacobian of sensor function (if nil, uses differentiation to obtain)
  :prediction       F, Jacobian of state function (if nil, uses differentiation to obtain)
  :noise            Q, matrix of untracked influences
  :gain             K, gain
  :sensor-noise     R, sensor variance matrix
  :state-function   f(x, u)
  :sensor-function  h(x)
  :control          u"
  [size]
  {:state (m/matrix (repeat size 0))
   :covariance (m/identity-matrix size)
   :sensor-variance (m/identity-matrix size)
   :prediction (m/identity-matrix size)
   :noise (m/zero-matrix size size)
   :gain (m/zero-matrix size size)
   :sensor-noise (m/zero-matrix size size)
   :state-function first
   :sensor-function identity
   :control (m/matrix (repeat size 0))
   :size size})
