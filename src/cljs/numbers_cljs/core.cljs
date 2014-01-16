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


(defn- info [numbers values]
  (let [{:keys [divisor condition]} values]
    (map(fn[x] {:n x
            :divisor (= 0 (mod x divisor))
            :condition false}) 
         numbers)))

(defn- get-several [h & more]
     (map #(aget h %) more))
 
(defn- numbers-ctrl [$scope]
  (aset $scope "values" (clj->js {
                         "range" 200
                         :divisor 2
                         :condition "x % 4 == 0"
                         }))
  (.$watchCollection $scope "[values.range, values.divisor, values.condition]" (
       fn[] 
        (let [values {:range (-> (.-values $scope)
                                 (.-range))
                      :condition (-> (.-values $scope)
                                 (.-condition))
                      :divisor (-> (.-values $scope)
                                 (.-divisor))}
              numbers (range 1 (:range values))
              numbers-with-info (info numbers values) ]
        (aset $scope "numbers" (clj->js numbers))
        (aset $scope "numbersWithInfo" (clj->js numbers-with-info))))))

;; main
(defn ^:export main []
  (doto (.module js/angular "numbers-cljs" (array "ngRoute"))
    (.config (array "$routeProvider" routes))
    (.controller "NumbersCtrl" (array "$scope" numbers-ctrl))))