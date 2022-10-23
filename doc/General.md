Vision 
===============================================================================

We want to create an application that can be used to analyze data in real time and display it on frontend as graphs time-value, value displays and in other forms. We will focus on making this app work well when analyzing data about stock markets, and we will design visuals and ways to define things to display appropriately. However, we do not want to make our project suitable only for stocks, we want it to work well with other use cases. 

Our main focus and the most important part of the project is the ability to easily change the component responsible for analyzing the data. Ideally, we want to be able to change what is calculated without touching other parts of the project. Moreover, anyone who changes data processing logic should not have to think about problems that are common in stream processing. We want to achieve it through separating processing into separate service or separate module inside a service. 

Note that some parts of the project may include some alternative designs, they will be presented in *italic*.



Definitions
===============================================================================

***Variable*** &ndash; value that changes in time; it can be represented as function `f: Time -> Value`; e.g. value of stock, number of stocks traded, standard deviation of another variable, ...

***Statistic*** &ndash; group of variables that share the way they are calculated, but are defined for different things; e.g. value of stocks of CDPROJECT, PGE and PEKAO, or rates of exchange of USD, EUR and GBP, ...

***Key*** &ndash; value that separates parts of a statistic into separate variables; e.g. names of different companies, different currencies, ...



Goal
===============================================================================

We want an application that allows the user to do the following: 
- display graph of a variable
- display graph of a statistic for different
- see updates to graphs in real time

The following should be possible by only changing configs and calculations component:
- define variable that is calculated based on other variable; defined by `f: a -> b`
- define variable that is calculated based on multiple other variables; defined by `f: (a1, a2, ..., an) -> b`
- define variable that is calculated periodically without any meaningful input; defined by `f: Unit -> b`

Note that we do not want any calculations that could be defined by effectful function `f: a -> Unit`.



Notes
===============================================================================

- all variables will be updated with the same frequency; having separated update times would be too much of a problem, and it does not fit well with our main use case.



Changes
===============================================================================

This section presents possible changes that can be reasonably made to the project. Presented architecture is already prepared to easily accommodate them. 

Note that some description will include information on how these extensions can be implemented. These fragments will be in *italic*.

## Dynamic load balancing
By default, services related to calculated variables will have static load balancing. I think it is enough for our project, especially for our main use case, because keys there should require very similar amount of processing power. However, it there is a need for dynamic load balancing, it can be implemented. Presented solutions are not prefect, but according to my research, there are no prefect solutions for our case. 

## Single component calculations
The current version of the project uses two services to calculate variables: `Calculations` and `Calculations manager`. We can merge them together and remove any communications between them and load balancing of requests to `Calculations`. However, this will not affect other parts of these services, as they are needed to organize computations regardless. 

I currently think that this separation is useful, because we can freely choose any language we want in `Calculations`. Merging it with `Calculations manager` would force use into choosing the same language for both components, which is quite limiting. 



Services
===============================================================================

Go to [services](Services.md).
