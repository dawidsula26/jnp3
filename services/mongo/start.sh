sleep 15

cd /init
mongosh "mongodb://mongo:27017" < init-collection.js
