Controller
===============================================================================

New service responsible for basic maintenance tasks like stopping and restarting variable calculation or inspecting how much data has already been processed. It will also track all existing keys also modifying them. 


## API

It needs to be decided whether it should serve its own frontend or just answer requests from `Front` service. It also need to have its functionalities specified, so for now there is no api overview. Expected requests: 

- `REQUEST IS_RUNNING: [StatName] -> [(StatName, Bool)]`  
Checks which statistics are calculated at the moment and which statistics are disabled at the moment. 

- `REQUEST STAT_START: [StatName] -> ()`  
Start of calculations for given stat. If necessary new task will be created to calculate past values for some variables.

- `REQUEST STAT_STOP: [StatName] -> ()`  
Stop calculations for given stat. 

- `REQUEST STAT_DATA: [StatName] -> [Data]`  
Get data about processing of some statistic, eg. how much is calculated, are there any gabs, were there any errors, ...

- `REQUEST ADD_KEY: String -> Unit`  
Adds new and informs other services about the change. Most likely it will only need `Front` to change. 

- `REQUEST REMOVE_KEY: String -> Unit`
Removes key and informs other services about the change.



## Scaling / Load balancing

I do not think that there is any sense in making this component scalable. Someone making a request would already be high workload. Furthermore, 2 out of 4 available request cannot be done concurrently, because they require restarting stream processing. Remaining 2 are stateless and, if necessary, could be calculated on multiple nodes.


## Database

We need some key-value database to keep information about statistics.


## Caching

There will be no caching. Requests are rare and we want to be sure that their results are up to date. 


## Tools
The best choice for this service is just to copy technological stack from `Front`.



Calculations &ndash; changes
===============================================================================

## Processing

Additionally to what is already done, we need to keep some runtime statistics about calculated statistics, eg. number of runtime errors. Whenever we get a request to change what is calculated, we just stop calculations and start them again with modified definitions. 


## API

- `REQUEST STAT_START: [StatName] -> Unit`  
Starts processing given statistics.

- `REQUEST STAT_STOP: [StatName] -> Unit`  
Stops processing given statistics.

- `REQUEST STAT_DATA: [StatName] -> [Data]`  
Sends collected data about statistics. 

- `REQUEST CALCULATE_OLD: StateName, Time -> Unit`  
Calculates historical values for given statistic then processes values using incoming.



Statistics &ndash; changes
===============================================================================

## API

- `REQUEST STAT_DATA: [StatName] -> [Data]`  
Returns data about how many values of given statistic are stored in the database.
