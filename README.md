# kalman

An implementation of the Kalman filter based on https://home.wlu.edu/~levys/kalman_tutorial/

## Installation

Download from https://github.com/recursiveMake/kalman

## Usage

``` shell
lein repl
```

``` clojure
;; Create a system with parameters
(def start-state
  (set-system-parameter (create-system 1)
                        {:state (m/matrix [881])
                         :sensor-noise (m/matrix [200])
                         :state-function #(m/add (m/mmul 0.75 %) (m/mmul 0 %2))
                         :prediction (m/matrix [0.75])}))

;; Create observations
(def obs (map #(m/matrix [%]) [662 496 372 279 157 118 88 66]))

;; Run the filter
(run-filter start-state obs)
```

## License

Copyright © 2017 recursiveMake

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
