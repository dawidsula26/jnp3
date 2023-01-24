use testDB
db.createCollection(
  "variables",
  {
     timeseries: {
        timeField: "time",
        metaField: "name",
     }
  }
)
