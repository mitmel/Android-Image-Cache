package edu.mit.mobile.android.imagecache.test;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;
import edu.mit.mobile.android.imagecache.SimpleThumbnailAdapter;

public class TestData extends ArrayList<HashMap<String, String>> {

    /**
     *
     */
    private static final long serialVersionUID = -6862061777028855417L;

    public void addItem(String title, String image) {
        final HashMap<String, String> m = new HashMap<String, String>();

        m.put("title", title);
        m.put("thumb", image);

        add(m);
    }

    public static ImageLoaderAdapter generateAdapter(Context context, TestData data, int layout,
            ImageCache cache, int width, int height) {
        final SimpleThumbnailAdapter bigAdapter = new SimpleThumbnailAdapter(context, data,
 layout,
                new String[] { "thumb" }, new int[] { R.id.thumb },
                new int[] { R.id.thumb });

        return new ImageLoaderAdapter(context, bigAdapter, cache, new int[] { R.id.thumb }, width,
                height, ImageLoaderAdapter.UNIT_DIP);
    }
}
