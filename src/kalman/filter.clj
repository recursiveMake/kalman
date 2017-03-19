(ns kalman.filter
  (:require [clojure.core.matrix :as m]
            [kalman.system :refer [set-system-parameter]])
  (:gen-class))

(defn- differentiate
  "Finite step differentiation (complex step is better)"
  ([function value]
   (differentiate function value 1e-8))
  ([function value epsilon]
   (let [fx-e  (function (m/add value epsilon))
         fx (function value)]
     (m/mmul (m/sub fx-e fx) (/ 1 epsilon)))))

;; Predict functions

(defn predict-state
  "f(x, u)"
  [system]
  ((:state-function system) (:state system) (:control system)))

(defn predict-covariance
  "FPF' + Q"
  [system]
  (let [jacobian (or
                  (:prediction system)
                  (differentiate
                   #((:state-function system) % (:control system))
                   (:state system)))]
    (m/add (m/mmul jacobian
                   (m/mmul (:covariance system)
                           (m/transpose jacobian)))
           (:noise system))))

;; Update functions

(defn update-gain
  "PH'(HPH' + R)^-1"
  [system]
  (let [jacobian (or
                  (:sensor-variance system)
                  (differentiate (:sensor-function system) (:state system)))]
      (m/mmul (m/mmul (:covariance system) (m/transpose jacobian))
              (m/inverse
               (m/add
                (m/mmul jacobian (m/mmul (:covariance system) (m/transpose jacobian)))
                (:sensor-noise system))))))

(defn update-state
  "x + G(z-h(x))"
  [system observation]
  (m/add (:state system)
         (m/mmul (:gain system)
                 (m/sub observation
                        ((:sensor-function system) (:state system))))))

(defn update-covariance
  "P - GHP"
  [system]
  (m/sub (:covariance system)
         (m/mmul (:gain system)
                 (m/mmul (:sensor-variance system)) (:covariance system))))

(defn kalman-predict
  "Estimate state and covariance matrix"
  [system]
  (set-system-parameter system {:state (predict-state system)
                                :covariance (predict-covariance system)}))

(defn kalman-update
  "Run the Kalman update step"
  [system observation]
  (let [sys (set-system-parameter system {:gain (update-gain system)})]
    (set-system-parameter sys {:state (update-state sys observation)
                               :covariance (update-covariance sys)})))

(defn kalman-step
  "Run Kalman on one observation"
  [system observation]
  (kalman-update (kalman-predict system) observation))

(defn kalman
  "Run Kalman filter on a system given a seq of observations"
  [system all-observations]
  (loop [sys system
         observations all-observations]
    (if-let [observation (first observations)]
      (let [updated-sys (kalman-step sys observation)]
        (println (select-keys updated-sys [:state :covariance]))
        (recur updated-sys (rest observations)))
      sys)))