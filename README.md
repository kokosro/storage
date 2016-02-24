# storage

A Lucene simple storage. With rest server and CRUD capabilities.

## Usage
###Starting a server
```clojure
(ns a-simple-storage-server
(:require [kokos.storage.server :as server])
(:gen-class))
;;starting a server on port 12345
;;server address will be http://localhost:12345
(server/re-start 12345)
```
###Using the client
```clojure
(ns a-simple-storage-client
(:require [kokos.storage.client :as client])
(:gen-class))

;;with the started server we can use the client to CRUD it
;;the second value of the server is the basic auth, 
;;add nil or leave blank if you don't use one
;;or add a ["user" "pass"] as the value
(client/with-server ["http://localhost:12345"]
	(client/create "index/type" {:value "a value"}))
;;this will return the created object - with _id and version
(client/with-server ["http://localhost:12345"]
	(client/update "index/type" {:value "a value"} "some-id"))

;;searching takes :query or :id. 
;;if not is supplied it's doing a general query
;;*:*
```

for more info on lucene query syntax
go to  [lucene tutorial](http://www.lucenetutorial.com/lucene-query-syntax.html)

```clojure
(client/with-server ["http://localhost:12345"]
	(client/search "index/type" :query "a value"))

(client/with-server ["http://localhost:12345"]
	(client/search "index/type" :id "some-id"))

;;deleting is easy
(client/with-server ["http://localhost:12345"]
	(client/delete "index/type" "some-id"))

```

##Running locally, without a server?
```clojure
(ns running-local
	(:require [kokos.storage.crud :as storage])
(:gen-class))

(def a-type (storage/storage-place ["index-name" "value-type"]))

(a-type storage/upsert {:value "some-value" :_id "specific-id-or-no-id"})
(a-type storage/search "_id:specific-id-or-no-id" 0)
(a-type storage/get-by-id "specific-id-or-no-id")
(a-type storage/delete "specific-id-or-no-id")

```



## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
