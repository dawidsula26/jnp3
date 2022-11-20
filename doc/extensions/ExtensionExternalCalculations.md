External calculations
===============================================================================

Services responsible solely for calculating values of variables.


## Motivation

This service allows us to completely separate stream processing stuff from business logic. This allows application to be adapted to desired use case just by swapping this services. Moreover, this component is designed to not have anything complicated except for business logic itself, therefore it can be easily made in any language that is necessary. 


## API

- `REQUEST PROCESS: VarName, Time, Args -> Res`  
Calculates value of statistic using saved state. If state is unavailable locally then error is returned.

- `REQUEST PROCESS_STATE: VarName, Time, State, Args -> State, Res`  
Same as previous one, but uses given state. 

- `REQUEST GET_STATE: VarName, Time -> State`  
Returns state of given variable. Returns error if no state is available.


## Scaling

`PROCESS_STATE` are stateless, but the rest requires that operations for the same variable happen on the same node. This still allows us to easily scale this component as long as we can provide that request for one variable are mostly sent to one node.


## Load balancing

We need load balancer to send requests for one variable to the same node. Otherwise all computations would have to be done with more expensive operation `PROCESS_STATE`


## Database

This component will not use a database. Technically, it could be used to store states, but having to use a database would make this service more complicated than I want. Additionally, having to read and write to the database would be more expensive than just having the state in memory. 



Calculations
===============================================================================

## Processing

We modify the way state is stored. Instead of just keeping state, we keep last known state and list of values that were sent after the state was sent. This allows us to not send state with every request and still be able to reconstruct it when something happens. With this, function that processes values could look like this

```python
def fun(val):
    if (...):
        (state, previousValues) = getState()
        res = requestProcess(val)
        setState((state, previousValues ++ val))
    else:
        res = requestProcess(val)
        state = requestState()
        setState((state, []))
```
