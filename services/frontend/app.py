import pandas as pd
import json
import os
import plotly
import plotly.express as px
import redis
import random
import requests
import yaml
from dotenv import load_dotenv
from datetime import datetime
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
   f = open('config.yaml')
   service_config = yaml.load(f)

   strategies_sets = service_config['available_strategies']
   return strategies_sets


def get_variable_values(variable_name,
                        date_begin,
                        date_end):

   date_begin = datetime.strptime(date_begin, '%Y-%m-%d')
   date_end = datetime.strptime(date_end, '%Y-%m-%d')
      
   data = {'variableName': {'statistic': 'statT', 'strategy': 'stratT', 'subject': 'subjT'},
           'startTime': date_begin.strftime('%Y-%m-%dT%H:%M:%SZ'),
           'endTime': date_end.strftime('%Y-%m-%dT%H:%M:%SZ')}

   response = cache.get(str(data))

   if response is None:        
      r = requests.post(url = app.config['STATISTICS_URL'] + '/reader/getVariable', json = data) 
      response = r.json()
      cache.set(str(data), response)

   values = response['values']
   print(values)

   df = pd.DataFrame({
      'Date': [v[1] for v in values],
      'USD': [v[0] for v in values]
   })
   return df


# Dashboard
@app.route('/', methods=['GET'])
def index():
   strategies_sets = get_available_strategies()
   return render_template('index.html', strats=strategies_sets)


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

   df = get_variable_values(selected_strategy, date_begin, date_end)
   fig = px.line(df, x='Date', y='USD')
   graphJSON = json.dumps(fig, cls=plotly.utils.PlotlyJSONEncoder)

   strategies_sets = get_available_strategies()
   return render_template('variable.html', strats=strategies_sets,
                                           graphJSON=graphJSON,
                                           strat_name=selected_strategy,
                                           set_name=selected_set,
                                           strat_desc='DESCRIPTION')
      

if __name__ == "__main__":
   app.run(debug=True)