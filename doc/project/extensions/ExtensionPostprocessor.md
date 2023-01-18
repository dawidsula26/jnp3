Postprocessor
===============================================================================

Allows additional processing of data just before displaying them on frontend. 

## Motivation

We could add new option to the user. They would write expression in SpEL using available variables (eg. `#max_price - #opening_price`), specify the key, and they get a diagram of values of given expression. This feature would allow user more flexibility in how they use existing data. 

More pragmatically, it would look cool if user was able to this, and all hard things are already implemented in `Spring`.


## Processing

We find all variables used in SpEL expression and pull values of corresponding variables (or get all variables) and then just use method from `Spring` `evaluateWithContext(expr, variables)` for every requested time. 

The we will have to make some validations, like checking if all used variables are available, but majority of it is already done in `Spring`. 


## API

- `REQUEST POSTPROCESS_VAR: Expr, Key -> [Value] | ExprError`  
Calculates given expression for variables with given key. Returns calculated values and timestamp of the first value. 

- `REQUEST POSTPROCESS_UPDATE: Expr, Key, Time -> [Value] | ExprError` 
Similar to previous request, but calculates only values with time greater or equal to given one. 


## Scaling / Load balancing

It is stateless, so there is nothing work noting here. 


## Caching

We can cache the same things as statistics, but it may be less useful, because there are less requests and they are more unique. Still, we may expect that the same request can be sent multiple times, so caching should still be worth it, but results should be stored for shorter times. 


## Tools

We need `Spring` so we have to use JVM language. Our choices are Scala and Kotlin (I assume that we do not want to work with Java).



Front &ndash; changes
===============================================================================

## API

- `REQUEST POSTPROCESS_VAR: Expr, Key -> [Value] | ExprError`  
Resends request to `Postprocessor`

- `REQUEST POSTPROCESS_UPDATE: Expr, Key, Time -> [Value] | ExprError` 
Resends request to `Postprocessor`
