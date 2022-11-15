Front
===============================================================================

Service used to create responses to end user by sending requests to other services and processing responses. 


## API

I will not write down all possible request, because they depend on the design of frontend. Two requests are listed, because they are a base for all other nontrivial requests, but they will likely not be available themselves. 

- `REQUEST SHOW_VARS: [VarName] -> [(VarName, Time, [Value])]`  
Returns histories of values of listed variables. The response includes the timestamp of the first value, and all values are chronologically, and there are no gabs between them. It is calculated by making a series of requests to `Statistics` service. We do not guarantee that the number of displayed values is consistent. This request may return things like `{"a": [6, 8], "b": [3, 2], "a/b": [2, 4, 3]}` and it is OK as long as calculated values are actually correct.

- `REQUEST SHOW_UPDATES: [(VarName, Time)] -> [(VarName, [Value])]`  
Returns histories of given variables from given timestamp forwards. We only send values and assume that frontend should be able to calculate their timestamps, as long as they are chronologically and without gabs.


## Scaling

This is a stateless component that does not make use of any database, so it can be easily scaled just by adding additional nodes.  


## Load balancing

This is a simple stateless request-response server, so there are no additional requirements for load balancing. We can use a generic solution that will most likely be presented during future classes. 


## Caching

We should be able to cache answers to requests as they are quite similar, but it may not be necessary as there will also be caching in front of `Statistics` service, and we do not do any expensive computations here. 

We need to remember that cached responses cannot live long, because statistics are constantly updated, and we do not want to present outdated information. We can safely store responses for as long as time between updates, but not much longer. We may decide that frontend always calls `SHOW_VARS` and `SHOW_UPDATES` immediately after, then we should be able to cache `SHOW_VARS` requests almost indefinitely. 


## Tools
This is quite generic web server so we have great freedom in choosing tools. I would suggest trying Go for this service. It is generaly liked and is considered good language for creating web servers. It would also be good oportunity to try it out, because it is a simple component and not much can go wrong, even when we do not have experience with Go. 

Alternatively, if we to use something we have experience with, then Python and Flask are a great choice. 



Statistics
===============================================================================

Service used to hold information about values of the tracked statistics. It serves as a connector between the processing segment, where values are calculated, and the front segment, where they are displayed. This component exists to provide quick access to all values of a given statistic. Taking values directly from an asynchronous queue would most likely be slower, and it would put an unnecessary load on the queue.  


## API

- `REQUEST GET_VAR: VarName -> (Time, [Value])`  
Returns timestamp of first value and list of values of given statistic based on data stored in the database. It returns data based on what is currently available in the database. It does not wait for any missing values. 

- `REQUEST GET_UPDATE: VarName, Time -> [Value]`  
Returns list of values of given variable with timestamps after given time. Values are sorted chronologically and there are no gabs, so there is no need to include timestamps. 

- `ASYNC INPUT APPEND_VAR: VarName, Time, Value -> ()`  
Adds new value for given variable. If a value with a given timestamp already exists, it is ignored. Timestamp is included, because we may have gabs in calculated values.

## Scaling

Read accesses can easily be scaled without any upper limit, because they can be satisfied with any information that is recovered from the database in a very simple transaction (basically `SELECT * FROM vars WHERE key = name AND time > t`). 

Writes may be problematic, but they should not be very difficult. Request can mix with writes in any way they want, and writes are guaranteed to not be contradictory. If we assume that trying to write value, that already exists, is ignored, then we should not need any synchronization between writes.  


## Load balancing

We will use some basic load balancing. All operations are stateless except for writing to the database, but it handled by assynchronous queue. 


## Database

We will be making two kinds of requests to the database

`SELECT * FROM vars WHERE name = n AND time >= t`

`INSERT (name, time, value) INTO vars`

and we can guarantee that inserted data will have times more or less in order. Maybe it can be done with simple key-value store, but most likely we needs something more capable. Luckly we do not require any transactional guarantees.


## Caching

We can cache read requests, and this may significantly reduce loads when there are a lot of frontend users. As with `Front` service, responses cannot be cached for too long because underlying data will change.  


## Tools

I have not found the right database yet and all other choices will be dependent on chosen database and existing libraries for it. 



Calculation manager
===============================================================================

Service responsible for handling computation of statistics. It will not handle the calculations themselves, there will be a separate service just for that. 


## Processing

Variables are divided into groups and each group will be handled separately in its own thread. The way we divide them is described in the load balancing section. 

For each group, two threads will be created. They will create a TCP connection with `Calculations` and use it to exchange data about the calculated variable. One thread will be responsible for writing requests with inputs, and the other will use receive pairs `(VarName, Value)` and process them further.

Sender thread initiates processing of its variables by loading states from the database and sending `INIT` requests. Then it waits for required values to appear on the queue. When it has collected all values necessary to calculate a new value, it sends `PROCESS` request with this data and starts waiting again. In regular intervals, this thread sends `CHECKPOINT` requests. If, for whatever reason, it needs to stop processing, the thread sends `FINISH` request for every variable.

Receiver thread process two kinds of responses. It can receive pair `(VarName, Value)`, it is put back on queue, so it can be used in future calculations, and It is also sent to `Statistics` using `APPEND_VAR` queue.  It can also receive information about state `(VarName, State)`, then it overrides variable's state with new one. 
*We could skip sending back `VarName`, but then we would need to share information about order of inputs between sender and receiver treads.*

For groups containing only one statistic, pseudocode for this solution could look like this:

```python
init()
while True:
    for _ in range(SMALL_COUNTER):
        values = required_stats.map(name -> queue.pull(name))
        result = process(values)
        statistics.push(result)
        queue.push(result) # Pushing value to the queue means that it is processed so it must be done last.
    checkpoint()
```

*If we want dynamic load balancing, then we need to add an ability to move variables from one thread to another. It is not very difficult, but it requires some extra effort. When we add a new variable, its processing is initiated with `INIT` and then it is processed like other variables. When we remove a variable, then `FINISH` is called, and it is removed from processed variables.*


## API

- `ASYNC OUTPUT APPEND_VAR: (VarName, Value)`   
After a response from `Calculations` is received, the value from the result is sent to this output. 

*Internal*
- *`REQUEST ADD_VAR: VarName, Time -> Unit`  
Informs the thread that it should start processing the given variable from given timestamp. If stored state is newer than the provided timestamp, then newer state is used. If stored state is older, then this thread waits for some time and if it is not updated, then it starts from older state.*

- *`REQUEST REMOVE_VAR: VarName -> Time`  
Informs the thread that it should stop processing the given variable. Its state should be stored and timestamp of this state should be returned.*


## Scaling

Each variable in this node needs to be processed by a single thread, so we cannot add any parallelism to a single variable. However, variables can be freely distributed to different nodes, so this service can be scaled as long as there are enough variables to be distributed. 


## Load balancing

We will do static load balancing here. Variables will be divided into static groups by their statistic and key in such a way that a single statistic is as dispersed as possible. This should reduce the chance of imbalance, because we can reasonably expect that calculations for different keys of the same variable should take roughly the same amount of processing power. 

*If we want dynamic load balancing, then we can achieve it by moving variables between threads. We add a thread that has information about what threads are calculating. It periodically checks what variables are calculated too slowly (check latest calculated value compared to latest calculated requirement) or checks for resource usage, and moves variables from high load nodes to low load nodes.*

*We can also make thread end connection after some (rather long) time, and reconnect. This will allow `Calculation` to move this connection to a different, less used, node if there is such need. This requires `Calculations` to have some reasonable load balancing, but that is simple to achieve.*

*These two extensions would allow us to have quite good dynamic load balancing as long as loads for different variables remain stable, but this is a reasonable assumption. I just want to note this again, I do not think that this is necessary.*


## Database

We need database to store states so that they can be used by different nodes, and can survive failures. Database should have for of `Map[String, Data]`, because we do not need anything more. It should be able to handle large amounts of swaping values for given key, but there will be a lot less reading and no adding or removing keys.



Calculations
===============================================================================

This is an interchangeable service used to calculate values of the statistics.


## API
 
Request to the API will be made during TCP connections. One connection will be directed to one node, and then it will be handled by this node until the connection is ended or broken. 

During the connection, two types of requests can be handled. Data processing requests have to happen in certain order. They must start with `INIT` followed by a series of `PROCESS` and `CHECKPOINT` requests and end with `FINISH` request. 

- `REQUEST INIT: VarName, State -> Unit`  
Starts processing of given variable using given state as initial state.

- `REQUEST PROCESS: VarName, Value -> Value`  
Processes given value and returns updated version.

- `REQUEST CHECKPOINT: VarName -> State`  
Returns current state of given variable. If this service uses some local storage when handling `INIT` request, then state must be saved locally.

- `REQUEST FINISH: VarName -> State`  
Does the same as `CHECKPOINT` and then finishes computations of given variable. 


This API will also provide some technical requests that can make startup easier and less prone to errors where information from two places does not match. 

- `REQUEST METADATA: Unit -> Metadata`  
Sends information about all statistics handled by this service, including dependencies between statistics or information that some statistic does not need any inputs, it just has to be triggered.


## Scaling

This service can be scaled by moving different variables to different nodes. There will be no scaling that causes one statistic to be calculated concurrently. 


## Load balancing

Variables are distributed across nodes and threads, similarly to what happens in `Calculations manager`. This is a static load balancing, but it should be good enough.

*Connections could be distributed to less used nodes, then whenever some connection is reestablished, nodes get more balanced. If connections last only for certain time, then they should get balanced, provided that workload for each connection is stable.*


## Database

This component does not need a database by default, but it may use one to store states. Then it would just send `Unit` and save the state to the database in response to `CHECKPOINT` and `FINISH` requests.
