(ns kalman.core
  (:require [clojure.core.matrix :as m])
  (:gen-class))

(m/set-current-implementation :vectorz)

(def example-state {:state [] ; x, vector of n quantities that evolve with time
                    :covariance [] ; P, Sigma, matrix of correlations between objects in state, nxn
                    :sensor-variance [] ; H, matrix of variance of sensor n
                    :prediction [] ; F, matrix that evolves x
                    :control [] ; B, matrix for external influence
                    :control-vector [] ; u, vector for external influence
                    :noise [] ; Q, matrix of untracked influences
                    :gain [] ; K, matrix for gain
                    :sensor-noise [] ; R, matrix of noise in the sensor
                    })

;; (def start-state
;;   (set-system-parameter (create-system 1)
;;                         {:state (m/matrix [881])
;;                          :sensor-noise (m/matrix [200])
;;                          :prediction (m/matrix [0.75])}))

;; (def obs (map #(m/matrix [%]) [662 496 372 279 157 118 88 66]))

(defn set-system-parameter
  "Updates system with a vector"
  [system kv]
  (reduce #(assoc %1 (first %2) (second %2)) system kv))

(defn create-system
  "Create an empty system"
  [size]
  {:state (m/matrix (repeat size 0))
   :covariance (m/identity-matrix size)
   :sensor-variance (m/identity-matrix size)
   :prediction (m/identity-matrix size)
   :control (m/zero-matrix size size)
   :control-vector (m/matrix (repeat size 0))
   :noise (m/zero-matrix size size)
   :gain (m/zero-matrix size size)
   :sensor-noise (m/zero-matrix size size)
   :size 0})

(defn predict-state
  "Fx + Bu"
  [system]
  (m/add (m/mmul (:prediction system) (:state system))
         (m/mmul (:control system) (:control-vector system))))

(defn predict-covariance
  "FPF' + Q"
  [system]
  (m/add (m/mmul (:prediction system)
                 (m/mmul (:covariance system)
                         (m/transpose (:prediction system))))
         (:noise system)))

(defn update-gain
  "PH'(HPH' + R)^-1"
  [system]
  (m/mmul (m/mmul (:covariance system) (m/transpose (:sensor-variance system)))
          (m/inverse (m/add (m/mmul (:sensor-variance system) (m/mmul (:covariance system) (m/transpose (:sensor-variance system))))
                            (:sensor-noise system)))))

(defn update-state
  "x + K(z-Hx)"
  [system observation]
  (m/add (:state system)
         (m/mmul (:gain system)
                 (m/sub observation
                        (m/mmul (:sensor-variance system) (:state system))))))

(defn update-covariance
  "P - KHP"
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
  [system all-observations]
  (loop [sys system
         observations all-observations]
    (if (> (count observations) 0)
      (let [new-sys (kalman sys (first observations))]
        (println (select-keys new-sys [:state :covariance]))
        (recur new-sys (rest observations))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
