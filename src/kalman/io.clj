(ns kalman.io
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn read-file
  "Load lines in a file"
  [file-name]
  (string/split-lines (slurp file-name)))

(defn lazy-read-file
  "Load lines in a file"
  [file-name]
  (with-open [rdr (io/reader file-name)]
    (line-seq rdr)))

(defn cast-line
  "Convert a tab-separated file into doubles"
  ([line]
   (cast-line line #"\t"))
  ([line separator]
   (map #(Double. %) (string/split line separator))))

(defn annotate-row
  "Load columns in a row with resources format"
  ([separated-line labels]
   (zipmap labels separated-line))
  ([separated-line]
   (annotate-row separated-line
                 [:x-position :y-position
                  :x-velocity :y-velocity
                  :x-observable :y-observable])))
