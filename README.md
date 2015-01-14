# Objectify Insight

This library provides insight into your high-volume GAE datastore activity. It records read and write activity
broken down by time, namespace, kind, operation, and query and aggregates this data into Google BigQuery. By
aggregating at multiple levels, Insight scales to thousands of requests per second.
 
Insight works well with Google App Engine applications that use Objectify, but (with some limitations) it can work 
with any application that uses the low level datastore API.

Insight is a metrics collection system. It flows aggregated data into BigQuery in a format that should be useful to
developers and system administrators. It does not provide a query interface to BigQuery.

## Code

https://github.com/stickfigure/objectify-insight

https://github.com/stickfigure/objectify-insight-example

## Design

Insight has several moving parts:

 * A facade of the low-level `AsyncDatastoreService` which aggregates metrics in instance memory and periodically
   flushes them to a pull queue.
 * A task, which should be called via cron (every minute) which aggregates pull queue tasks and pushes these
   aggregations into BigQuery.
 * A task, which should be called via cron (infrequently) which ensures that the appropriate BigQuery tables
   exist.
   
The resulting BigQuery table data will look something like this:

```
| uploaded                | codepoint                        | namespace  | kind   | op     | query                          | time                    | reads | writes |
| ----------------------- | -------------------------------- | ---------- | ------ | ------ | ------------------------------ | ----------------------- | ----- | ------ |
| 2014-09-15 04:58:40 UTC | d41d8cd98f00b204e9800998ecf8427e | namespace2 | Thing1 | QUERY  | SELECT * FROM Thing1 WHERE ... | 2014-09-15 04:58:40 UTC | 4     | 0      |	 
| 2014-09-15 04:58:40 UTC | 9e107d9d372bb6826bd81d3542a419d6 | namespace1 | Thing2 | DELETE |                                | 2014-09-15 04:58:40 UTC | 0     | 1      |	 
| 2014-09-15 04:58:40 UTC | e4d909c290d0fb1ca068ffaddf22cbd0 | namespace1 | Thing1 | SAVE   |                                | 2014-09-15 04:58:40 UTC | 0     | 1      |
```

If you've ever seen a ROLAP database, this should look familiar. *codepoint*, *namespace*, *kind*, *op*, *query*, and *time* are
dimensions; *reads* and *writes* are the aggregated statistics.
 
*uploaded* is the date that the batch was uploaded to BigQuery. *time* is the actual date of the operation,
rounded to a configurable boundary (default 1 minute) to allow for reasonable aggregation.

*reads* and *writes* are entity counts, not operation counts. Insight cannot determine the number of write operations
required to update or delete the indexes of an entity.

*codepoint* is the md5 hash of a stacktrace to the unique point in your code where the datastore operation took place.
To look up the actual stacktrace, grep your App Engine logs for the hash value. Each instance will log the
definition of each codepoint exactly once. Enable INFO logging at `com.googlecode.objectify.insight`.

## Installation

If you use Guice, you may find it helpful to read the code at https://github.com/stickfigure/objectify-insight-example
Guice is not required to use Insight, but it helps. This documentation assumes you will use Guice.

### Set up Queue

Add a pull queue named "insight" to your `queue.xml`:

```xml
<queue-entries>
	<queue>
		<name>insight</name>
		<mode>pull</mode>
	</queue>
</queue-entries>
```

### Set up Cron

Add two entries to your `cron.xml`:

```xml
<cronentries>
	<cron>
		<url>/private/tableMaker</url>
		<description>Make sure we have enough tables for a week</description>
		<schedule>every 8 hours</schedule>
	</cron>
	<cron>
		<url>/private/puller</url>
		<description>Move all data to BQ</description>
		<schedule>every 1 minutes</schedule>
	</cron>
</cronentries>
```

### Enable the servlets

In your guice `ServletModule`, serve the paths you specified in `cron.xml` above with the relevant servlets:

```java
serve("/private/tableMaker").with(GuiceTableMakerServlet.class);
serve("/private/puller").with(GuicePullerServlet.class);
```

You will likely want to secure these servlets by using the standard security features in GAE:

https://developers.google.com/appengine/docs/java/config/webxml#Security_and_Authentication

It is not dangerous to expose these endpoints to the public, but they are not for human consumption.

#### Servlets without Guice

If you are not using Guice (or another JSR-330 compabile DI framework), extend the `AbstractTableMakerServlet` 
and `AbstractPullerServlet` classes. They offer a poor-man's DI system.

### Get an AsyncDatastoreService

Insight is implemented as a wrapper to the GAE low-level API `AsyncDatastoreService` class.
The `InsightAsyncDatastoreService` itself is constructed by passing in the raw `AsyncDatastoreService`
you get from Google, plus the Insight `Recorder`. The `Recorder` requires the `Collector` and `BucketFactory`... etc.
Guice (or any other JSR-330 compatible DI framework) makes this much more convenient and all pretty much automatic.

Here is the minimum Guice configuration you would need to be able to get the `Recorder`
out of the injector. That is, we want this to work:

```java
AsyncDatastoreService raw = DatastoreServiceFactory.getAsyncDatastoreService();
Recorder recorder = injector.getInstance(Recorder.class);
AsyncDatastoreService tracksMetrics = new InsightAsyncDatastoreService(raw, recorder);
```

These are the bindings you will need to create in your Guice module:
 
```java
@Provides
Bigquery bigquery() { 
	// your complicated code to generate an authenticated connection here
}

/** The bigquery project and dataset ids where you will write data */
@Provides
InsightDataset insightDataset() {
	return new InsightDataset() {
		@Override
		public String projectId() {
			return "objectify-insight-test";
		}
	
		@Override
		public String datasetId() {
			return "insight_example";
		}
	};
}

/** There must be a Queue bound with the name "insight" */
@Provides
@Named("insight")
public Queue queue() {
	return QueueFactory.getQueue(Flusher.DEFAULT_QUEUE);
}

```

Creating an authenticated instance of `Bigquery` is not in the scope of this document. If you make it injectible,
Guice will inject it into Insight. Insight also needs to know the project and dataset ids for bigquery, and the
pull queue that will be used for aggregation.

### Decide what to record

By default, Insight ignores everything. You can tell the `Recorder` to record specific kinds or to record everything.
`Recorder` is a singleton; this configuration only needs to happen once:

```java
Recorder recorder = injector.getInstance(Recorder.class);

// You can specify kinds individually
recorder.recordKind("Thing");
recorder.recordKind("OtherThing");

// If true, all kinds will be recorded
recorder.setRecordAll(true);
```

You can disable recording of codepoint hashes by calling:

```java
recorder.getCodepointer().setDisabled(true);
```

### Use Insight with Objectify

Assuming you have injected the `Recorder` into your `ObjectifyFactory`, override these methods:

#### ObjectifyFactory.createRawAsyncDatastoreService() 

The `ObjectifyFactory` uses an overridable method to obtain the low-level `AsyncDatastoreService` interface. 
Override this method and return your wrapper `InsightAsyncDatastoreService`:

```java
	@Override
	protected AsyncDatastoreService createRawAsyncDatastoreService(DatastoreServiceConfig cfg) {
		AsyncDatastoreService raw = super.createRawAsyncDatastoreService(cfg);
		return new InsightAsyncDatastoreService(raw, recorder);
	}
```

#### ObjectifyFactory.register()

This allows you to use the `Collect` annotation on POJO entity classes to enable recording. This is an alternative
to registering kinds one-at-a-time by hand.

```java
	@Override
	public <T> void register(Class<T> clazz) {
		super.register(clazz);

		if (clazz.isAnnotationPresent(Collect.class))
			recorder.recordKind(Key.getKind(clazz));
	}
```

This override can be skipped if you use `Recorder.setRecordAll(true)`.
  
## Configuration

Insight has tunable parameters spread across several different singleton objects in the object graph. You can
inject/fetch them in Guice and reset values, or (if you aren't using guice) set them as you construct the object
graph manually.
 
Broken down by object:

### Collector

```java
Collector collector = injector.getInstance(Collector.class);
collector.setSizeThreshold(500);
collector.setAgeThresholdMillis(1000 * 30);
```

The Collector is responsible for aggregating metrics and periodically flushing aggregations to the `Flusher`.
Flushing occurs when the number of separate aggregations exceeds a threshold, or the oldest bucket hits an
age threshold.

Note that age-threshold flushing occurs within the context of the next collection request; Insight does not
create extra threads in your application.
 
### Clock

```java
Clock clock = injector.getInstance(Clock.class);
clock.setGranularityMillis(1000 * 600);
```

Most requests come in at fairly unique millisecond clock values. In order to get meaningful aggregation,
we must 'round' clock values to something more granular. Coarser (higher) numbers provide better aggregation
at the cost of less precisely knowing when activities happen.

### TablePicker

```java
TablePicker picker = injector.getInstance(TablePicker.class);
picker.setFormat(new SimpleDateFormat("'myprefix_'YYMMdd");
```

You can change the format of table names; be sure to include any prefix as a constant in the DateFormat.

### Puller

```java
Puller puller = injector.getInstance(Puller.class);
puller.setBatchSize(50);
puller.setLeaseDurationSeconds(300);
```

The Puller pulls batches of data off of the pull queue and pushes them to BigQuery. Since BigQuery is limited
to how large a single request can be, you might need to adjust the batch size. The default is 20. If you get
"request too large" errors, adjust this down.

See https://github.com/stickfigure/objectify-insight/issues/3

## Limitations

Insight tracks all of the datastore operations used by Objectify, but does not track every operation you can
possibly perform in the low-level API. In particular, it is possible to trigger read operations on `List`
objects in such a way that Insight cannot determine statistics without potentially impacting the performance
of your application. For example:

```java
PreparedQuery pq = ds.prepare(query);
List<Entity> entities = pq.asList(fetchOpts).asList();
int size = entities.size();
```

Insight doesn't know what to do with this without explicitly iterating through the `List`.

As long as you iterate through results in the low-level API at least once, Insight will track statistics.
Note that if you use Objectify, this limitation does not apply; Objectify always iterates the original `List`.

## More

If you have questions, ask on the Objectify Google Group:

http://groups.google.com/group/objectify-appengine

## License

Released under the MIT License.

## Thanks

Huge thanks to BetterCloud (http://www.bettercloud.com/) for funding this project!
