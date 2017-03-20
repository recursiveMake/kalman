(ns kalman.system
  (:require [clojure.core.matrix :as m])
  (:gen-class))

(m/set-current-implementation :vectorz)

(defn- compatible-shape?
  "Check if matrices have the same size"
  [m-1 m-2]
  (= (m/shape m-1) (m/shape m-2)))

(defn set-system-parameter
  "Updates system map with values from supplied map"
  [system kv]
  (loop [updated-system system
         [key value] (first kv)
         other (rest kv)]
    (if-not key
      updated-system
      (if (compatible-shape? (get updated-system key) value)
        (recur (assoc updated-system key value)
               (first other)
               (rest other))
        (throw (Exception. (str "incompatible shape: " key
                                ". Expected: " (m/shape (get updated-system key))
                                ". Received: " (m/shape value))))))))

(defn create-system
  "Create an empty system of given size (states sensors)
  :state            x, vector of system state (n,1)
  (observable)      z, vector of system observables (m,1)
  :state-variance   P, covariance of states (n,n)
  :state-predict    F, (n,n)
  :state-noise      Q, matrix of untracked influences (n,n)
  :control          u, control input (n,1)
  :sensor-variance  H, relation between sensor and state (m,n)
  :sensor-noise     R, sensor variance matrix (m,m)
  :gain             K, gain (n,m)

  Relevant in non-linear settings
  :state-function   f(x, u) If non-nil, F = f'
  :sensor-function  h(x) If non-nil, H = h'"
  [states sensors]
  {:state (m/matrix (repeat states 0))
   :state-variance (m/identity-matrix states)
   :state-predict (m/identity-matrix states)
   :state-noise (m/zero-matrix states states)
   :control (m/matrix (repeat states 0))

   :sensor-variance (m/zero-matrix sensors states)
   :sensor-noise (m/zero-matrix sensors sensors)

   :gain (m/zero-matrix states sensors)

   :state-function nil
   :sensor-function nil

   :size {:states states :sensors sensors}})
