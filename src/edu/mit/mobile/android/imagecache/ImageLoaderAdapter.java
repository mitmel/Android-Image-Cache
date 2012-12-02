package edu.mit.mobile.android.imagecache;

/*
 * Copyright (C) 2011-2012 MIT Mobile Experience Lab
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.commonsware.cwac.adapter.AdapterWrapper;

/**
 * <p>
 * An adapter that wraps another adapter, loading images into ImageViews asynchronously.
 * </p>
 *
 * <p>
 * To use, pass in a ListAdapter that generates ImageViews in the layout hierarchy of getView().
 * ImageViews are searched for using the IDs specified in imageViewIDs. When found,
 * {@link ImageView#getTag(R.id.ic__load_id)} is called and should return a {@link Uri} referencing
 * a local or remote image. See {@link ImageCache#loadImage(long, Uri, int, int)} for details on the
 * types of URIs and images supported.
 * </p>
 *
 * @author <a href="mailto:spomeroy@mit.edu">Steve Pomeroy</a>
 *
 */
public class ImageLoaderAdapter extends AdapterWrapper implements ImageCache.OnImageLoadListener {
    private static final String TAG = ImageLoaderAdapter.class.getSimpleName();

    private final HashMap<Long, SoftReference<ImageView>> mImageViewsToLoad = new HashMap<Long, SoftReference<ImageView>>();

    private final int[] mImageViewIDs;
    private final ImageCache mCache;

    private final int mDefaultWidth, mDefaultHeight;

    private final SparseArray<ViewDimensionCache> mViewDimensionCache = new SparseArray<ImageLoaderAdapter.ViewDimensionCache>();

    public static final int UNIT_PX = 0, UNIT_DIP = 1;

    /**
     * @param context
     * @param wrapped
     * @param cache
     * @param imageViewIDs
     *            a list of resource IDs matching the ImageViews that should be scanned and loaded.
     * @param defaultWidth
     *            the default maximum width, in the specified unit. This size will be used if the
     *            size cannot be obtained from the view.
     * @param defaultHeight
     *            the default maximum height, in the specified unit. This size will be used if the
     *            size cannot be obtained from the view.
     * @param unit
     *            one of UNIT_PX or UNIT_DIP
     */
    public ImageLoaderAdapter(Context context, ListAdapter wrapped, ImageCache cache,
            int[] imageViewIDs, int defaultWidth, int defaultHeight, int unit) {
        super(wrapped);

        mImageViewIDs = imageViewIDs;
        mCache = cache;
        mCache.registerOnImageLoadListener(this);

        switch (unit) {
            case UNIT_PX:
                mDefaultHeight = defaultHeight;
                mDefaultWidth = defaultWidth;
                break;

            case UNIT_DIP: {
                final float scale = context.getResources().getDisplayMetrics().density;
                mDefaultHeight = (int) (defaultHeight * scale);
                mDefaultWidth = (int) (defaultWidth * scale);
            }
                break;

            default:
                throw new IllegalArgumentException("invalid unit type");

        }
    }

    /**
     * @param wrapped
     * @param cache
     * @param imageViewIDs
     *            a list of resource IDs matching the ImageViews that should be scan
     * @param width
     *            the maximum width, in pixels
     * @param height
     *            the maximum height, in pixels
     */
    public ImageLoaderAdapter(ListAdapter wrapped, ImageCache cache, int[] imageViewIDs, int width,
            int height) {
        this(null, wrapped, cache, imageViewIDs, width, height, UNIT_PX);
    }

    @Override
    protected void finalize() throws Throwable {
        // TODO this should probably be in its own method, so it can be called in onPause / onResume
        mCache.unregisterOnImageLoadListener(this);
        super.finalize();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View v = super.getView(position, convertView, parent);


        for (final int id : mImageViewIDs) {
            if (convertView != null) {
                final ImageView iv = (ImageView) convertView.findViewById(id);
                if (iv != null) {
                    final Long tagId = (Long) iv.getTag(R.id.ic__load_id);
                    if (tagId != null) {
                        mCache.cancel(tagId);
                    }
                }
            }

            final ImageView iv = (ImageView) v.findViewById(id);
            if (iv == null) {
                continue;
            }
            ViewDimensionCache mViewDimension = mViewDimensionCache.get(id);
            if (mViewDimension == null) {
                final int w = iv.getMeasuredWidth();
                final int h = iv.getMeasuredHeight();
                if (w > 0 && h > 0) {
                    mViewDimension = new ViewDimensionCache();
                    mViewDimension.width = w;
                    mViewDimension.height = h;
                    mViewDimensionCache.put(id, mViewDimension);
                }
            }

            final Uri tag = (Uri) iv.getTag(R.id.ic__uri);
            if (tag != null) {
                final long imageID = mCache.getNewID();
                iv.setTag(R.id.ic__load_id, imageID);
                // attempt to bypass all the loading machinery to get the image loaded as quickly
                // as possible
                Drawable d = null;
                try {
                    if (mViewDimension != null && mViewDimension.width > 0
                            && mViewDimension.height > 0) {
                        d = mCache.loadImage(imageID, tag, mViewDimension.width,
                                mViewDimension.height);
                    } else {
                        d = mCache.loadImage(imageID, tag, mDefaultWidth, mDefaultHeight);
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                if (d != null) {
                    iv.setImageDrawable(d);
                } else {
                    if (ImageCache.DEBUG) {
                        Log.d(TAG, "scheduling load with ID: " + imageID + "; URI;" + tag);
                    }
                    mImageViewsToLoad.put(imageID, new SoftReference<ImageView>(iv));
                }
            }
        }
        return v;
    }

    @Override
    public void onImageLoaded(long id, Uri imageUri, Drawable image) {
        final SoftReference<ImageView> ivRef = mImageViewsToLoad.get(id);
        if (ivRef == null) {
            return;
        }
        final ImageView iv = ivRef.get();
        if (iv == null) {
            mImageViewsToLoad.remove(id);
            return;
        }
        if (ImageCache.DEBUG) {
            Log.d(TAG, "loading ID " + id + " with an image");
        }
        if (imageUri.equals(iv.getTag(R.id.ic__uri))) {
            iv.setImageDrawable(image);
        }
        mImageViewsToLoad.remove(id);
    }

    private static class ViewDimensionCache {
        int width;
        int height;
    }
}
