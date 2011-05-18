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
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Gallery;
import android.widget.ListAdapter;
import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;
import edu.mit.mobile.android.imagecache.SimpleThumbnailCursorAdapter;

public class InteractiveDemo extends ListActivity {
    /** Called when the activity is first created. */
	private ImageCache mCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Gallery gallery = (Gallery) findViewById(R.id.gallery);

        final Uri casts = Uri.parse("content://edu.mit.mobile.android.locast.provider/casts");

        mCache = ImageCache.getInstance(this);

        final ListAdapter castAdapter = new SimpleThumbnailCursorAdapter(this,
        		R.layout.thumbnail_item,
        		managedQuery(casts, new String[] {BaseColumns._ID, "thumbnail_uri"}, null, null, null),
        		new String[]{"thumbnail_uri"},
        		new int[]{R.id.thumb},
        		new int[]{R.id.thumb}, 0);

        final float scale = getResources().getDisplayMetrics().density;

        setListAdapter(new ImageLoaderAdapter(castAdapter, mCache, new int[]{R.id.thumb}, (int)(320 * scale), (int)(200 * scale)));


        final ListAdapter castAdapterSmall = new SimpleThumbnailCursorAdapter(this,
        		R.layout.small_thumbnail_item,
        		managedQuery(casts, new String[] {BaseColumns._ID, "thumbnail_uri"}, null, null, null),
        		new String[]{"thumbnail_uri"},
        		new int[]{R.id.thumb},
        		new int[]{R.id.thumb}, 0);

        gallery.setAdapter(new ImageLoaderAdapter(castAdapterSmall, mCache, new int[]{R.id.thumb}, (int)(160 * scale), (int)(100 * scale)));
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
}