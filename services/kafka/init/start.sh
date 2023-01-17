sleep 15

kafka-topics --bootstrap-server kafka:9092 \
             --create \
             --topic quickstart
