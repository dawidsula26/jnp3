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