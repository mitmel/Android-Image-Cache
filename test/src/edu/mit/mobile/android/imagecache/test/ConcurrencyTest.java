package edu.mit.mobile.android.imagecache.test;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import edu.mit.mobile.android.imagecache.ImageCache;

public class ConcurrencyTest extends Activity {

    private GridView mGrid;
    private ImageCache mCache;
    private TestData mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_concurrency_test);

        mCache = new SlowImageCache(this);

        mGrid = (GridView) findViewById(R.id.grid);

        mData = new TestData();

        initData();

        mGrid.setAdapter(TestData.generateAdapter(this, mData, R.layout.square_thumbnail_item,
                mCache, 64, 64));
    }

    private void initData() {
        mData.addItem(
                "Federico",
                "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/person_profile/sites/mel-drudev.mit.edu/files/pic_64px_boss.jpg");
        mData.addItem(
                "Leo",
                "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/person_profile/sites/mel-drudev.mit.edu/files/leonardo_0.jpg");

        mData.addItem(
                "Nick",
                "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/person_profile/nwallen_pic.jpg");

        mData.addItem(
                "Steve",
                "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/person_profile/sites/mel-drudev.mit.edu/files/pic_64px_steve.jpg");

        mData.addItem(
                "Amar",
                "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/person_profile/me-icon_0.png");

        for (int i = 0; i < 10; i++) {
            mData.addAll(mData);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                mCache.clear();
                return true;

            case R.id.grid:
                startActivity(new Intent(this, ConcurrencyTest.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private static class SlowImageCache extends ImageCache {

        protected SlowImageCache(Context context) {
            super(context, CompressFormat.JPEG, 85);
        }

        @Override
        protected void downloadImage(String key, Uri uri) throws ClientProtocolException,
                IOException {
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {

            }
            super.downloadImage(key, uri);

        }
    }

}
