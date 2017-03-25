(ns kalman.sample
  (:require [clojure.core.matrix :as m]
            [clojure.string :as string]
            [kalman.io :refer [read-file, lazy-read-file, cast-line, annotate-row]]
            [kalman.system :refer [set-system-parameter, create-system]]))

(defn file-name
  "Get sample file name"
  []
  "resources/data.txt")

(defn data
  "Load sample data from resources"
  [file-name]
  (let [header (map keyword (string/split (first (lazy-read-file file-name)) #","))]
    (map #(annotate-row % header)
         (map #(cast-line % #",")
              (rest (read-file file-name))))))

(defn observables
  "Get observables from data map"
  [data]
  (map #(m/matrix [(:x-observable %) (:y-observable %)]) data))

(defn system
  "Create a sample system"
  [observable]
  (set-system-parameter
   (create-system 4 2)
   {:state (m/matrix [(first observable) (second observable) 0 0])
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
    :state-noise (m/matrix [
                            [0.1  0  0.1  0 ]
                            [ 0  0.1  0  0.1]
                            [0.1  0  0.1  0 ]
                            [ 0  0.1  0  0.1]])
    :sensor-variance (m/matrix [
                                [ 1  0  1  0]
                                [ 0  1  0  1]])
    :sensor-noise (m/matrix [
                             [0.1  0 ]
                             [ 0  0.1]])}))
