package edu.mit.mobile.android.imagecache.test;
/*
 * Copyright (C) 2011  MIT Mobile Experience Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Gallery;
import edu.mit.mobile.android.imagecache.ImageCache;

@SuppressWarnings("deprecation")
public class InteractiveDemo extends ListActivity {
    /** Called when the activity is first created. */
    private ImageCache mCache;

    private final TestData mTestData = new TestData();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Gallery gallery = (Gallery) findViewById(R.id.gallery);

        mCache = ImageCache.getInstance(this);

        initData();

        setListAdapter(TestData.generateAdapter(this, mTestData, R.layout.thumbnail_item, mCache,
                320, 200));

        gallery.setAdapter(TestData.generateAdapter(this, mTestData, R.layout.small_thumbnail_item,
                mCache, 160, 100));
    }

    private void initData() {

        mTestData
                .addItem(
                        "locast tourism",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/locast_tourism.jpg");
        mTestData
                .addItem(
                        "green home",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/gha_01.jpg");
        mTestData
                .addItem(
                        "green home 2",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/gha_05.jpg");
        mTestData
                .addItem(
                        "Locast healthcare",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/locast%20healthcare.jpg");
        mTestData
                .addItem(
                        "locast h2flow 1",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/water%20project%20IMAGE-72dpi.jpg");
        mTestData
                .addItem(
                        "locast h2flow 2",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/H2flOw_image1.jpg");
        mTestData
                .addItem(
                        "locast h2flow 3",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/H2flOw_image2.jpg");
        mTestData
                .addItem(
                        "locast unicef 1",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/Screen%20shot%202011-08-16%20at%204.05.57%20PM.png");
        mTestData
                .addItem(
                        "locast unicef 2",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/Screen%20shot%202011-08-16%20at%204.14.00%20PM.png");
        mTestData
                .addItem(
                        "locast unicef 3",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/DSC01492.JPG");
        mTestData
                .addItem(
                        "memory traces",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/North%20end%20memory.jpg");
        mTestData
                .addItem(
                        "uv tracking",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/MEL_LocastProjectsandNextTV2%2031_1.jpg");
        mTestData
                .addItem(
                        "civic media 1",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/sites/mel-drudev.mit.edu/files/locast_civic_00.jpg");
        mTestData
                .addItem(
                        "civic media 2",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/locastPA2.jpg");
        mTestData
                .addItem(
                        "civic media 3",
                        "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/locastPA3.jpg");

        // fill it up!
        mTestData.addAll(mTestData);
        mTestData.addAll(mTestData);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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

}