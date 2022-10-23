Service name
===============================================================================

Short description that describes main purpose of the service.


## Processing (optional)

Description of how computations are organized in a service. This section is unncessary in request-response stateless services, but it can include important information about state-handling or running bath processing. 


## API

Description of all request this api can do. Alternatively it can describe asynchronous communication. It should include short description of how responses are created. 

- `REQUEST [NAME]: [INPUT #1], [INPUT #2], ... -> OUTPUT`  
Description of actions performed by this request and conditions required to use it.

- `ASYNC INPUT [NAME]: [INPUT]`  
Type of events that can be received through asynchronous queue. Includes all actions performed because of it.

- `ASYNC OUTPUT [NAME]: [OUTPUT]`  
Type of events sent to asynchronous queue. Includes description of condition that cause this output to be used. 


## Scaling

How service is scaled, including limitations on how much it can be scaled and considerations how much scaling is required. If neccessary, it should include statement when we stop carring about scaling further. 


## Load balancing

Description of load balancing solution with their limitations and drawbacks.


## Database (optional)

Description of expected performance characteristics of the database and considerations regarding scaling it.


## Caching (optional)

Description of what is cached and how it affects computations and performance in general. 


## Open questions

- Where can we write about questions that are still not answered or are under considerations?  
Open questions is a great section to talk about anything that still has not been decided.


## Notes

Place to leave anything else. 
