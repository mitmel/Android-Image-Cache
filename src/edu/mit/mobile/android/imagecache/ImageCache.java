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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * <p>
 * An image download-and-cacher that also knows how to efficiently generate
 * thumbnails of various sizes.
 * </p>
 *
 * <p>
 * The cache is shared with the entire process, so make sure you
 * {@link #registerOnImageLoadListener(OnImageLoadListener)} and
 * {@link #unregisterOnImageLoadListener(OnImageLoadListener)} any load
 * listeners in your activities.
 * </p>
 *
 * @author <a href="mailto:spomeroy@mit.edu">Steve Pomeroy</a>
 *
 */
public class ImageCache extends DiskCache<String, Bitmap> {
	private static final String TAG = ImageCache.class.getSimpleName();

	static final boolean DEBUG = false;

	private final HashSet<OnImageLoadListener> mImageLoadListeners = new HashSet<ImageCache.OnImageLoadListener>();

	private final HashMap<String, SoftReference<Drawable>> mMemCache = new HashMap<String, SoftReference<Drawable>>();

	private long mIDCounter = 0;

	private static ImageCache mInstance;

	private final HttpClient hc;

	private final CompressFormat mCompressFormat;
	private final int mQuality;

	private final Resources mRes;

	// TODO make it so this is customizable on the instance level.
	/**
	 * Gets an instance of the cache.
	 *
	 * @param context
	 * @return an instance of the cache
	 */
	public static ImageCache getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ImageCache(context, CompressFormat.JPEG, 85);
		}
		return mInstance;
	}

	private ImageCache(Context context, CompressFormat format, int quality) {
		super(context.getCacheDir(), null, getExtension(format));
		hc = getHttpClient();

		mRes = context.getResources();

		mCompressFormat = format;
		mQuality = quality;
	}

	private static String getExtension(CompressFormat format) {
		String extension;
		switch (format) {
		case JPEG:
			extension = ".jpg";
			break;

		case PNG:
			extension = ".png";
			break;

		default:
			throw new IllegalArgumentException();
		}

		return extension;
	}

	/**
	 * If loading a number of images where you don't have a unique ID to
	 * represent the individual load, this can be used to generate a sequential
	 * ID.
	 *
	 * @return a new unique ID
	 */
	public synchronized long getNewID() {
		return mIDCounter++;
	}

	@Override
	protected Bitmap fromDisk(String key, InputStream in) {

		if (DEBUG) {
			Log.d(TAG, "disk cache hit");
		}
		try {
			final Bitmap image = BitmapFactory.decodeStream(in);
			return image;

		} catch (final OutOfMemoryError oom) {
			oomClear();
			return null;
		}
	}

	@Override
	protected void toDisk(String key, Bitmap image, OutputStream out) {
		if (DEBUG) {
			Log.d(TAG, "cache write for key " + key);
		}
		if (image != null) {
			if (!image.compress(mCompressFormat, mQuality, out)) {
				Log.e(TAG, "error writing compressed image to disk for key "
						+ key);
			}
		} else {
			Log.e(TAG, "attempting to write null image to cache");
		}
	}

	/**
	 * Gets an instance of AndroidHttpClient if the devices has it (it was
	 * introduced in 2.2), or falls back on a http client that should work
	 * reasonably well.
	 *
	 * @return a working instance of an HttpClient
	 */
	private HttpClient getHttpClient() {
		HttpClient ahc;
		try {
			final Class<?> ahcClass = Class
					.forName("android.net.http.AndroidHttpClient");
			final Method newInstance = ahcClass.getMethod("newInstance",
					String.class);
			ahc = (HttpClient) newInstance.invoke(null, "ImageCache");

		} catch (final ClassNotFoundException e) {
			DefaultHttpClient dhc = new DefaultHttpClient();
			final HttpParams params = dhc.getParams();
			dhc = null;

			final SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));

			final ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
					params, registry);
			ahc = new DefaultHttpClient(manager, params);

		} catch (final NoSuchMethodException e) {

			final RuntimeException re = new RuntimeException(
					"Programming error");
			re.initCause(e);
			throw re;

		} catch (final IllegalAccessException e) {
			final RuntimeException re = new RuntimeException(
					"Programming error");
			re.initCause(e);
			throw re;

		} catch (final InvocationTargetException e) {
			final RuntimeException re = new RuntimeException(
					"Programming error");
			re.initCause(e);
			throw re;
		}
		return ahc;
	}

	/**
	 * <p>
	 * Registers an {@link OnImageLoadListener} with the cache. When an image is
	 * loaded asynchronously either directly by way of
	 * {@link #scheduleLoadImage(long, Uri, int, int)} or indirectly by
	 * {@link #loadImage(long, Uri, int, int)}, any registered listeners will
	 * get called.
	 * </p>
	 *
	 * <p>
	 * This should probably be called from {@link Activity#onResume()}.
	 * </p>
	 *
	 * @param onImageLoadListener
	 */
	public void registerOnImageLoadListener(
			OnImageLoadListener onImageLoadListener) {
		mImageLoadListeners.add(onImageLoadListener);
	}

	/**
	 * <p>
	 * Unregisters the listener with the cache. This will not cancel any pending
	 * load requests.
	 * </p>
	 *
	 * <p>
	 * This should probably be called from {@link Activity#onPause()}.
	 * </p>
	 *
	 * @param onImageLoadListener
	 */
	public void unregisterOnImageLoadListener(
			OnImageLoadListener onImageLoadListener) {
		mImageLoadListeners.remove(onImageLoadListener);
	}

	private class LoadResult {
		public LoadResult(long id, Uri image, Drawable drawable) {
			this.id = id;
			this.drawable = drawable;
			this.image = image;
		}

		final Uri image;
		final long id;
		final Drawable drawable;
	}

	/**
	 * @param uri
	 *            the image uri
	 * @return a key unique to the given uri
	 */
	public String getKey(Uri uri) {
		return uri.toString();
	}

	/**
	 * Gets the given key as a drawable, retrieving it from memory cache if it's present. If it's not in memory cache,
	 * it will check disk cache as well and return a BitmapDrawable if it finds it there.
	 *
	 * @param key a key generated by {@link #getKey(Uri)} or {@link #getKey(Uri, int, int)}
	 * @return the drawable if it's in the cache or null.
	 * @throws IOException if there is a problem getting accessing the disk cache
	 */
	public Drawable getDrawable(String key) throws IOException {
		final SoftReference<Drawable> memCached = mMemCache.get(key);
		if (memCached != null) {
			final Drawable img = memCached.get();
			if (img != null) {
				if (DEBUG) {
					Log.d(TAG, "mem cache hit");
				}
				return img;
			}
		}

		return null;
	}

	/**
	 * Puts a drawable into memory cache.
	 * @param key a key generated by {@link #getKey(Uri)} or {@link #getKey(Uri, int, int)}
	 * @param drawable
	 */
	public void putDrawable(String key, Drawable drawable) {
		mMemCache.put(key, new SoftReference<Drawable>(drawable));
	}

	/**
	 * A blocking call to get an image. If it's in the cache, it'll return the
	 * drawable immediately. Otherwise it will download, scale, and cache the
	 * image before returning it. For non-blocking use, see {@link #loadImage(long, Uri, int, int)}
	 *
	 * @param uri
	 * @param width
	 * @param height
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws ImageCacheException
	 */
	public Drawable getImage(Uri uri, int width, int height)
			throws ClientProtocolException, IOException, ImageCacheException {
		final String scaledKey = getKey(uri, width, height);

		Drawable d = getDrawable(scaledKey);
		if (d != null){
			return d;
		}

		Bitmap bmp = get(scaledKey);

		if(bmp == null){
			if ("file".equals(uri.getScheme())) {
				bmp = scaleLocalImage(new File(uri.getPath()), width,
						height);
			} else {
				final String sourceKey = getKey(uri);

				if (! contains(sourceKey)) {
					downloadImage(sourceKey, uri);
				}
				bmp = scaleLocalImage(getFile(sourceKey), width, height);
				if (bmp == null){
					clear(sourceKey);
				}
			}
			put(scaledKey, bmp);
		}
		if (bmp == null) {
			throw new ImageCacheException("got null bitmap from request to scale");

		}
		d = new BitmapDrawable(mRes, bmp);
		putDrawable(scaledKey, d);

		return d;
	}

	/**
	 * Returns an opaque cache key representing the given uri, width and height.
	 *
	 * @param uri
	 *            an image uri
	 * @param width
	 *            the desired image max width
	 * @param height
	 *            the desired image max height
	 * @return a cache key unique to the given parameters
	 */
	public String getKey(Uri uri, int width, int height) {
		return uri.buildUpon()
				.appendQueryParameter("width", String.valueOf(width))
				.appendQueryParameter("height", String.valueOf(height)).build()
				.toString();
	}

	private class ImageLoadTask extends AsyncTask<Object, Void, LoadResult> {

		@Override
		protected LoadResult doInBackground(Object... params) {
			final long id = (Long) params[0];
			final Uri uri = (Uri) params[1];
			final int width = (Integer) params[2];
			final int height = (Integer) params[3];

			try {
				return new LoadResult(id, uri, getImage(uri, width, height));

				// TODO this exception came about, no idea why:
				// java.lang.IllegalArgumentException: Parser may not be null
			} catch (final IllegalArgumentException e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			} catch (final OutOfMemoryError oom) {
				oomClear();
			} catch (final ClientProtocolException e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			} catch (final IOException e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			} catch (final ImageCacheException e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			}
			return null;
		};

		@Override
		protected void onPostExecute(LoadResult result) {
			if (result == null) {
				Log.w(TAG, "ImageLoadTask result was null");
				return;
			}

			for (final OnImageLoadListener listener : mImageLoadListeners) {
				listener.onImageLoaded(result.id, result.image, result.drawable);
			}
		};
	}

	private void oomClear() {
		Log.w(TAG, "out of memory, clearing mem cache");
		mMemCache.clear();
	}

	/**
	 * Checks the cache for an image matching the given criteria and returns it.
	 * If it isn't immediately available, calls {@link #scheduleLoadImage}.
	 *
	 * @param id
	 *            An ID to keep track of image load requests. For one-off loads,
	 *            this can just be the ID of the {@link ImageView}. Otherwise,
	 *            an unique ID can be acquired using {@link #getNewID()}.
	 *
	 * @param image
	 *            the image to be loaded. Can be a local file or a network
	 *            resource.
	 * @param width
	 *            the maximum width of the resulting image
	 * @param height
	 *            the maximum height of the resulting image
	 * @return the cached bitmap if it's available immediately or null if it
	 *         needs to be loaded asynchronously.
	 */
	public Drawable loadImage(long id, Uri image, int width, int height) throws IOException {
		final Drawable res = getDrawable(getKey(image, width, height));
		if (res == null) {
			scheduleLoadImage(id, image, width, height);
		}
		return res;
	}

	/**
	 * Schedules a load of the given image. When the image has finished loading
	 * and scaling, all registered {@link OnImageLoadListener}s will be called.
	 *
	 * @param id
	 *            An ID to keep track of image load requests. For one-off loads,
	 *            this can just be the ID of the {@link ImageView}. Otherwise,
	 *            an unique ID can be acquired using {@link #getNewID()}.
	 *
	 * @param image
	 *            the image to be loaded. Can be a local file or a network
	 *            resource.
	 * @param width
	 *            the maximum width of the resulting image
	 * @param height
	 *            the maximum height of the resulting image
	 */
	public void scheduleLoadImage(long id, Uri image, int width, int height) {
		if (DEBUG){
			Log.d(TAG, "executing new ImageLoadTask");
		}
		final ImageLoadTask imt = new ImageLoadTask();

		try {
			imt.execute(id, image, width, height);
		}catch (final RejectedExecutionException re){
			Log.e(TAG, re.getLocalizedMessage(), re);
			// oh well. At least we didn't crash!
		}
	}

	/**
	 * Cancels all the asynchronous image loads.
	 * Note: currently does not function properly.
	 *
	 */
	public void cancelLoads() {
		// TODO actually make it possible to cancel tasks
	}

	/**
	 * Blocking call to scale a local file. Scales using preserving aspect ratio
	 *
	 * @param localFile
	 *            local image file to be scaled
	 * @param width
	 *            maximum width
	 * @param height
	 *            maximum height
	 * @return the scaled image
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static Bitmap scaleLocalImage(File localFile, int width, int height)
			throws ClientProtocolException, IOException {

		if (DEBUG){
			Log.d(TAG, "scaleLocalImage(" + localFile + ", "+ width +", "+ height + ")");
		}

		if (!localFile.exists()) {
			throw new IOException("local file does not exist: " + localFile);
		}
		if (!localFile.canRead()) {
			throw new IOException("cannot read from local file: " + localFile);
		}

		// the below borrowed from:
		// https://github.com/thest1/LazyList/blob/master/src/com/fedorvlasov/lazylist/ImageLoader.java

		// decode image size
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;

		BitmapFactory.decodeStream(new FileInputStream(localFile), null, o);

		// Find the correct scale value. It should be the power of 2.
		//final int REQUIRED_WIDTH = width, REQUIRED_HEIGHT = height;
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 <= width || height_tmp / 2 <= height) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// decode with inSampleSize
		final BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		final Bitmap prescale = BitmapFactory.decodeStream(new FileInputStream(localFile), null, o2);

		if (prescale == null) {
			Log.e(TAG, localFile + " could not be decoded");
		}

		return prescale;
	}
	private static final boolean USE_APACHE_NC = true;

	/**
	 * Blocking call to download an image. The image is placed directly into the disk cache at the given key.
	 *
	 * @param uri
	 *            the location of the image
	 * @return a decoded bitmap
	 * @throws ClientProtocolException
	 *             if the HTTP response code wasn't 200 or any other HTTP errors
	 * @throws IOException
	 */
	private void downloadImage(String key, Uri uri) throws ClientProtocolException,
			IOException {

		if (DEBUG){
			Log.d(TAG, "downloadImage("+uri+")");
		}
		if (USE_APACHE_NC){
			final HttpGet get = new HttpGet(uri.toString());

			final HttpResponse hr = hc.execute(get);
			final StatusLine hs = hr.getStatusLine();
			if (hs.getStatusCode() != 200) {
				throw new HttpResponseException(hs.getStatusCode(),
						hs.getReasonPhrase());
			}

			final HttpEntity ent = hr.getEntity();


			try {
				putRaw(key, ent.getContent());

			} finally {
				ent.consumeContent();
			}
		}else{
			final URLConnection con = new URL(uri.toString()).openConnection();
			putRaw(key, con.getInputStream());
		}
	}

	/**
	 * Implement this and register it using
	 * {@link ImageCache#registerOnImageLoadListener(OnImageLoadListener)} to be
	 * notified when asynchronous image loads have completed.
	 *
	 * @author <a href="mailto:spomeroy@mit.edu">Steve Pomeroy</a>
	 *
	 */
	public interface OnImageLoadListener {
		/**
		 * Called when the image has been loaded and scaled.
		 *
		 * @param id the ID provided by {@link ImageCache#loadImage(long, Uri, int, int)} or {@link ImageCache#scheduleLoadImage(long, Uri, int, int)}
		 * @param imageUri the uri of the image that was originally requested
		 * @param image the loaded and scaled image
		 */
		public void onImageLoaded(long id, Uri imageUri, Drawable image);
	}
}
