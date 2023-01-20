import pandas as pd
import json
import plotly
import plotly.express as px
import redis
import random
import requests
import yaml
from datetime import datetime
from flask import Flask,\
                  render_template,\
                  url_for,\
                  request,\
                  redirect

app = Flask(__name__, static_url_path='')

# Let's ignore the fact, that secret key is public - for simplicity :)
app.config['SECRET_KEY'] = "2a7c9ef01952a238eb"

STATISTICS_URL = 'http://statistics:8080'

def get_available_strategies():
   f = open('config.yaml')
   service_config = yaml.load(f)

   strategies_sets = service_config['available_strategies']
   return strategies_sets


def get_variable_values(variable_name,
                        date_begin,
                        date_end):
   data = {'variableName': variable_name,
           'startTime': None,
           'endTime': None}
   r = requests.post(url = STATISTICS_URL + '/reader/getVariable', json = data) 
   response = r.json()
   values = response['values']
   print(values)

   df = pd.DataFrame({
      'Date': [v['time'] for v in values],
      'USD': [v['value'] for v in values]
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