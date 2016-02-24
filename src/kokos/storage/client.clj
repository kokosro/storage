(ns kokos.storage.client
  (:require [org.httpkit.client :as http]
            [clojure.edn :as edn])
  (:refer-clojure :exclude [update])
  (:gen-class))


(def *server-address* (atom ["" nil]))

(defn set-server! [server-address]
  (reset! *server-address* server-address))

(defmacro with-server [server-address & body]
  `(binding [*server-address* ~server-address]
     ~@body))

(defn create [type-path data]
  (let [response (http/post (str (first @*server-address*) "/" type-path "/0")
                            {:form-params data
                             :basic-auth (second @*server-address*)})
        response-body (try (apply str (map char (.bytes (:body @response))))
                       (catch Exception e nil))]
    (if (nil? response-body)
      false
      (edn/read-string response-body))))

(defn update [type-path update-info id]
  (let [response (http/post (str (first @*server-address*) "/" type-path "/" id)
                            {:form-params update-info
                             :basic-auth (second @*server-address*)})
        response-body (try (apply str (map char (.bytes (:body @response))))
                       (catch Exception e nil))]
    (if (nil? response-body)
      false
      (edn/read-string response-body))))

(defn search [type-path  & {:keys [id query] :or {id nil
                                                  query "*:*"}}]
  (let [response (if-not (nil? id) 
                   (http/get (str (first @*server-address*) "/" type-path "/" id)
                             {:basic-auth (second @*server-address*)})
                   (http/get (str (first @*server-address*) "/" type-path "/_search")
                             {:query-params {:q query}
                              :basic-auth (second @*server-address*)}))]
    (edn/read-string (try (apply str (map char (.bytes (:body @response))))
                       (catch Exception e nil)))))
