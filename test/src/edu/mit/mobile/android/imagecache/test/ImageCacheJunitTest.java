package edu.mit.mobile.android.imagecache.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import edu.mit.mobile.android.imagecache.ImageCache;

public class ImageCacheJunitTest extends AndroidTestCase {

	private ImageCache imc;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		imc = ImageCache.getInstance(getContext());
	}

	public void testPreconditions(){
		assertNotNull(imc);
	}

	public void testClear(){
		assertTrue(imc.clear());
		assertEquals(0, imc.getCacheSize());
	}

	public void testGetPut(){

		testClear();

		final String key01 = "foo";
		final String key02 = "bar";
		Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.icon);
		assertTrue(bmp.getHeight() > 0);
		assertTrue(bmp.getWidth() > 0);

		imc.put(key01, bmp);

		assertEquals(1, imc.getCacheSize());

		Bitmap bmpResult = imc.get(key01);
		assertNotNull(bmpResult);

		// check dimensions
		assertEquals(bmp.getHeight(), bmpResult.getHeight());
		assertEquals(bmp.getWidth(), bmpResult.getWidth());

		// check contents to ensure it's the same
		// TODO

		bmp = BitmapFactory.decodeResource(getContext().getResources(), android.R.drawable.ic_dialog_alert);

		assertTrue(bmp.getHeight() > 0);
		assertTrue(bmp.getWidth() > 0);

		// call it again, ensure we overwrite
		imc.put(key01, bmp);
		assertEquals(1, imc.getCacheSize());

		bmpResult = imc.get(key01);
		assertNotNull(bmpResult);

		// check dimensions
		assertEquals(bmp.getHeight(), bmpResult.getHeight());
		assertEquals(bmp.getWidth(), bmpResult.getWidth());

		// test to make sure an empty result returns null
		assertNull(imc.get(key02));

		testClear();
	}
/*
	public void testNetworkLoad(){
		testClear();

		final long id = 31337;

		final Bitmap bmp = imc.loadImage(id, Uri.parse("http://locast.mit.edu/images/logo_start_locast1.png"), 800, 800);

		assertNull(bmp); // we shouldn't have it already in the cache

		imc.registerOnImageLoadListener(new OnImageLoadListener() {

			@Override
			public void onImageLoaded(long id, Uri imageUri, Bitmap image) {
				// TODO Auto-generated method stub

			}
		});



	}
*/
}
