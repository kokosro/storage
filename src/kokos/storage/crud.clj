(ns kokos.storage.crud
  (:require [clucy.core :as clucy]
            [clucy.analyzers :as analyzers]
            [digest]
            [clojure.string :as string]
            [clojure.data.json :as json])
  (:gen-class))


(def ^:dynamic *storage-root-path* (java.lang.System/getProperty "user.dir"))

(def ^:dynamic *storage* "kokostore")

(defn make-path 
  "makes path from a vector of strings"
  [& path-parts]
(let [the-path (string/join "/" (into [*storage-root-path* *storage*] path-parts))]
  the-path))


(defn make-type-analyzer 
  "proxying making analyzer from meta info"
  [type-analyzer-info]
  (let [analyzer-info (analyzers/make-analyzer :class (get type-analyzer-info :class :standard))]
    analyzer-info))

(defn storage-place 
  "Returns a type maker inside the main *storage*."
  [for-type]
      (let [type-path (apply make-path (if (vector? for-type) 
                                         (into [] for-type)
                                         [for-type] ))
            type-analyzer (make-type-analyzer (meta for-type))]
        (binding [clucy/*analyzer* type-analyzer]
          (fn [do-this & with-these]
            (apply do-this (into [(clucy/disk-index type-path)] with-these))))))



(defn assign-id [to-obj]
  (let [id (digest/sha1 (.toString (System/currentTimeMillis)))]
    (if-not (contains? to-obj :_id)
      (assoc to-obj :_id id)
      to-obj)))
(defn get-by-id [index id]
  (let [existing-element (first (try (clucy/search index (str "_id:" id) 1) (catch Exception e nil)))]
    existing-element))

(defn exists? [index id]
  (not (nil? (get-by-id index id))))

(defn delete [index id]
  (clucy/search-and-delete index (str "_id:" id)))

(defn upsert [index data & no-return]
  (let [data-with-id (if (contains? data :_id) data (assign-id data))
        existing-element (get-by-id index (:_id data))
        p-data (merge existing-element
                      data-with-id
                      {:_ver (if (contains? existing-element :_ver) 
                               (inc (java.lang.Integer/parseInt 
                                      (:_ver existing-element)))
                               1)})
        _ (delete index (:_id p-data))
        _ (clucy/add index p-data)]
    (if (= 0 (count no-return))
      (:_id data-with-id)
      (get-by-id index (:_id p-data)))))

(defn search [index search-query page]
  (let [results (clucy/search index search-query 10000000 
                              :page page 
                              :results-per-page 20)]
    (merge (meta results)
           {:_hits (doall (map (fn [hit] 
                                 (merge {:_score (get (meta hit) 
                                                      :_score 0)} hit)) 
                               results))})))





