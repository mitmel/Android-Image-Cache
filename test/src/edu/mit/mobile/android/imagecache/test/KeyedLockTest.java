package edu.mit.mobile.android.imagecache.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.test.AndroidTestCase;
import android.util.Log;
import edu.mit.mobile.android.imagecache.KeyedLock;

public class KeyedLockTest extends AndroidTestCase {

    protected static final String TAG = "KeyedLockTest";
    final KeyedLock<String> mLock = new KeyedLock<String>();

    private final Set<String> mGotFromStore = Collections.synchronizedSet(new HashSet<String>());

    public void testKeyedLock() throws InterruptedException {

        final Thread t1 = new Thread(new Sandwich());
        final Thread t2 = new Thread(new Sandwich());
        final Thread t3 = new Thread(new Sandwich());

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
    }

    public void getIngredient(String ingredient) {
        mLock.lock(ingredient);

        log("Getting " + ingredient);

        try {
            if (mGotFromStore.contains(ingredient)) {
                log("Already have " + ingredient);
                return;
            }

            log("Going to the store to get " + ingredient);

            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                log("interrupted going to the store");
            }
            mGotFromStore.add(ingredient);

            log("Returned from the store with " + ingredient);
        } finally {
            mLock.unlock(ingredient);
        }
    }

    private static int mId = 0;

    private static long startTime = System.nanoTime();

    private long getTime() {
        return (System.nanoTime() - startTime) / 1000000;
    }

    private void log(String msg) {
        Log.d(TAG, "(" + getTime() + "\t" + Thread.currentThread().getId() + "): " + msg);
    }

    private class Sandwich implements Runnable {

        int id = mId++;

        @Override
        public void run() {
            log("Going to make a sandwich! I need bread...");

            getIngredient("bread");
            log("got bread");

            getIngredient("cheese");
            log("got cheese");

        }

        private void log(String msg) {
            KeyedLockTest.this.log("#" + id + ": " + msg);
        }

    }
}
