package edu.mit.mobile.android.imagecache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

/**
 * A synchronization lock that creates a separate lock for each key.
 *
 * @author <a href="mailto:spomeroy@mit.edu">Steve Pomeroy</a>
 *
 * @param <K>
 */
public class KeyedLock<K> {
    private static final String TAG = KeyedLock.class.getSimpleName();

    private final Map<K, ReentrantLock> mLocks = new HashMap<K, ReentrantLock>();

    private static boolean DEBUG = false;

    /**
     * @param key
     */
    public void lock(K key) {
        if (DEBUG) {
            log("acquiring lock for key " + key);
        }

        ReentrantLock lock;
        synchronized (mLocks) {
            lock = mLocks.get(key);
            if (lock == null) {
                lock = new ReentrantLock();
                mLocks.put(key, lock);
                if (DEBUG) {
                    log(lock + " created new lock and added it to map");
                }

            }
        }

        lock.lock();
    }

    /**
     * @param key
     */
    public void unlock(K key) {
        if (DEBUG) {
            log("unlocking lock for key " + key);
        }
        ReentrantLock lock;

        synchronized (mLocks) {
            lock = mLocks.remove(key);
            if (lock == null) {
                Log.e(TAG, "Attempting to unlock lock for key " + key + " which has no entry");
                return;
            }
        }
        lock.unlock();
    }

    private void log(String message) {

        Log.d(TAG, Thread.currentThread().getId() + "\t" + message);

    }
}