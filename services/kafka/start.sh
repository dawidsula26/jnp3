echo Waiting for Kafka to be ready... 
sleep 10

kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic toStatisticsTest


kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic fromStatisticsTest


kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic toStatisticsProd


kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic fromStatisticsProd


kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic toCalculationsTest


kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --if-not-exists \
             --partitions 1 \
             --replication-factor 1 \
             --topic toCalculationsProd


echo Created topics
