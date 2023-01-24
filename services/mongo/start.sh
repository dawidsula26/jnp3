sleep 15
cd /init
mongosh "mongodb://configsvr01:27017" < init-configserver.js
mongosh "mongodb://shard01-a:27017" < init-shard01.js
mongosh "mongodb://shard02-a:27017" < init-shard02.js

sleep 10
mongosh "mongodb://mongo:27017" < init-router.js
mongosh "mongodb://mongo:27017" < init-collection.js
