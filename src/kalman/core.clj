(ns kalman.core
  (:gen-class))

(def example-state {:position 1070.0
                    :error 1.0
                    :gain 0.0
                    :constant 0.75
                    :variance 200.0})

(def example-observations [814.0 754.0 435.0 126.0 180.0 128.0 45.0 -25.0 154.0])

(defn predict-position
  [state]
  (update-in state [:position] #(* (:constant state) %)))

(defn predict-error
  [state]
  (let [constant (:constant state)]
    (update-in state [:error] #(* constant (* % constant))))
)

(defn update-gain
  [state]
  (let [error (:error state)]
    (assoc-in state [:gain] (/ error (+ error (:variance state))))))

(defn update-position
  [state observation]
  (let [position (:position state)]
    (assoc-in state [:position] (+ position (* (:gain state) (- observation position))))))

(defn update-error
  [state]
  (assoc-in state [:error] (* (- 1 (:gain state)) (:error state))))

(defn kalman-predict
  [state]
  (predict-error (predict-position state)))

(defn kalman-update
  [state observation]
  (update-error (update-position (update-gain state) observation)))

(defn kalman
  [state observation]
  (kalman-update (kalman-predict state) observation))

(defn run-filter
  [start-state all-observations]
  (loop [state start-state
         observations all-observations]
    (if (> (count observations) 0)
      (let [new-state (kalman state (first observations))]
        (println new-state)
        (recur new-state (rest observations))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
