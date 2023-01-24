FROM python:3.8-bullseye
WORKDIR /usr/app/src
COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt
COPY scraper.py ./
CMD [ "python3", "-u", "./scraper.py"]