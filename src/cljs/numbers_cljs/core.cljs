(ns numbers_clsjs.core
  (:require [cljs.reader :as reader]))

;; angular helpers
(defn- ng-route [provider path route-spec]
  (.when provider path (clj->js route-spec)))

(defn- ng-route-otherwise [provider route-spec]
  (.otherwise provider (clj->js route-spec)))

(defn- edn-ajax-cfg [& [headers]]
  (let [cfg {:transformRequest (fn [data] (pr-str data))
             :transformResponse (fn [raw] (reader/read-string raw))}]
    (clj->js (if headers
               (assoc cfg :headers headers)
               cfg))))



;; hooks
(defn- navbar-helper [$rootScope]
  (aset $rootScope "sharedVars" (clj->js {:page 1
                                          :minDC 4
                                          :minC 7
                                          :selectType "newest"
                                          :searchQuery ""
                                          :showJunk false}))
  (.$on $rootScope "$routeChangeSuccess"
        (fn [_ current-route]
          (aset $rootScope "pageKey" (.-pageKey current-route)))))

;; routes
(defn- routes [$routeProvider]
  (doto $routeProvider
    (ng-route "/phrases-monitor"
              {:controller "PhraseListCtrl"
               :pageKey "phrases-monitor"
               :templateUrl "templates/phrases-monitor/main.html"})
    (ng-route "/phrases/:phraseId"
              {:controller "PhraseInfoCtrl"
               :pageKey "phrases-monitor"
               :templateUrl "templates/phrases-monitor/phrase-info.html"})
    (ng-route-otherwise {:redirectTo "/"})))

;; controllers
(defn- navbar-ctrl [$scope]
  (aset $scope "isActive"
        (fn [page-key]
          (= (.. $scope -$parent -pageKey) page-key))))

(defn- phrases-monitor-ctrl [$scope]
  (let [source (js/EventSource. "/api/sse/new-phrases")]
    (doto source
      (.addEventListener "open" (fn [_] (.debug js/console "SSE opened")))
      (.addEventListener "error" (fn [_] (.close source)))
      (.addEventListener "new-phrase"
                         (fn [e]
                           (let [phrases (reader/read-string (.-data e))]
                             (.debug js/console (clj->js phrases))
                             (.$apply $scope #(aset $scope "phrases" (clj->js phrases))))))
      (.addEventListener "message"
                         (fn [e]
                           (.debug js/console "message" (.-data e)))))))

(defn- get-shared-vars [$scope]
  (js->clj (aget $scope "sharedVars")
           :keywordize-keys true))

(defn- update-shared-vars [$scope shared-vars]
  (aset (.-$parent $scope) "sharedVars"
        (clj->js shared-vars)))

(defn- search-phrases [$scope $http]
  (let [shared-vars (get-shared-vars $scope)
        query (:searchQuery shared-vars)
        page (:page shared-vars)
        min-dc (:minDC shared-vars)
        min-c (:minC shared-vars)
        select-type (:selectType shared-vars)
        show-junk (:showJunk shared-vars)
        url (str "/api/phrases/" select-type)]
    (.debug js/console (pr-str shared-vars))
    (.debug js/console "url" url)
    (aset $scope "searching" true)
    (-> (.post $http
               url
               {:query query
                :page page
                :min-dc min-dc
                :min-c min-c
                :show-junk show-junk}
               (edn-ajax-cfg {"Content-Type" "application/edn"}))
        (.success (fn [[phrases cnt]]
                    (.debug js/console "phrases" (clj->js phrases) cnt)
                    (aset $scope "phrases" (clj->js phrases))
                    (aset $scope "phraseCount" cnt)
                    (aset $scope "searching" false)))
        (.error (fn [data status]
                  (aset $scope "searching" false)
                  (.error js/console "phrases" status data))))))

(defn- phrase-list-ctrl [$scope $http]
  (aset $scope "searching" false)
  (aset $scope "search" (partial search-phrases $scope $http))
  (aset $scope "firstPage" (fn []
                             (let [shared-vars (get-shared-vars $scope)]
                               (update-shared-vars $scope (assoc shared-vars :page 1))
                               (search-phrases $scope $http))))
  (aset $scope "prevPage" (fn []
                            (let [shared-vars (get-shared-vars $scope)
                                  current-page (max (dec (:page shared-vars)) 1)]
                              (update-shared-vars $scope (assoc shared-vars :page
                                                                current-page))
                              (search-phrases $scope $http))))
  (aset $scope "nextPage" (fn []
                            (let [shared-vars (get-shared-vars $scope)
                                  current-page (inc (:page shared-vars))]
                              (update-shared-vars $scope (assoc shared-vars :page
                                                                current-page))
                              (search-phrases $scope $http)))))

(defn- phrase-info-ctrl [$scope $http $routeParams]
  (let [phrase-id (aget $routeParams "phraseId")
        url (str "/api/phrases/" phrase-id)]
    (.debug js/console "phrase-id" phrase-id)
    (-> (.get $http url (edn-ajax-cfg))
        (.success (fn [data]
                    (let [sorted-appearances (reverse
                                              (sort-by #(second %)
                                                       (:appearances data)))
                          data (assoc data :appearances sorted-appearances)]
                      (.debug js/console "phrase" (clj->js data))
                      (aset $scope "phrase" (clj->js data)))))
        (.error #(.error js/console "phrases" %2 %1)))))

;; main
(defn ^:export main []
  (doto (.module js/angular "broca-console" (array "ngRoute" "ui.bootstrap"))
    (.config (array "$routeProvider" routes))
    (.run (array "$rootScope" navbar-helper))
    #_(.factory "httpInterceptor" (array "$q" "$rootScope" http-interceptor))
    (.controller "NavbarCtrl" (array "$scope" navbar-ctrl))
    (.controller "PhrasesMonitorCtrl" (array "$scope" phrases-monitor-ctrl))
    (.controller "PhraseListCtrl" (array "$scope" "$http" phrase-list-ctrl))
    (.controller "PhraseInfoCtrl" (array "$scope" "$http" "$routeParams" phrase-info-ctrl))))
