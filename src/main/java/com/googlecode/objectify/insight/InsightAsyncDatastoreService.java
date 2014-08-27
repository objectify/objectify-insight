package com.googlecode.objectify.insight;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import lombok.RequiredArgsConstructor;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 */
@RequiredArgsConstructor
public class InsightAsyncDatastoreService implements AsyncDatastoreService {

	private final AsyncDatastoreService raw;

	private final InsightCollector collector;

	@Override
	public Future<Entity> get(Key key) {
		collector.collect(Bucket.forGet(NamespaceManager.get(), key.getKind(), 1));
		return raw.get(key);
	}

	@Override
	public Future<Entity> get(Transaction transaction, Key key) {
		return raw.get(transaction, key);
	}

	@Override
	public Future<Map<Key, Entity>> get(Iterable<Key> keys) {
		return raw.get(keys);
	}

	@Override
	public Future<Map<Key, Entity>> get(Transaction transaction, Iterable<Key> keys) {
		return raw.get(transaction, keys);
	}

	@Override
	public Future<Key> put(Entity entity) {
		return raw.put(entity);
	}

	@Override
	public Future<Key> put(Transaction transaction, Entity entity) {
		return raw.put(transaction, entity);
	}

	@Override
	public Future<List<Key>> put(Iterable<Entity> entities) {
		return raw.put(entities);
	}

	@Override
	public Future<List<Key>> put(Transaction transaction, Iterable<Entity> entities) {
		return raw.put(transaction, entities);
	}

	@Override
	public Future<Void> delete(Key... keys) {
		return raw.delete(keys);
	}

	@Override
	public Future<Void> delete(Transaction transaction, Key... keys) {
		return raw.delete(transaction, keys);
	}

	@Override
	public Future<Void> delete(Iterable<Key> keys) {
		return raw.delete(keys);
	}

	@Override
	public Future<Void> delete(Transaction transaction, Iterable<Key> keys) {
		return raw.delete(transaction, keys);
	}

	@Override
	public Future<Transaction> beginTransaction() {
		return raw.beginTransaction();
	}

	@Override
	public Future<Transaction> beginTransaction(TransactionOptions transactionOptions) {
		return raw.beginTransaction(transactionOptions);
	}

	@Override
	public Future<KeyRange> allocateIds(String s, long l) {
		return raw.allocateIds(s, l);
	}

	@Override
	public Future<KeyRange> allocateIds(Key key, String s, long l) {
		return raw.allocateIds(key, s, l);
	}

	@Override
	public Future<DatastoreAttributes> getDatastoreAttributes() {
		return raw.getDatastoreAttributes();
	}

	@Override
	public Future<Map<Index, Index.IndexState>> getIndexes() {
		return raw.getIndexes();
	}

	@Override
	public PreparedQuery prepare(Query query) {
		return raw.prepare(query);
	}

	@Override
	public PreparedQuery prepare(Transaction transaction, Query query) {
		return raw.prepare(transaction, query);
	}

	@Override
	public Transaction getCurrentTransaction() {
		return raw.getCurrentTransaction();
	}

	@Override
	public Transaction getCurrentTransaction(Transaction transaction) {
		return raw.getCurrentTransaction(transaction);
	}

	@Override
	public Collection<Transaction> getActiveTransactions() {
		return raw.getActiveTransactions();
	}
}
