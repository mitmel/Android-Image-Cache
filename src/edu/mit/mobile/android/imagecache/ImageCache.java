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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.commonsware.cwac.task.AsyncTaskEx;

/**
 * An image download-and-cache that also knows how to efficiently generate thumbnails of various sizes.
 *
 * @author <a href="mailto:spomeroy@mit.edu">Steve Pomeroy</a>
 *
 */
public class ImageCache extends DiskCache<String, Bitmap> {
	private static final String TAG = ImageCache.class.getSimpleName();

	private final HashSet<OnImageLoadListener> mImageLoadListeners = new HashSet<ImageCache.OnImageLoadListener>();

	private final HashMap<String, SoftReference<Bitmap>> mMemCache = new HashMap<String, SoftReference<Bitmap>>();

	private long mIDCounter = 0;

	private static ImageCache mInstance;

	private final HttpClient hc;

	private final CompressFormat mCompressFormat;
	private final int mQuality;

	// TODO make it so this is customizable on the instance level.
	public static ImageCache getInstance(Context context){
		if (mInstance == null){
			mInstance = new ImageCache(context, CompressFormat.JPEG, 85);
		}
		return mInstance;
	}

	private ImageCache(Context context, CompressFormat format, int quality) {
		super(context.getCacheDir(), null, getExtension(format));
		hc = getHttpClient();

		mCompressFormat = format;
		mQuality = quality;
	}

	private static String getExtension(CompressFormat format){
		String extension;
		switch(format){
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


	public synchronized long getNewID(){
		return mIDCounter++;
	}

	@Override
	protected Bitmap fromDisk(String key, InputStream in) {
		final SoftReference<Bitmap> memCached = mMemCache.get(key);
		if (memCached != null){
			final Bitmap bitmap = memCached.get();
			if (bitmap != null){
				Log.d(TAG, "mem cache hit");
				return bitmap;
			}
		}
		Log.d(TAG, "disk cache hit");
		try {
			final Bitmap image = BitmapFactory.decodeStream(in);
			mMemCache.put(key, new SoftReference<Bitmap>(image));
			return image;

		}catch (final OutOfMemoryError oom){
			oomClear();
			return null;
		}
	}

	@Override
	protected void toDisk(String key, Bitmap image, OutputStream out) {
		mMemCache.put(key, new SoftReference<Bitmap>(image));

		Log.d(TAG, "cache write for key "+key);
		if (image != null){
			image.compress(mCompressFormat, mQuality, out);
		}else{
			Log.e(TAG, "attempting to write null image to cache");
		}
	}

	/**
	 * Gets an instance of AndroidHttpClient if the devices has it (it was introduced in 2.2),
	 * or falls back on a http client that should work reasonably well.
	 *
	 * @return a working instance of an HttpClient
	 */
	private HttpClient getHttpClient(){
		HttpClient ahc;
		try {
			@SuppressWarnings("rawtypes")
			final Class ahcClass = Class.forName("android.net.http.AndroidHttpClient");
			final Method newInstance = ahcClass.getMethod("newInstance", String.class);
			ahc = (HttpClient) newInstance.invoke(null, "ImageCache");

		} catch (final ClassNotFoundException e) {
			DefaultHttpClient dhc = new DefaultHttpClient();
			final HttpParams params = dhc.getParams();
			dhc = null;

	        final SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http",PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https",PlainSocketFactory.getSocketFactory(), 80));

	        final ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
	        ahc = new DefaultHttpClient(manager, params);

		} catch (final NoSuchMethodException e) {

			final RuntimeException re = new RuntimeException("Programming error");
			re.initCause(e);
			throw re;

		} catch (final IllegalAccessException e) {
			final RuntimeException re = new RuntimeException("Programming error");
			re.initCause(e);
			throw re;

		} catch (final InvocationTargetException e) {
			final RuntimeException re = new RuntimeException("Programming error");
			re.initCause(e);
			throw re;
		}
		return ahc;
	}

	public void registerOnImageLoadListener(OnImageLoadListener onImageLoadListener){
		mImageLoadListeners.add(onImageLoadListener);
	}

	public void unregisterOnImageLoadListener(OnImageLoadListener onImageLoadListener){
		mImageLoadListeners.remove(onImageLoadListener);
	}


	private class LoadResult{
		public LoadResult(long id, Uri image, Bitmap bitmap) {
			this.id = id;
			this.bitmap = bitmap;
			this.image = image;
		}
		final Uri image;
		final long id;
		final Bitmap bitmap;
	}

	public String getKey(Uri uri){
		return uri.toString();
	}

	public String getKey(Uri uri, int width, int height){
		return uri.buildUpon().appendQueryParameter("width", String.valueOf(width)).appendQueryParameter("height", String.valueOf(height)).build().toString();
	}

	private class ImageLoadTask extends AsyncTaskEx<Object, Void, LoadResult>{

		@Override
		protected LoadResult doInBackground(Object... params) {
			final long id = (Long)params[0];
			final Uri uri = (Uri) params[1];
			final int width = (Integer) params[2];
			final int height = (Integer) params[3];

			final String scaledKey = getKey(uri, width, height);

			try {
				Bitmap bmp = ImageCache.this.get(scaledKey);
				if (bmp == null){
					if ("file".equals(uri.getScheme())){
						bmp = scaleLocalImage(new File(uri.getPath()), width, height);
					}else{
						final String sourceKey = getKey(uri);
						Bitmap sourceBitmap = ImageCache.this.get(sourceKey);
						if (sourceBitmap == null){
							sourceBitmap = downloadImage(uri);
							ImageCache.this.put(sourceKey, sourceBitmap);
						}
						if (sourceBitmap != null){
							bmp = scaleBitmapPreserveAspect(sourceBitmap, width, height);
						}
					}
					if (bmp != null){
						Log.e(TAG, "got null bitmap for request to scale "+uri);
						ImageCache.this.put(scaledKey, bmp);
					}
				}

				return new LoadResult(id, uri, bmp);

			// TODO this exception came about, no idea why: java.lang.IllegalArgumentException: Parser may not be null
			} catch (final IllegalArgumentException e){
				e.printStackTrace();
			} catch (final OutOfMemoryError oom){
				oomClear();
			} catch (final ClientProtocolException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return null;
		};

		@Override
		protected void onPostExecute(LoadResult result) {
			if (result == null){
				return;
			}

			for (final OnImageLoadListener listener: mImageLoadListeners){
				listener.onImageLoaded(result.id, result.image, result.bitmap);
			}
		};
	}

	private void oomClear(){
		Log.w(TAG, "out of memory, clearing mem cache");
		mMemCache.clear();
	}

	/**
	 * Checks the cache for an image matching the given criteria and returns it. If it isn't immediately available, calls {@link #scheduleLoadImage}
	 *
	 * @param id
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	public Bitmap loadImage(long id, Uri image, int width, int height){
		final Bitmap res = get(getKey(image, width, height));
		if (res == null){
			scheduleLoadImage(id, image, width, height);
		}
		return res;
	}

	/**
	 * Schedules a load of the given image. Will call the {@link OnImageLoadListener} with the loaded bitmap when successfully loaded from the network.
	 *
	 * @param id An ID for you to keep track of your image load requests.
	 *
	 * @param image
	 * @param width
	 * @param height
	 */
	public void scheduleLoadImage(long id, Uri image, int width, int height){
		final ImageLoadTask imt = new ImageLoadTask();

		imt.execute(id, image, width, height);

	}

	public void cancelLoads(){
		ImageLoadTask.clearQueue();
	}

	private Bitmap scaleLocalImage(File localFile, int width, int height) throws ClientProtocolException, IOException {

		Bitmap bmp;

		final Bitmap prescale = BitmapFactory.decodeFile(localFile.getAbsolutePath());
		if (prescale != null){
			bmp = scaleBitmapPreserveAspect(prescale, width, height);
			if (prescale != bmp){
				prescale.recycle();
			}
		}else{
			bmp = null;
		}

		return bmp;
	}

	private Bitmap downloadImage(Uri uri) throws ClientProtocolException, IOException{

		final HttpGet get = new HttpGet(uri.toString());

		final HttpResponse hr = hc.execute(get);
		final StatusLine hs = hr.getStatusLine();
		if (hs.getStatusCode() != 200){
			throw new HttpResponseException(hs.getStatusCode(), hs.getReasonPhrase());
		}

		final HttpEntity ent = hr.getEntity();
		Bitmap bmp;

		try {
			bmp = BitmapFactory.decodeStream(ent.getContent());

		}finally{
			ent.consumeContent();
		}
		return bmp;
	}

	/**
	 * Scale a bitmap so that it fits within the specified width and height, preserving its aspect ratio.
	 *
	 * @param bmap The input bitmap to be scaled. If the image would be upscaled, this bitmap is returned without scaling.
	 * @return an image that's scaled to be at most the given width/height.
	 */
	private Bitmap scaleBitmapPreserveAspect(Bitmap bmap, int width, int height){
        	if (bmap == null || height == 0 || width == 0){
        		return null;
        	}

        	final int origWidth = bmap.getWidth();
        	final int origHeight = bmap.getHeight();
        	final float scaleWidth = (float)height / origWidth;
        	final float scaleHeight = (float)width / origHeight;
        	final float scale = Math.min(scaleWidth, scaleHeight);

        	// prevent upscaling, as the drawable will happily take care of this for us.
        	if (scale < 1){
        		final Bitmap scaled = Bitmap.createScaledBitmap(bmap, (int)(origWidth * scale), (int)(origHeight * scale), true);
        		bmap = scaled;
        	}

    		return bmap;
	}



	public interface OnImageLoadListener {
		public void onImageLoaded(long id, Uri imageUri, Bitmap image);
	}
}
