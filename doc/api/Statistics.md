# Statistics

## Wersje

Wersja aplikacji jest zależna od zmiennej środowiskowej `SETUP_VERSION`. Dostępne są trzy wartości:

- `local` - domyślna wersja; używana przy lokalnym korzystaniu z aplikacji i zasobów z dockerów
- `dockerTest` - używana gdy aplikacja jest uruchamiana z docker-compose i korzysta ze środowiska testowego
- `dockerProd` - używane gdy aplikacja jest uruchamiana z docker-compose i korzysta ze środowiska "testowego" (tego, którego planujemy użyć przy prezentacji)

## API

### Manager

- `POST manager/initializeDatabase`  
    Inicjuje bazę danych i tworzy kolekcje typu timeseries

- `POST manager/removeDatabase`
    Usuwa istniejącą bazę danych

- `POST manager/fillWithGarbage (TEST ONLY)`
    Wypełnia bazę danych niezdefiniowanymi i poprawnymi wartościami

### Reader

- `POST reader/getVariable`
    ```json
    Request: {
      "variableName": "v",
      "startTime": null,
      "endTime": null
    }
    Request: {
      "variableName": "v",
      "startTime": "1970-01-01T00:00:00Z",
      "endTime": "1970-01-01T00:00:12Z"
    }
    Response {
      "variableName": "t",
      "values": [
        {
          "name": "t",
          "value": 4.87,
          "time": "1970-01-01T00:00:06Z"
        },
        ...
      ]
    }
    ```