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
  "Fx + u"
  [system]
  (m/add (m/mmul (:state-predict system) (:state system)) (:control system)))

(defn predict-covariance
  "FPF' + Q"
  [system]
  (let [predict-mtx (:state-predict system)]
    (m/add (m/mmul predict-mtx
                   (m/mmul (:state-variance system)
                           (m/transpose predict-mtx)))
           (:state-noise system))))

;; Update functions

(defn update-gain
  "PH'(HPH' + R)^-1"
  [system]
  (let [sensor-variance (:sensor-variance system)
        state-variance (:state-variance system)]
    (m/mmul (m/mmul state-variance (m/transpose sensor-variance))
            (m/inverse
             (m/add
              (m/mmul sensor-variance
                      (m/mmul state-variance (m/transpose sensor-variance)))
              (:sensor-noise system))))))

(defn update-state
  "x + G(z-Hx)"
  [system observation]
  (m/add (:state system)
         (m/mmul (:gain system)
                 (m/sub observation
                        (m/mmul (:sensor-variance system) (:state system))))))

(defn update-covariance
  "P - GHP"
  [system]
  (m/sub (:state-variance system)
         (m/mmul (:gain system)
                 (m/mmul (:sensor-variance system)) (:state-variance system))))

(defn kalman-predict
  "Estimate state and covariance matrix"
  [system]
  (set-system-parameter system {:state (predict-state system)
                                :state-variance (predict-covariance system)}))

(defn kalman-update
  "Run the Kalman update step"
  [system observation]
  (let [sys (set-system-parameter system {:gain (update-gain system)})]
    (set-system-parameter sys {:state (update-state sys observation)
                               :state-variance (update-covariance sys)})))

(defn kalman-observe
  "Show kalman current kalman observation
  z = Hx, Sigma = HPH' "
  [system]
  {:observation (m/mmul (:sensor-variance system) (:state system))
   :uncertainty (m/mmul (:sensor-variance system) (m/mmul (:state-variance system) (m/transpose (:sensor-variance system))))})

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
      (let [updated-sys (kalman-step sys observation)
            state (:state updated-sys)
            prediction (kalman-observe updated-sys)]
        (println (str "  Obs: " observation))
        (println (str "State: " state))
        (println (str " Pred: " (:observation prediction)))
        (println (str "  Var: " (:uncertainty prediction)))
        (println "")
        (recur updated-sys (rest observations)))
      sys)))
