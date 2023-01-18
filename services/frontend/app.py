import time

import pandas as pd
import json
import plotly
import plotly.express as px
import redis
import random
import requests
from datetime import datetime
from flask import Flask,\
                  render_template,\
                  url_for,\
                  request,\
                  redirect

app = Flask(__name__, static_url_path='')

# Let's ignore the fact, that secret key is public - for simplicity :)
app.config['SECRET_KEY'] = "2a7c9ef01952a238eb"

# # REDIS EXAMPLE
# cache = redis.Redis(host='redis', port=6379)
# def get_hit_count():
#     retries = 5
#     while True:
#         try:
#             return cache.incr('hits')
#         except redis.exceptions.ConnectionError as exc:
#             if retries == 0:
#                 raise exc
#             retries -= 1
#             time.sleep(0.5)

STATISTICS_URL = 'https://localhost:8080'

def get_variable_values(variable_name,
                        selected_set,
                        date_begin,
                        date_end):
   print(date_begin)

   data = {'variableName': 'v', 'startTime': None, 'endTime': None}
   r = requests.post(url = STATISTICS_URL + '/reader/getVariable', data = data) 
   response = r.json()
   print(response)

   date_series = pd.date_range(datetime.strptime(date_begin, '%Y-%m-%d'),
                               datetime.strptime(date_end, '%Y-%m-%d'))
   
   values = [0]
   for _ in range(len(date_series) - 1):
      today_value = random.uniform(-1.7, 2)
      values.append(values[-1] + today_value)

   df = pd.DataFrame({
      'Date': date_series,
      'USD': values,
   })
   return df

# Dashboard
@app.route('/', methods=['GET'])
def index():
   return render_template('index.html')

# specific variable
@app.route('/variable', methods=['GET', 'POST'])
def variable():
   if request.method == 'POST':
      selected_strategy = request.form.get('selected_strategy')
      selected_set = request.form.get('selected_set')
      date_begin = request.form.get('date_begin')
      date_end = request.form.get('date_end')
   else:
      return redirect('/')

   df = get_variable_values(selected_strategy, selected_set, date_begin, date_end)
   fig = px.line(df, x='Date', y='USD')
   graphJSON = json.dumps(fig, cls=plotly.utils.PlotlyJSONEncoder)

   return render_template('variable.html', graphJSON=graphJSON,
                                           strat_name=selected_strategy,
                                           set_name=selected_set,
                                           strat_desc='Easy strategy for buying a stock when its price is higher than last week')
      

if __name__ == "__main__":
   app.run(debug=True)