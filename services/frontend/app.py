import pandas as pd
import json
import os
import plotly
import plotly.express as px
import redis
import random
import requests
from dotenv import load_dotenv
from datetime import datetime
import socket
from copy import deepcopy
from flask import Flask,\
                  render_template,\
                  url_for,\
                  request,\
                  redirect
from flask_caching import Cache  # Import Cache from flask_caching module

load_dotenv()

app = Flask(__name__, static_url_path='')
app.config.update(
    CACHE_TYPE = os.environ['CACHE_TYPE'],
    CACHE_REDIS_HOST = os.environ['CACHE_REDIS_HOST'],
    CACHE_REDIS_PORT = os.environ['CACHE_REDIS_PORT'],
    CACHE_REDIS_DB = os.environ['CACHE_REDIS_DB'],
    CACHE_REDIS_URL = os.environ['CACHE_REDIS_URL'],
    CACHE_DEFAULT_TIMEOUT = os.environ['CACHE_DEFAULT_TIMEOUT'],
    STATISTICS_URL = os.environ['STATISTICS_URL'],
    SECRET_KEY = "2a7c9ef01952a238eb"
)

cache = Cache(app)  # Initialize Cache

def get_available_strategies():
   f = open('config.json')
   service_config = json.load(f)

   strategies_sets = service_config['available_variables']

   strategies = []
   for sset in strategies_sets:
      strategy = f'{sset[2]}-{sset[1]}-{sset[0]}'
      strategies.append(strategy)

   return strategies


def fetch_values_from_statistics(data):
   r = requests.post(url = app.config['STATISTICS_URL'] + '/reader/getVariable', json = data) 
   response = r.json()
   values = response['values']
   values = [(v[0], to_datetime(v[1])) for v in values]
   values.sort(key=lambda x: x[1])

   return values


def to_datetime(datestr):
   return datetime.strptime(datestr, '%Y-%m-%dT%H:%M:%SZ')


def get_variable_values(selected_strategy,
                        date_begin,
                        date_end):

   date_begin = datetime.strptime(date_begin, '%Y-%m-%d')
   date_end = datetime.strptime(date_end, '%Y-%m-%d')

   subject = selected_strategy.split('-')[0]
   strat = selected_strategy.split('-')[1]
   stat = selected_strategy.split('-')[2]

   strat = None if strat == 'None' else strat
   stat = None if stat == 'None' else stat
   subject = None if subject == 'None' else subject
      
   data = {'variableName': {'statistic': stat, 'strategy': strat, 'subject': subject},
           'startTime': date_begin.strftime('%Y-%m-%dT%H:%M:%SZ'),
           'endTime': date_end.strftime('%Y-%m-%dT%H:%M:%SZ')}

   # get cached data
   cache_key = str(data['variableName'])
   values = cache.get(cache_key)

   # nothing is cached:
   if values is None or len(values) == 0:
      values = fetch_values_from_statistics(data)
      cache.set(cache_key, values)
   # cached data found:
   else:
      lowest_cached_date = values[0][1]
      highest_cached_date = values[-1][1]

      print('Found cached values, cache from to:', flush=True)
      print(lowest_cached_date, flush=True)
      print(highest_cached_date, flush=True)

      # # get intersection of cached data and requested
      changed_cache = False

      # get missing data before cached
      if date_begin < lowest_cached_date:
         data2 = deepcopy(data)
         data2['endTime'] = lowest_cached_date.strftime('%Y-%m-%dT%H:%M:%SZ')
         missing_values = fetch_values_from_statistics(data2)
         values = missing_values + values
         changed_cache = True

      # get missing data after cached
      if highest_cached_date < date_end:
         data2 = deepcopy(data)
         data2['startTime'] = highest_cached_date.strftime('%Y-%m-%dT%H:%M:%SZ')
         missing_values = fetch_values_from_statistics(data2)
         values = values + missing_values
         changed_cache = True
      
      if changed_cache:
         cache.set(cache_key, values)

      # get selected area
      values = [v for v in values if date_begin <= v[1] <= date_end]

   df = pd.DataFrame({
      'Date': [v[1] for v in values],
      'USD': [v[0] for v in values]
   })
   return df


# Dashboard
@app.route('/', methods=['GET'])
def index():
   strategies_sets = get_available_strategies()
   return render_template('index.html',
                          strats=strategies_sets,
                          date_begin='2023-01-01',
                          date_end='2023-01-23',
                          container=socket.gethostname())


# specific variable
@app.route('/variable', methods=['GET', 'POST'])
def variable():
   if request.method == 'POST':
      selected_strategy = request.form.get('selected_strategy')
      date_begin = request.form.get('date_begin')
      date_end = request.form.get('date_end')
   else:
      return redirect('/')

   df = get_variable_values(selected_strategy, date_begin, date_end)
   fig = px.line(df, x='Date', y='USD')
   graphJSON = json.dumps(fig, cls=plotly.utils.PlotlyJSONEncoder)

   strategies_sets = get_available_strategies()
   return render_template('variable.html', strats=strategies_sets,
                                           graphJSON=graphJSON,
                                           strat_name=selected_strategy,
                                           date_begin=date_begin,
                                           date_end=date_end,
                                           strat_desc='DESCRIPTION')
      

if __name__ == "__main__":
   app.run(debug=True)