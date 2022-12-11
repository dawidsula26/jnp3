# syntax=docker/dockerfile:1

FROM python:3.8-slim-buster

WORKDIR /python-docker

COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt

ENV FLASK_RUN_HOST=0.0.0.0
EXPOSE 3000

COPY . .
CMD [ "python3", "-m", "flask", "run", "--port=3000", "--host", "0.0.0.0"]