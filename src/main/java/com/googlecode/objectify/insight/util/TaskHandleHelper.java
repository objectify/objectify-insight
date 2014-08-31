package com.googlecode.objectify.insight.util;

import com.google.appengine.api.taskqueue.TaskHandle;
import lombok.RequiredArgsConstructor;
import java.io.IOException;

/** */
@RequiredArgsConstructor
public class TaskHandleHelper<T> {
	private final TaskHandle handle;
	private final Class<T> clazz;

	/** */
	public T getPayload() {
		try {
			return QueueHelper.MAPPER.readValue(handle.getPayload(), clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/** */
	public TaskHandle getRaw() {
		return handle;
	}
}
