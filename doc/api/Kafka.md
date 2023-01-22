# Kafka

## Topiki

- wyliczone wartości statystyk: `processedTest`, `processedProd`  
    Przesyła dane z `calculation`, żeby je zapisać w `statistics`.

- dane od skraperów: `inputTest`, `inputProd`  
    Przesyła dane zebrane przez `scraper` do przetworzenia w `statistics`.

- historeczyne dane: `backfeedTest`, `backfeedProd`  
    Przesyła historycze wartości statystyk z `statistics` do `calculations`, żeby można było policzyć nowe statystyki dla dodanych zmiennych.

Każda para topiców ma trzy groupy konsumentów: `<name>TestLocalGroup`, `<name>TestDockerGroup`, `<name>ProdDockerGroup`

## Komendy

Przed pisaniem do kafki musimy połączyć się z jej dockerem. Teoretycznie nie musimy bić w tym samym dockerze co kafka, aby do niej pisać, ale tylko tam mamy polecenia pozwalające robić to z poziomu konsoli.
```sh
docker exec -it kafka bash
```

Następnie uruchamiamy producera:
```sh
kafka-console-producer --broker-list kafka:9092 --topic processedTest --property "parse.key=true" --property "key.separator=:"
```

Potem podajemy recordy postaci: 
```json
test:{"name":"test","value":5,"time":"1970-01-01T00:00:00Z"}
```
```
// kafka-console-producer --broker-list kafka:9092 --topic inputTest --property "parse.key=true" --property "key.separator=@"
{"statistic":"statT","strategy":"stratT","subject":"subjT"}@{"name":{"statistic":"statT","strategy":"stratT","subject":"subjT"},"value":5,"time":"1970-01-01T00:00:00Z"}
```