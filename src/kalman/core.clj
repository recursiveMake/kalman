(ns kalman.core
  (:require [clojure.core.matrix :as m])
  (:gen-class))

(m/set-current-implementation :vectorz)

(defn set-system-parameter
  "Updates system with a vector"
  [system kv]
  (reduce #(assoc %1 (first %2) (second %2)) system kv))

(defn create-system
  "Create an empty system"
  [size]
  {:state (m/matrix (repeat size 0))    ; x, state
   :covariance (m/identity-matrix size) ; P, covariance
   ; nil sensor-variance or prediction leads to differentiate f and h
   :sensor-variance (m/identity-matrix size)  ; H, Jacobian of sensor function h(x)
   :prediction (m/identity-matrix size) ; F, Jacobian of state-transition function f(x, u)
   :noise (m/zero-matrix size size)     ; Q, untracked influences
   :gain (m/zero-matrix size size)      ; G, gain
   :sensor-noise (m/zero-matrix size size) ; R, sensor variance
   :state-function first                ; f(x, u)
   :sensor-function identity            ; h(x)
   :control (m/matrix (repeat size 0))  ; u, control
   :size 0})

(defn predict-state
  "f(x, u)"
  [system]
  ((:state-function system) (:state system) (:control system)))

(defn differentiate
  "Finite step differentiation (complex step is better)"
  ([function value]
   (differentiate function value 1e-8))
  ([function value epsilon]
   (let [fx-e  (function (m/add value epsilon))
         fx (function value)]
     (m/mmul (m/sub fx-e fx) (/ 1 epsilon)))))

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

(defn update-gain
  "PH'(HPH' + R)^-1"
  [system]
  (let [jacobian (or
                  (:sensor-variance system)
                  (differentiate (:sensor-function system) (:state system)))]
      (m/mmul (m/mmul (:covariance system) (m/transpose jacobian))
              (m/inverse (m/add (m/mmul jacobian (m/mmul (:covariance system) (m/transpose jacobian)))
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

(defn kalman
  "Run Kalman on one observation"
  [system observation]
  (kalman-update (kalman-predict system) observation))

(defn run-filter
  "Run Kalman filter on a system given a seq of observations"
  [system all-observations]
  (loop [sys system
         observations all-observations]
    (if-let [observation (first observations)]
      (let [updated-sys (kalman sys observation)]
        (println (select-keys updated-sys [:state :covariance]))
        (recur updated-sys (rest observations)))
      sys)))

(defn -main
  ""
  [& args]
  (println "Hello, World!"))
