package edu.mit.mobile.android.imagecache.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.util.Log;
import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;

public class ImageCacheJunitTest extends InstrumentationTestCase implements OnImageLoadListener {
	private static final String TAG = ImageCacheJunitTest.class.getSimpleName();

	private ImageCache imc;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		imc = ImageCache.getInstance(getInstrumentation().getTargetContext());
	}

	public void testPreconditions(){
		assertNotNull(imc);
	}

	public void testClear(){
		assertTrue(imc.clear());
		assertEquals(0, imc.getCacheSize());
	}

	public void testGetPut(){
		final Context contextInst = getInstrumentation().getContext();
		testClear();

		final String key01 = "foo";
		final String key02 = "bar";
		Bitmap bmp = BitmapFactory.decodeResource(contextInst.getResources(), R.drawable.icon);
		assertTrue(bmp.getHeight() > 0);
		assertTrue(bmp.getWidth() > 0);

		imc.put(key01, bmp);

		assertEquals(1, imc.getCacheSize());

		Bitmap bmpResult = imc.get(key01);
		assertNotNull(bmpResult);

		// check dimensions
		assertBitmapEqual(bmp, bmpResult);

		// check contents to ensure it's the same
		// TODO

		bmp = BitmapFactory.decodeResource(contextInst.getResources(), android.R.drawable.ic_dialog_alert);

		assertTrue(bmp.getHeight() > 0);
		assertTrue(bmp.getWidth() > 0);

		// call it again, ensure we overwrite
		imc.put(key01, bmp);
		assertEquals(1, imc.getCacheSize());

		bmpResult = imc.get(key01);
		assertNotNull(bmpResult);

		// check dimensions
		assertBitmapEqual(bmp, bmpResult);

		// test to make sure an empty result returns null
		assertNull(imc.get(key02));

		testClear();
	}

	private void assertBitmapSize(int expectedWidth, int expectedHeight, Bitmap actual){
		assertEquals(expectedWidth, actual.getWidth());
		assertEquals(expectedHeight, actual.getHeight());

	}

	private void assertBitmapMaxSize(int maxExpectedWidth, int maxExpectedHeight, Bitmap actual){
		assertTrue(maxExpectedWidth >= actual.getWidth());
		assertTrue(maxExpectedHeight >= actual.getHeight());

	}

	private void assertBitmapEqual(Bitmap expected, Bitmap actual){
		assertEquals(expected.getHeight(), actual.getHeight());
		assertEquals(expected.getWidth(), actual.getWidth());
	}

	public void testLocalFileLoad() throws IOException {
		testClear();

		final String testfile = "logo_locast.png";
		final Context contextInst = getInstrumentation().getContext();
		final Context context = getInstrumentation().getTargetContext();
		final InputStream is = contextInst.getAssets().open(testfile);

		assertNotNull(is);

		final FileOutputStream fos = context.openFileOutput(testfile, Context.MODE_PRIVATE);

		assertNotNull(fos);

		int read=0;
		final byte[] bytes = new byte[1024];

		while((read = is.read(bytes))!= -1){
			fos.write(bytes, 0, read);
		}

		is.close();
		fos.close();

		final File outFile = context.getFileStreamPath(testfile);

		final Uri fileUri = Uri.fromFile(outFile);
		imc.registerOnImageLoadListener(this);

		assertNotNull(fileUri);

		final Bitmap gotBmp = imc.loadImage(0, fileUri, 100, 100);

		if (gotBmp == null){
			Log.d(TAG, "image was null, so waiting for it to load");
			//gotBmp = waitForImage(0);
		}

//		assertNotNull(gotBmp);

	//	assertBitmapMaxSize(100, 100, gotBmp);

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

	private long gotID = -1;
	private Bitmap gotImage;

	private Bitmap waitForImage(long id){
		try {
			Log.d(TAG, "waiting for image");
			wait(5000);
		} catch (final InterruptedException e) {
			// ok!
		}
		Log.d(TAG, "waitForImage done waiting");
		notifyAll();
		Log.d(TAG, "WaitForImage notified");

		gotID = -1;
		final Bitmap retval = gotImage;
		gotImage = null;
		return retval;
	}

	@Override
	public void onImageLoaded(long id, Uri imageUri, Bitmap image) {
		Log.d(TAG, "onImageLoaded");
//		try {
//			wait();
//		} catch (final InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Log.d(TAG, "onImageLoaded done waiting");

		gotImage = image;
		gotID = id;


//		notifyAll();
		Log.d(TAG, "onImageLoaded notified");
	}
}
