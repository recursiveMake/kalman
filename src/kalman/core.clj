(ns kalman.core
  (:require [kalman.filter :refer [kalman]]
            [kalman.system :refer [set-system-parameter, create-system]])
  (:gen-class))

(defn -main
  ""
  [& args]
  (println "Hello, World!"))
