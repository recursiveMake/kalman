(ns kalman.core
  (:require [kalman.filter :refer [kalman, logging-kalman]]
            [kalman.sample :as sample]
            [kalman.visual :as visual])
  (:gen-class))

(defn -main
  ""
  [& args]
  (let
      [s-data (sample/data (sample/file-name))
       obs (sample/observables s-data)
       system (sample/system (first obs))
       observables (rest obs)
       result (logging-kalman system observables)
       data (map sample/group (rest s-data) result)]
    (visual/plot
     data :x-observable :y-observable :x-prediction :y-prediction)
    (visual/plot
     data :x-position :y-position :x-prediction :y-prediction)
    (visual/plot
     data :x-velocity :y-velocity :x-v-prediction :y-v-prediction)))
