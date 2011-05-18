package edu.mit.mobile.android.imagecache;
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
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.commonsware.cwac.adapter.AdapterWrapper;

/**
 * An adapter that wraps another adapter.
 *
 * @author steve
 *
 */
public class ImageLoaderAdapter extends AdapterWrapper implements ImageCache.OnImageLoadListener {
	private static final String TAG = ImageLoaderAdapter.class.getSimpleName();

	private final HashMap<Long, SoftReference<ImageView>> mImageViewsToLoad = new HashMap<Long, SoftReference<ImageView>>();

	private final int[] mImageViewIDs;
	private final ImageCache mCache;

	private final int mWidth, mHeight;

	/*public static final int
	//	TAG_IMAGE_URI 			= R.id.icon,
		TAG_SECONDARY_IMAGE_URI = R.id.message; // XXX these are arbitrary for the moment. Should fix.
*/

	public static final int
		UNIT_PX = 0,
		UNIT_DIP = 1;

	/**
	 * @param context
	 * @param wrapped
	 * @param cache
	 * @param imageViewIDs
	 * @param width in the specified unit
	 * @param height in the specified unit
	 * @param unit one of UNIT_PX or UNIT_DIP
	 */
	public ImageLoaderAdapter(Context context, ListAdapter wrapped, ImageCache cache, int[] imageViewIDs, int width, int height, int unit) {
		super(wrapped);

		mImageViewIDs = imageViewIDs;
		mCache = cache;
		mCache.registerOnImageLoadListener(this);



		switch (unit){
		case UNIT_PX:
			mHeight = height;
			mWidth = width;
			break;

		case UNIT_DIP:{
			final float scale = context.getResources().getDisplayMetrics().density;
			mHeight = (int) (height * scale);
			mWidth = (int) (width * scale);
		}break;

		default:
			throw new IllegalArgumentException("invalid unit type");

		}
	}

	/**
	 * @param wrapped
	 * @param cache
	 * @param imageViewIDs
	 * @param width in pixels
	 * @param height in pixels
	 */
	public ImageLoaderAdapter(ListAdapter wrapped, ImageCache cache, int[] imageViewIDs, int width, int height) {
		this(null, wrapped, cache, imageViewIDs, width, height, UNIT_PX);
	}

	@Override
	protected void finalize() throws Throwable {
		mCache.unregisterOnImageLoadListener(this);
		super.finalize();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View v = super.getView(position, convertView, parent);

		for (final int id: mImageViewIDs){
			final ImageView iv = (ImageView) v.findViewById(id);
			final Uri tag = (Uri) iv.getTag();
			if (tag != null){
				final long imageID = mCache.getNewID();
				final Bitmap bmp = mCache.loadImage(imageID, tag, mWidth, mHeight);
				if (bmp != null){
					iv.setImageBitmap(bmp);
				}else{
					Log.d(TAG, "scheduling load with ID: "+ imageID+"; URI;"+ tag);
					mImageViewsToLoad.put(imageID, new SoftReference<ImageView>(iv));
				}
			}
		}
		return v;
	}

	@Override
	public void onImageLoaded(long id, Uri imageUri, Bitmap image) {
		final SoftReference<ImageView> ivRef = mImageViewsToLoad.get(id);
		if (ivRef == null){
			return;
		}
		final ImageView iv = ivRef.get();
		if (iv == null){
			mImageViewsToLoad.remove(id);
			return;
		}
		Log.d(TAG, "loading ID "+id + " with an image");
		if (imageUri.equals(iv.getTag())) {
			iv.setImageBitmap(image);
		}
		mImageViewsToLoad.remove(id);
	}
}