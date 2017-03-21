(ns kalman.core
  (:require [kalman.filter :refer [kalman]]
            [kalman.system :refer [set-system-parameter, create-system]]
            [kalman.io :refer [read-file, lazy-read-file, cast-line, annotate-row]]
            [clojure.core.matrix :as m]
            [incanter.core :refer [view]]
            [incanter.charts :refer [scatter-plot]]
            [incanter.io :refer [read-dataset]])
  (:gen-class))

(def sample-data (map annotate-row
                      (map #(cast-line % #",")
                           (rest (read-file "resources/data.txt")))))

(def observables (map #(m/matrix [(:x-observable %) (:y-observable %)]) sample-data))

(def initial-state (first observables))

(def system
  (set-system-parameter
   (create-system 4 2)
   {:state (m/matrix [(first initial-state) (second initial-state) 0 0])
    :state-variance (m/matrix [
                               [ 1   0   0   0 ]
                               [ 0   1   0   0 ]
                               [ 0   0   1   0 ]
                               [ 0   0   0   1 ]])
    :state-predict (m/matrix [
                              [1 0 1 0]
                              [0 1 0 1]
                              [0 0 1 0]
                              [0 0 0 1]])
    ; :state-noise (m/mmul 0.1 (m/identity-matrix 4))
    :state-noise (m/matrix [
                            [0.1  0  0.1  0 ]
                            [ 0  0.1  0  0.1]
                            [0.1  0  0.1  0 ]
                            [ 0  0.1  0  0.1]])
    :sensor-variance (m/matrix [
                                [1 0 1 0]
                                [0 1 0 1]])
    :sensor-noise (m/matrix [
                             [0.1  0 ]
                             [ 0  0.1]])}))

(def data (read-dataset "resources/data.txt" :header true))

(view (scatter-plot :x-velocity :y-velocity :data data))

(defn -main
  ""
  [& args]
  (println "Hello, World!"))
