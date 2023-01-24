sh.addShard("rs-shard-01/shard01-a:27017")
sh.addShard("rs-shard-01/shard01-b:27017")
sh.addShard("rs-shard-02/shard02-a:27017")
sh.addShard("rs-shard-02/shard02-b:27017")

use testDB
sh.enableSharding("testDB")
sh.shardCollection(
  "variables",
  { "name": 1 },
  {
     timeseries: {
        timeField: "time",
        metaField: "name",
     }
  }
)
