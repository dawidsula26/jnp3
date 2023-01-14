import time

import redis 
from flask import Flask,\
                  render_template,\
                  url_for,\
                  request,\
                  redirect

app = Flask(__name__, static_url_path='')
cache = redis.Redis(host='redis', port=6379)

# Let's ignore the fact, that secret key is public - for simplicity :)
app.config['SECRET_KEY'] = "2a7c9ef01952a238eb"


def get_hit_count():
    retries = 5
    while True:
        try:
            return cache.incr('hits')
        except redis.exceptions.ConnectionError as exc:
            if retries == 0:
                raise exc
            retries -= 1
            time.sleep(0.5)


@app.route('/', methods=['GET'])
def index():
   count = get_hit_count()

   return render_template('index.html', hits=count)


if __name__ == "__main__":
   app.run(debug=True)