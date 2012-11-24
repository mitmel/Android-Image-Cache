package edu.mit.mobile.android.imagecache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class KeyedLock<K> {
    private final Map<K, ReentrantLock> mLocks = new HashMap<K, ReentrantLock>();

    public void lock(K key) {
        ReentrantLock lock;
        synchronized (mLocks) {
            lock = mLocks.get(key);
            if (lock == null) {
                lock = new ReentrantLock();
                mLocks.put(key, lock);
            }
        }
        lock.lock();
    }

    public void unlock(K key) {
        ReentrantLock lock;

        synchronized (mLocks) {
            lock = mLocks.remove(key);
            if (lock == null) {
                return;
            }
        }
        lock.unlock();
    }
}