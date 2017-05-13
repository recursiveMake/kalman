(ns kalman.sample
  (:require [clojure.core.matrix :as m]
            [clojure.string :as string]
            [clojure.math.numeric-tower :as math]
            [kalman.io :refer [read-file, lazy-read-file, cast-line, annotate-row]]
            [kalman.system :refer [set-system-parameter, create-system]]))

;; Elements in this file are hardcoded for sample data
;; Generalize to remove the dependency on column headings in data matrix

(defn file-name
  "Demo sample file name"
  []
  "resources/data.txt")

(defn data
  "Load sample data from resources

  Assumes comma separated list of values with a header row.
  Returns a list of maps keyed by column headings"
  [file-name]
  (let [header (map keyword (string/split (first (lazy-read-file file-name)) #","))]
    (map #(annotate-row % header)
         (map #(cast-line % #",")
              (rest (read-file file-name))))))

(defn observables
  "Get observables from data map

  Formats observables as a data matrix.
  Hardcoded to :{x,y}-observable TODO: generalize"
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

(defn group
  "Given data seq and result seq, do zip assoc"
  [data result]
  (let [prediction (:prediction result)
        uncertainty (:uncertainty result)
        state (:state result)
        [x y vx vy] (seq state)]
    (assoc data
           :x-prediction (first prediction)
           :y-prediction (second prediction)
           :x-uncertainty (math/sqrt (first uncertainty))
           :y-uncertainty (math/sqrt (second uncertainty))
           :x-v-prediction vx
           :y-v-prediction vy)))
