(ns kokos.storage.client
  (:require [org.httpkit.client :as http]
            [clojure.edn :as edn])
  (:refer-clojure :exclude [update])
  (:gen-class))


(def server-address-url (atom ["" nil]))

(defn set-server! [server-address]
  (reset! server-address-url server-address))

(defmacro with-server [server-address & body]
  `(binding [server-address-url ~server-address]
     ~@body))

(defn create [type-path data]
  (let [response (http/post (str (first @server-address-url) "/" type-path "/0")
                            {:form-params data
                             :basic-auth (second @server-address-url)})
        response-body (try (apply str (map char (.bytes (:body @response))))
                       (catch Exception e nil))]
    (if (nil? response-body)
      false
      (edn/read-string response-body))))

(defn update [type-path id update-info]
  (let [response (http/post (str (first @server-address-url) "/" type-path "/" id)
                            {:form-params update-info
                             :basic-auth (second @server-address-url)})
        response-body (try (apply str (map char (.bytes (:body @response))))
                       (catch Exception e nil))]
    (if (nil? response-body)
      false
      (edn/read-string response-body))))

(defn search [type-path  & {:keys [id query] :or {id nil
                                                  query "*:*"}}]
  (let [response (if-not (nil? id) 
                   (http/get (str (first @server-address-url) "/" type-path "/" id)
                             {:basic-auth (second @server-address-url)})
                   (http/get (str (first @server-address-url) "/" type-path "/_search")
                             {:query-params {:q query}
                              :basic-auth (second @server-address-url)}))]
    (edn/read-string (try (apply str (map char (.bytes (:body @response))))
                       (catch Exception e nil)))))
