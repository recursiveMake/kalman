(ns kalman.visual
  (:require [incanter.charts :refer [scatter-plot]]
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
