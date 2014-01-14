(ns modern-cljs.test
  (:require-macros [hiccups.core :refer [hic-html]])
  (:require [shoreleave.remotes.http-rpc :refer [remote-callback]]))

(defn my-last [sel]
  (str "nehoray advah " sel)
  )

(defn run []
  (js/alert 
    (-> (js/jQuery "#odaya")
        (. html)
        (my-last))))
(defn adam []
  (let [a '(10 2 3 4 5 6 7 8 9 )]
    (.log js/console (first (map #(+ 3 %) a)))))

(defn symbols[]
  (.log js/console (str "aaa" " bbb"))
  )
(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (js/jQuery symbols)))
