(ns kalman.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [kalman.filter :refer [kalman, logging-kalman]]
            [kalman.sample :as sample]
            [kalman.visual :as visual])
  (:gen-class))

(def cli-options
  ; A file name
  [["-f" "--file FILE" "Input data file"
    :default (sample/file-name)]
   ["-h" "--help"]]
)

(defn demo
  ""
  [file-name]
  (let
      [s-data (sample/data file-name)
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

(defn parse-arguments
  [args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]
    (cond
      ; help
      (:help options)
      summary
      ; failed
      errors
      (string/join \newline errors)
      ; success
      true
      (demo (:file options)))))

(defn -main
  [& args]
  (println (parse-arguments args)))
