Services
===============================================================================

Front
-------------------------------------------------------------------------------

Service used to create responses to end user by sending requests to other services and processing responses. 


### API

I will not write down all posible request, because they depend on the desing of frontend. One request is listed, because it is a base for all other non-trival requests, but it will not be available itself. 

- `REQUEST SHOW_STATS: stat_names: [StatName] -> [(StatName, [(Time, Value)])]`  
Returns histories of values of listed statistics. It is calculated by making a series of requests to `Statistics` service. We do not guarantee that amount of displayed values is consistent. This request may return things like `{"a": [6, 8], "b": [3, 2], "a/b": [2, 4, 3]}` and it is OK as long as calculated values are actually correct. 


### Scaling

This is a stateless component that does not make use of any database, so it can be easily scaled just by adding additional nodes.  


### Load balancing

This is a simple stateless request-response server so there are no additional requirements for load balancing. We can use generic solution that will most likely be presented during future classes. 


### Caching

We should be able to cache answers to requests as they are quite similar, but it may not be neccessary as there will also be caching in front of `Statistics` service and we do not do any expensive computations here. 

We need to remember that cached responses cannot live long, because statistics are constantly updated and we do not want present outdated information. We can safely store responses for $1/r$ and storage times around $O(1/r)$ could prove reasonable balance between latency and preformance. 



Statistics
-------------------------------------------------------------------------------

Service used to hold information about values of the tracked statistics. It serves as a connector between processing segment where values are calculated and front segment where they are displayed. This component exists to provide quick access to all values of given statistic. Taking values directly from asynchronous queue would most likely be slower and it would put unncessary load on the queue.  


### API

- `REQUEST GET_STAT: StatName -> [(Time, Value)]`  
Returns values of given statistic based on data stored in the database. It returns data based on what is currently available in the database. It does not wait for any missing values. 

- `REQUEST APPEND_STAT: StatName, [(Time, Value)] -> Unit`  
Adds new values to the database. If some values already exist than they are ignored and `WARNING` is loged.


### Scaling

Read accesses can easily be scaled without any upper limit, because they can be satisfied with any information that is recovered from the database in a very simple transaction (basicaly `SELECT * FROM name`). 

Write accesses require some considerations but there is nothing problematic here, because there is no synchronization needed between different writes or reads and writes. This seems like a common problem that has been already solved, so we will just implement a popular solution. 


### Load balancing

We will use some basic load balancing. All operations are stateless except for modifying database, so there should be no need for anything special


### Database

We will access our database by making request like get all variables for given key and add new variables for given key. Read requests will likely be more common than write requests even when we consider the fact that read request are much bigger. 

There is no synchronization needed so we only need to care about read and write efficiency of the database. 


### Caching

We can cache read requests and this may significantly reduce loads when there is a lot of frontend users. As with `Front` service, responses cannot be cached for too long because underlying data will change.  

