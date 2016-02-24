(ns kokos.storage.server
  (:require [org.httpkit.server :as http-server]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.core :as compojure]
            [clojure.string :as string])
  (:use [kokos.storage.crud 
          :only (storage-place get-by-id exists? 
                               upsert delete search)])
  (:gen-class))

(def indexing-status (atom {}))

(defn indexing? [type]
  (and (not (nil? (get @indexing-status type))) 
       (get @indexing-status type)))


(defn start-indexing [type]
  (swap! indexing-status update-in [type] (fn [& args] true)))
(defn stop-indexing [type]
  (swap! indexing-status update-in [type] (fn [& args] false)))

(defn search-for-type [type-path id]
  (fn [request]
    {:headers {"Content-type" "application/edn"}
     :body (pr-str 
             (let [place (storage-place type-path)
                   query (if (= "_search" id) 
                           (:q (:params request))
                           (str "_id:" id))
                   results (try (place search query 0) 
                             (catch Exception e nil))]
                      results))}))
(defmacro wait-on-index [type & body]
  `(loop [status# (indexing? ~type)]
     (if-not status#
         (do
           (start-indexing ~type)
           (let [r# (try ~@body
                      (catch Exception e# false))]
             (stop-indexing ~type)
             (if-not r#
               (recur (indexing? ~type))
               r#)))
         (do
           (java.lang.Thread/sleep 1)
           (recur (indexing? ~type))))))

(defn upsert-for-type [type-path id]
  (fn [request]
    (let [place (storage-place type-path)]
      {:headers {"Content-type" "application/edn"}
       :body (pr-str (if (or (= "" id)
                             (= "0" id))
                        (wait-on-index (string/join "/" type-path) 
                                       (place upsert (:params request) true))
                        (wait-on-index (string/join "/" type-path)
                                       (place upsert (merge (:params request) {:_id id}) true))))})))

(defn delete-for-type [type-path id]
  (fn [request]
    (let [place (storage-place type-path)]
      {:headers {"Content-type" "application/edn"}
       :body (pr-str (wait-on-index type-path (place delete id)))})))

(defonce server (atom nil))
(compojure/defroutes web-routes
  (compojure/GET "/:_index/:_type/:id" [_index _type id] (search-for-type [_index _type] id))
  (compojure/POST "/:_index/:_type/:id" [_index _type id] (upsert-for-type [_index _type] id))
  (compojure/DELETE "/:_index/:_type/:id" [_index _type id] (delete-for-type [_index _type] id))
  (route/not-found "{:error \"not found\"}"))



(defn stop []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))




(defn re-start [port]
  (if (nil? @server)
    (do
      (println "Started rest server on " port)
      (reset! server (http-server/run-server (handler/site #'web-routes) {:port port})))
    (do
      (@server :timeout 100)
      (reset! server nil)
      (re-start port)
      (println "rest server started on port:" port)))
  nil)


(defn -main [& args]
  (re-start (Integer/parseInt (first args))))


