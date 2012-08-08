package edu.mit.mobile.android.imagecache;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

public class DrawableMemCache<T> extends LruCache<T, Drawable> {

	@SuppressWarnings("unused")
	private static final String TAG = DrawableMemCache.class.getSimpleName();

	public DrawableMemCache(int maxSize) {
		super(maxSize);
	}

	@Override
	protected int sizeOf(T key, Drawable value) {
		int size = 0;
		if (value instanceof BitmapDrawable) {
			final Bitmap b = ((BitmapDrawable) value).getBitmap();
			if (b != null) {
				size = b.getRowBytes() * b.getHeight();
			}
		}
		return size;
	}
}
