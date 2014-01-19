(ns numbers_cljs.core
  (:require [cljs.reader :as reader]))

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


(defn- info [numbers {:keys [divisor condition]}]
    (map(fn[x] {:n x
            :divisible (= 0 (mod x divisor))
            :condition false}) 
         numbers))

(defn- numbers-ctrl [$scope]
  (aset $scope "range" 100)
  (aset $scope "divisor" 2)
  (aset $scope "condition" "x % 4 == 0")
  (.$watchCollection $scope "[range, divisor, condition]" (
       fn[new-vals] 
        (let [[my-range divisor condition] new-vals
              values {:divisor (js/parseInt divisor)
                      :condition condition}
              numbers (range 1 (+ 1 (js/parseInt my-range)))
              numbers-with-info (info numbers values)]
        (aset $scope "numbers" (clj->js numbers))
        (aset $scope "numbersWithInfo" (clj->js numbers-with-info))))))

;; main
(defn ^:export main []
  (doto (.module js/angular "numbers-cljs" (array "ngRoute"))
    (.config (array "$routeProvider" routes))
    (.controller "NumbersCtrl" (array "$scope" numbers-ctrl))))
