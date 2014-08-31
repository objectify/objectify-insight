package com.googlecode.objectify.insight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * All we really need to serialize today is a List of buckets. However, it's bad for future extensibility
 * to have an array as the top-level object in our JSON structure, so we wrap it with an object. If we
 * ever need to add fields, we can do it without invalidating any data stored in the task queue.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketList {
	List<Bucket> buckets;

	public BucketList(Collection<Bucket> buckets) {
		this.buckets = new ArrayList<>(buckets);
	}
}
