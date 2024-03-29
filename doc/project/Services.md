Front
===============================================================================

Service used to create responses to end user by sending requests to other services and processing responses. 


## API

I will not write down all possible request, because they depend on the design of frontend. Two requests are listed, because they are a base for all other nontrivial requests, but they will likely not be available themselves. 

- `REQUEST SHOW_VARS: [VarName] -> [(VarName, [Value])]`  
Returns histories of values of listed variables. We do not guarantee that the number of displayed values is consistent. This request may return things like `{"a": [6, 8], "b": [3, 2], "a/b": [2, 4, 3]}` and it is OK as long as calculated values are actually correct.

- `REQUEST SHOW_UPDATES: [(VarName, Time)] -> [(VarName, [Value])]`  
Returns histories of given variables from given timestamp forwards.


## Scaling

This is a stateless component that does not make use of any database, so it can be easily scaled just by adding additional nodes.  


## Load balancing

This is a simple stateless request-response server, so there are no additional requirements for load balancing. We can use a generic solution that will most likely be presented during future classes. 


## Caching

We should be able to cache answers to requests as they are quite similar, but it may not be necessary as there will also be caching in front of `Statistics` service, and we do not do any expensive computations here. 

We need to remember that cached responses cannot live long, because statistics are constantly updated, and we do not want to present outdated information. Responses may be stored for as long as time between updates, but not much longer. We may decide that frontend always calls `SHOW_VARS` and `SHOW_UPDATES` immediately after, then we should be able to cache `SHOW_VARS` requests almost indefinitely. 


## Tools
This is quite generic web server, so we have great freedom in choosing tools. I would suggest trying Go for this service. It is generally liked and is considered good language for creating web servers. It would also be a good opportunity to try it out, because it is a simple component and not much can go wrong, even when we do not have experience with Go. 

Alternatively, if we to use something we have experience with, then Python and Flask are a great choice. 



Statistics
===============================================================================

Service used to hold information about values of the tracked statistics. It serves as a connector between the processing segment, where values are calculated, and the front segment, where they are displayed. This component exists to provide quick access to all values of a given statistic. Taking values directly from an asynchronous queue would most likely be slower, and it would put an unnecessary load on the queue.  


## API

- `REQUEST GET_VAR: VarName -> [Value]`  
Returns timestamp of first value and list of values of given statistic based on data stored in the database. It returns data based on what is currently available in the database. It does not wait for any missing values. 

- `REQUEST GET_UPDATE: VarName, Time -> [Value]`  
Returns list of values of given variable with timestamps after given time.

- `ASYNC INPUT APPEND_VAR: (VarName, Value)`  
Adds new value for given variable. If a value with a given timestamp already exists, it is ignored.

## Scaling

Read accesses can easily be scaled without any upper limit, because they can be satisfied with any information that is recovered from the database in a very simple transaction (basically `SELECT * FROM vars WHERE key = name AND time > t`). 

Writes may be problematic, but they should not be very difficult. Reads can mix with writes in any way they want, and writes are guaranteed to not be contradictory (and even when they are, we said that then we can choose arbitrary one).


## Load balancing

We will use some basic load balancing. All operations are stateless except for writing to the database, but it handled by asynchronous queue. 


## Database

We will be making two kinds of requests to the database

`SELECT * FROM vars WHERE name = n AND time >= t`

`INSERT (name, time, value) INTO vars`

and we can guarantee that inserted data will have times more or less in order. Maybe it can be done with a simple key-value store, but most likely it will need something more capable. Luckily, we do not require any transactional guarantees.


## Caching

We can cache read requests, and this may significantly reduce loads when there are a lot of frontend users. As with `Front` service, responses cannot be cached for too long because underlying data will change.  


## Tools

I have not found the right database yet, and all other choices will be dependent on the chosen database and existing libraries for it. 



Calculations
===============================================================================

Service responsible for handling computation of statistics. It must provide nice programmer API for declaring new statistics. 


## Processing

We will use a prepared framework for processing stream data. It will take definitions of statistics to calculate in form like

```json
{
    name: "VarName"
    function: f
    requirements: ["var1", "var2", "var3"]
}
```

convert it into definitions of stream computations and additional element responsible for feeding data and sending it further to `Statistics`. Then it start streams and run them until stopped.


## API

- `ASYNC INPUT PROCESS_VAR: (VarName, Value)`   
Receives values from scrappers and processes them.


- `ASYNC OUTPUT APPEND_VAR: (VarName, Value)`   
Place where computed results are sent to next component. 

- `STOP`
There should be some way of gracefully stopping this component to make some changes. 


## Scaling / Load Balancing / Database
This service is just a wrapper for a stream processing framework, so the framework handle all of it. 


## Tools

Kafka Streams looks like the best solution. It can handle stateful computations that will be useful, it supports newest version of Scala 2 (neither Kafka Streams nor Flink support Scala 3), and has some options that may useful if we decide to implement external calculations extension. Of course the language is Scala. 


Collector
===============================================================================

Service used to collect data and send it for processing. 


## API

- `ASYNC OUTPUT PROCESS_VAR: (VarName, Time, Value)`  
Sends variable for processing.


## Scaling

We can independently collect values for different variables. Technically, we can also concurrently collect values for one variable, but we do not want to do it, because it may introduce out-of-order data, which, while manageable, is undesired. 


## Load balancing

We statically assign different variables to different nodes. Collecting data should be stateless, so we can move variables between nodes, but most likely it will be an expensive operation. 


## Tools

To be decided when we have decided on what and how will be collected.


## Note 

I actually don not care about this component. It could just be mocked data and project would not really suffer. 
