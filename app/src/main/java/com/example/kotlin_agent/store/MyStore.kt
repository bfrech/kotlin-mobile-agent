package com.example.kotlin_agent.store

import android.content.Context
import com.example.kotlin_agent.ariesAgent.AriesAgent
import org.hyperledger.aries.api.Iterator
import org.hyperledger.aries.api.Store

class MyStore: Store {

    // Batch performs multiple Put and/or Delete operations in order.
    // Depending on the implementation, this method may be faster than repeated Put and/or Delete calls.
    // The "operations" argument must be JSON representing an array of aries-framework-go/spi/storage/Operation.
    // If any of the given keys are empty, then an error will be returned.
    override fun batch(operations: ByteArray?) {
        TODO("Not yet implemented")
    }

    // Close closes this store object, freeing resources. For persistent store implementations, this does not delete
    // any data in the underlying databases.
    override fun close() {
        TODO("Not yet implemented")
    }

    // Delete deletes the key + value pair (and all tags) associated with key.
    // If key is empty, then an error will be returned.
    override fun delete(key: String?) {
        TODO("Not yet implemented")
    }

    // Flush forces any queued up Put and/or Delete operations to execute.
    // If the Store implementation doesn't queue up operations, then this method is a no-op.
    override fun flush() {
        TODO("Not yet implemented")
    }

    // Get fetches the value associated with the given key.
    // If key cannot be found, then an error wrapping ErrDataNotFound will be returned.
    // If key is empty, then an error will be returned.
    override fun get(key: String?): ByteArray {
        TODO("Not yet implemented")
    }

    // GetBulk fetches the values associated with the given keys.
    // If no data exists under a given key, then a nil []byte is returned for that value. It is not considered an error.
    // Depending on the implementation, this method may be faster than calling Get for each key individually.
    // The "keys" argument must be JSON representing an array of strings, one for each key.
    // If any of the given keys are empty, then an error will be returned.
    // The returned array of bytes is expected to be a JSON representation of the values, one for each key, that can
    // be unmarshalled to a [][]byte.
    override fun getBulk(keys: ByteArray?): ByteArray {
        TODO("Not yet implemented")
    }

    // Get fetches all tags associated with the given key.
    // If key cannot be found, then an error wrapping ErrDataNotFound will be returned.
    // If key is empty, then an error will be returned.
    // The returned array of bytes is expected to be JSON that can be unmarshalled to an array of
    // aries-framework-go/spi/storage/Tag.
    override fun getTags(key: String?): ByteArray {
        TODO("Not yet implemented")
    }

    // Put stores the key + value pair along with the (optional) tags.
    // The "tags" argument is optional, but if present,
    // must be JSON representing an array of aries-framework-go/spi/storage/Tag.
    // If key is empty or value is nil, then an error will be returned.
    override fun put(key: String?, value: ByteArray?, tags: ByteArray?) {

        TODO("Not yet implemented")
    }

    // Query returns all data that satisfies the expression. Expression format: TagName:TagValue.
    // If TagValue is not provided, then all data associated with the TagName will be returned.
    // For now, expression can only be a single tag Name + Value pair.
    // PageSize sets the maximum page size for data retrievals done within the Iterator returned by the Query call.
    // Paging is handled internally by the Iterator. Higher values may reduce CPU time and the number of database calls at
    // the expense of higher memory usage.
    override fun query(expression: String?, pageSize: Long): Iterator {
        TODO("Not yet implemented")
    }


}