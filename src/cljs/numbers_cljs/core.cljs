(ns numbers_cljs.core
  (:require [clojure.string :as string]))

;; angular helpers
(defn- ng-route [provider path route-spec]
  (.when provider path (clj->js route-spec)))

(defn- ng-route-otherwise [provider route-spec]
  (.otherwise provider (clj->js route-spec)))


;; routes
(defn- routes [$routeProvider]
  (doto $routeProvider
    (ng-route "/numbers"
              {:controller "NumbersCtrl"
               :templateUrl "partials/numbers.html"})
    (ng-route-otherwise {:redirectTo "/numbers"})))


(defn- numbers-ctrl [$scope]
  (let [
       my-merge (fn[a b]
          (doseq [[k v] b]
            (aset a k v)))
       watch-changes (fn[]
            (.$watchCollection $scope "[range, divisor, condition]" (
               fn[[rr dd condition] old-vals] 
                (let [[my-range divisor] (map #(js/parseInt %) [rr dd])
                      numbers (range 1 (+ 1 my-range))
                      check (fn[z] (try 
                                     (js/eval (string/replace condition "x" z))
                                     (catch js/Object e  false)))
                      numbers-with-info (map (fn[x] { :n x
                                                      :divisible (= 0 (mod x divisor))
                                                      :condition (check x)
                                                    }) 
                                         numbers)]
                (doto $scope 
                  (aset "numbers" (clj->js numbers))
                  (aset "numbersWithInfo" (clj->js numbers-with-info)))))))
        ]

  (my-merge $scope { "range" 100
                     "divisor" 2
                     "condition" "x % 4 == 0"})
  (watch-changes)))

;; main
(defn ^:export main []
  (doto (.module js/angular "numbers-cljs" (array "ngRoute"))
    (.config (array "$routeProvider" routes))
    (.controller "NumbersCtrl" (array "$scope" numbers-ctrl))))
