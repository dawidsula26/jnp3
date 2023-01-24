# StockRabbit
### Stock statistics calculations and analysis tool

![Zrzut ekranu z 2023-01-24 08-19-42](https://user-images.githubusercontent.com/34144008/214241066-ed2fc919-1e46-4061-af4d-ca88255d78c2.png)

### What is StockRabbit?

It's a powerful app with distributed workers that calculate necessary statistics all the time, so you can view and analyze them! 

**Right now it supports**:
- Displaying stock value of company
- Calculating and displaying rolling mean and sum of company's stock value
- Filling last week data with augmented pseudo-random values, adding new data each minute

### What's inside?

We created microservices architecture with additional load balancing and cache to store statistics values recently displayed by the user. Architecture consists of:
- Front(frontend_worker) + Nginx loadbalancer + Redis Cache
- Wyniki(statistics_worker) + Nginx loadbalancer
- Kafka, KafkaStreams
- Obliczenia(calculations)
- Scraper

Specific API's and descriptions can be found in `doc/` directory

Our presentation (polish) with more information can be found here: https://docs.google.com/presentation/d/1NeY_ds-0lgPKPMuKfU92a4aIgXwGDdNmopUcuS2iQR4/edit?usp=sharing


### How to run it locally?

In `services` directory, do 

`chmod a+x run.sh`

`./run.sh`

Or specify manually number of front and statistics workers:

`sudo docker compose build`

`sudo docker-compose up --scale front_worker=2 --scale statistics_worker=2`

Web app should be visible at port `80`. It may take up to 10-15 minutes to download all necessary images. Scraper will fill last week with augmented data
