import numpy as np
from datetime import datetime, timedelta
from time import sleep
from kafka import KafkaProducer
import json

LINEAR_COEFF = 0.05
NOISE_AMPLITUDE = 30
SIN_AMPLITUDE = 1
SIN_LENGTH = 500

sleep(30)

# adress to kafkabroker
producer = KafkaProducer(bootstrap_servers='kafka:9092')

def generate_next_value(previous_value, minute):
    new_value = previous_value + LINEAR_COEFF
    new_value += np.random.uniform(-NOISE_AMPLITUDE, NOISE_AMPLITUDE)

    # sin_length = 10000 means that 10000 minutes = pi
    x = (minute / SIN_LENGTH) % (2*np.pi)
    sin = np.sin(x)

    new_value += sin * SIN_AMPLITUDE
    
    return new_value


def push_val_to_kafka(cur_date, value, statistic, subject):
    d_key = { "statistic": statistic, "strategy": None, "subject": subject}
    d_value = {"name": d_key, "value":value, "time": cur_date.strftime('%Y-%m-%dT%H:%M:%SZ')}

    d_key = json.dumps(d_key).encode('utf-8')
    d_value = json.dumps(d_value).encode('utf-8')
    producer.send('inputTest', value=d_value, key=d_key)


last_val1 = 0
last_val2 = 0
minute = 0

start_date = datetime.now() - timedelta(days=7)
cur_date = start_date

while cur_date < datetime.now():
    v1 = generate_next_value(last_val1, minute)
    v2 = generate_next_value(last_val2, minute)

    minute += 1
    last_val1 = v1
    last_val2 = v2

    push_val_to_kafka(cur_date, v1, 'stockValue', 'Macrohard')
    push_val_to_kafka(cur_date, v2, 'stockValue', 'Telsa')
    cur_date += timedelta(minutes=1)

print('Filled with garbage!')

while True:
    if cur_date < datetime.now():
        v1 = generate_next_value(last_val1, minute)
        v2 = generate_next_value(last_val2, minute)
        minute += 1
        last_val1 = v1
        last_val2 = v2

        push_val_to_kafka(cur_date, v1, 'stockValue', 'Macrohard')
        push_val_to_kafka(cur_date, v2, 'stockValue', 'Telsa')
        print('Values pushed to kafka')
        cur_date += timedelta(minutes=1)

    sleep(1)