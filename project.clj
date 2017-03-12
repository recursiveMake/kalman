(defproject kalman "0.1.0-SNAPSHOT"
  :description "Implementation of Kalman Filter based on https://home.wlu.edu/~levys/kalman_tutorial/"
  :url "https://github.com/recursiveMake/kalman"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [incanter/incanter-core "1.9.1"]]
  :main ^:skip-aot kalman.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
