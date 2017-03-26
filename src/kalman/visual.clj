(ns kalman.visual
  (:require [incanter.charts :refer [scatter-plot, add-points]]
            [incanter.core :refer [view]]
            [incanter.io :refer [read-dataset]]
            [kalman.sample :as sample])
  (:gen-class))

(defn data
  "Load the sample dataset"
  []
  (read-dataset (sample/file-name) :header true))

(defn v
  "View a dataset"
  [data]
  (view data))

(defn plot
  "Plot a dataset"
  [data]
  (scatter-plot :x-position :y-position :data data))

(defn plot
  [data x-1 y-1 x-2 y-2]
  (doto
      (scatter-plot (map x-1 data) (map y-1 data))
    (add-points (map x-2 data) (map y-2 data))
    (v)))
