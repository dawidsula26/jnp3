echo Waiting for Kafka to be ready... 
sleep 10

kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic processedTest

kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic inputTest

kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic backfeedTest


kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic processedProd

kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic inputProd

kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic backfeedProd


echo Created topics
