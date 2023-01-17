Vision 
===============================================================================

We want to create an application that can be used to analyze data in real time and display it on frontend as graphs time-value, value displays and in other forms. We will focus on making this app work well when analyzing data about stock markets, and we will design visuals and ways to define things to display appropriately. However, we do not want to make our project suitable only for stocks, it should work well with other use cases as long as the data has similar structure.

One of the main goals is to make modifying data-processing logic as easy as possible. We want it to be possible to change this part by modifying separate one module in our `Statistics` service or by just swapping one service (see [external calculations extension](extensions/ExtensionExternalCalculations.md)). Moreover modifications to processing logic should not require any changes to parts doing stream processing or communicating with other services. 



Definitions
===============================================================================

***Variable*** &ndash; value that changes in time; it can be represented as function `f: Time -> Value`; e.g. value of specified stock, number of stocks traded, standard deviation of another variable, ...

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

Limitations:
- we do not want any calculations that could be defined by effectful function `f: a -> Unit`
- we do not allow any modifications to processing logic when application is running (specifically `Calculations` service)([external calculations extension](extensions/ExtensionExternalCalculations.md) may partially fix this)
- if any variable is added after app has been running for some time, it will not be calculated for the past (this is fixed by [controller extension](extensions/ExtensionController.md))
- all calculations have to be pure or at least not modify anything external; in the latter case, calculated may be inconsistent, then application will choose arbitrary one



Notes
===============================================================================

- all variables will be updated with the same frequency; having separated update times would be too much of a problem, and it does not work well with our main use case.



Services
===============================================================================

Go to [services](Services.md).



Possible extensions
===============================================================================

- [Controller](extensions/ExtensionController.md) &ndash; provides ability to start and stop calculations, allows adding and removing statistics. This also allows recalculating historical values of added statistic.
- [External calculations](extensions/ExtensionExternalCalculations.md) &ndash; divides `Calculations` into two services: `Manager` and `ExternalCalculations`. The first one is responsible for running streams and sending data to storage, the latter is only responsible for providing definitions of statistics and calculating variables. This simplifies changing logic, allows easier definition of new logic and lets us choose different language for these two services.
- [Postprocessor](extensions/ExtensionPostprocessor.md) &ndash; adds option to calculate values of custom variable on frontend using definition like `(#v1 + #v2) / #v3` and have it calculated just for our request. 
