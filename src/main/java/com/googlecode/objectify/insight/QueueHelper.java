package com.googlecode.objectify.insight;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueConstants;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import lombok.Data;
import java.util.List;

/** Just a slightly more convenient interface for our purposes */
@Data
public class QueueHelper<T> {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final Queue queue;

	public void add(T jsonifyMe) {
		queue.addAsync(null, makeTask(jsonifyMe));
	}

	/** Allows any number of tasks; automatically partitions as necessary */
	public void add(Iterable<T> payloads) {
		Iterable<TaskOptions> opts = Iterables.transform(payloads, new Function<T, TaskOptions>() {
			@Override
			public TaskOptions apply(T thing) {
				return makeTask(thing);
			}
		});

		Iterable<List<TaskOptions>> partitioned = Iterables.partition(opts, QueueConstants.maxTasksPerAdd());

		for (List<TaskOptions> piece: partitioned)
			queue.addAsync(null, piece);
	}

	/** */
	private TaskOptions makeTask(T jsonifyMe) {
		try {
			byte[] payload = MAPPER.writeValueAsBytes(jsonifyMe);
			return TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).payload(payload, "application/json");
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
