# rsocket-metrics

This is a demo application showing configuration necessary to expose metrics for an rsocket server

The goal is for both sleuth tracing and micrometer metrics exposure, both through /actuator/metrics and prometheus metrics at /actuator/prometheus.


Currently only sleuth tracing appears to work, which can be observed in the logs.


/actuator/metrics and /actuator/prometheus appear to provide the usual application metrics, but don't seem to have any RSocket related details.


Data observed by running this application and making requests via `rsc`

```bash
$ rsc --request --route greetings.en_us  -d '{"name":"Nickolas"}' tcp://localhost:7000
{"greeting":"Hello, Nickolas"}
```

Metrics checked with `curl`

```bash
$ curl http://localhost:8080/actuator/metrics | jq
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   849  100   849    0     0   229k      0 --:--:-- --:--:-- --:--:--  276k
{
  "names": [
    "application.ready.time",
    "application.started.time",
    "disk.free",
    "disk.total",
    "executor.active",
    "executor.completed",
    "executor.pool.core",
    "executor.pool.max",
    "executor.pool.size",
    "executor.queue.remaining",
    "executor.queued",
    "http.server.requests",
    "jvm.buffer.count",
    "jvm.buffer.memory.used",
    "jvm.buffer.total.capacity",
    "jvm.classes.loaded",
    "jvm.classes.unloaded",
    "jvm.gc.live.data.size",
    "jvm.gc.max.data.size",
    "jvm.gc.memory.allocated",
    "jvm.gc.memory.promoted",
    "jvm.gc.overhead",
    "jvm.gc.pause",
    "jvm.memory.committed",
    "jvm.memory.max",
    "jvm.memory.usage.after.gc",
    "jvm.memory.used",
    "jvm.threads.daemon",
    "jvm.threads.live",
    "jvm.threads.peak",
    "jvm.threads.states",
    "logback.events",
    "process.cpu.usage",
    "process.files.max",
    "process.files.open",
    "process.start.time",
    "process.uptime",
    "system.cpu.count",
    "system.cpu.usage",
    "system.load.average.1m"
  ]
}
```
