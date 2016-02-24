# kokos.storage

A Lucene simple storage. With rest server and CRUD capabilities.

## Usage

add this for lein project dependencies  
[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.kokos/storage.svg)](https://clojars.org/org.clojars.kokos/storage)


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

(client/set-server! ["http://localhost:12345"])

(client/create "index/type" 
	{:value "a value"})

;;this will return the created object - with _id and version
(client/update "index/type" 
	"some-id"
	{:value "a value"} )

;;searching takes :query or :id. 
;;if not is supplied it's doing a general query
;;*:*
```

for more info on lucene query syntax
go to  [lucene tutorial](http://www.lucenetutorial.com/lucene-query-syntax.html)

```clojure
(client/search "index/type" 
	:query "a value")
(client/search "index/type" 
	:id "some-id")
(client/search "index/type")

```
deleting is easy

```clojure
(client/delete "index/type" 
		"some-id")

```

###Running locally, without a server?
#####beta
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

Distributed under the Eclipse Public License version 1.0 (found in LICENSE).
