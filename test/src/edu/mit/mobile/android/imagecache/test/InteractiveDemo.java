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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Gallery;
import android.widget.ListAdapter;
import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;
import edu.mit.mobile.android.imagecache.SimpleThumbnailAdapter;

public class InteractiveDemo extends ListActivity {
    /** Called when the activity is first created. */
	private ImageCache mCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Gallery gallery = (Gallery) findViewById(R.id.gallery);

        mCache = ImageCache.getInstance(this);

        final List<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();

        data.add(addItem("locast tourism", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/locast_tourism.jpg"));
        data.add(addItem("green home", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/gha_01.jpg"));
        data.add(addItem("green home 2", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/gha_05.jpg"));
        data.add(addItem("Locast healthcare", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/locast%20healthcare.jpg"));
        data.add(addItem("locast h2flow 1", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/water%20project%20IMAGE-72dpi.jpg"));
        data.add(addItem("locast h2flow 2", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/H2flOw_image1.jpg"));
        data.add(addItem("locast h2flow 3", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/H2flOw_image2.jpg"));
        data.add(addItem("locast unicef 1", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/Screen%20shot%202011-08-16%20at%204.05.57%20PM.png"));
        data.add(addItem("locast unicef 2", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/Screen%20shot%202011-08-16%20at%204.14.00%20PM.png"));
        data.add(addItem("locast unicef 3", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/DSC01492.JPG"));
        data.add(addItem("memory traces", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/North%20end%20memory.jpg"));
        data.add(addItem("uv tracking", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/MEL_LocastProjectsandNextTV2%2031_1.jpg"));
        data.add(addItem("civic media 1", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/sites/mel-drudev.mit.edu/files/locast_civic_00.jpg"));
        data.add(addItem("civic media 2", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/locastPA2.jpg"));
        data.add(addItem("civic media 3", "http://mobile.mit.edu/sites/mel-dru.mit.edu.mainsite/files/imagecache/implementation_big/locastPA3.jpg"));

        // fill it up!
        data.addAll(data);
        data.addAll(data);

        final ListAdapter bigAdapter = new SimpleThumbnailAdapter(this, data, R.layout.thumbnail_item, new String[]{"thumb"}, new int[]{R.id.thumb}, new int[]{R.id.thumb});

        setListAdapter(new ImageLoaderAdapter(this, bigAdapter, mCache, new int[]{R.id.thumb}, 320, 200, ImageLoaderAdapter.UNIT_DIP));

        final ListAdapter smallAdapter = new SimpleThumbnailAdapter(this, data, R.layout.small_thumbnail_item, new String[]{"thumb"}, new int[]{R.id.thumb}, new int[]{R.id.thumb});

        gallery.setAdapter(new ImageLoaderAdapter(this, smallAdapter, mCache, new int[]{R.id.thumb}, 160, 100, ImageLoaderAdapter.UNIT_DIP));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()){
    	case R.id.clear:
    		mCache.clear();
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

    private HashMap<String, String> addItem(String title, String image){
    	final HashMap<String, String> m = new HashMap<String, String>();

    	m.put("title", title);
    	m.put("thumb", image);

    	return m;
    }
}