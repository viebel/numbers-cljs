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


(defn- numbers-ctrl [$scope]
  (.$watch $scope "range" (fn []
      (aset $scope "range" 200)
      (aset $scope "divisor" 2)
      (aset $scope "condition" "x % 4 == 0")
  (.$watchCollection $scope "[range, divisor, condition]" (fn[] 
        (aset $scope "numbers" (clj->js (range 1 (.-range $scope))))
        ;(aset $scope "numbersWithInfo" (info x($scope.numbers, $scope.divisor, $scope.condition);
    )))))

;; main
(defn ^:export main []
  (doto (.module js/angular "numbers-cljs" (array "ngRoute"))
    (.config (array "$routeProvider" routes))
    (.controller "NumbersCtrl" (array "$scope" numbers-ctrl))))
