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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCacheException;

public class ImageCacheJunitTest extends InstrumentationTestCase {
    @SuppressWarnings("unused")
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

    public void testGetPut() throws IOException {
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

    private void assertBitmapMaxSize(int maxExpectedWidth, int maxExpectedHeight, Drawable actual){
        assertTrue(maxExpectedWidth >= actual.getIntrinsicWidth());
        assertTrue(maxExpectedHeight >= actual.getIntrinsicHeight());

    }

    private void assertBitmapMinSize(int minExpectedWidth, int minExpectedHeight, Drawable actual){
        assertTrue(minExpectedWidth <= actual.getIntrinsicWidth());
        assertTrue(minExpectedHeight <= actual.getIntrinsicHeight());

    }

    private void assertBitmapEqual(Bitmap expected, Bitmap actual){
        assertEquals(expected.getHeight(), actual.getHeight());
        assertEquals(expected.getWidth(), actual.getWidth());
    }

    static final int LOCAL_SCALE_SIZE = 100;

    public void testLocalFileLoad() throws IOException, ImageCacheException {
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

        assertNotNull(fileUri);

        final Drawable img = imc.getImage(fileUri, LOCAL_SCALE_SIZE, LOCAL_SCALE_SIZE);

        assertNotNull(img);

        // the thumbnails produced by this aren't precisely the size we request, due to efficiencies
        // in decoding the image.
        assertBitmapMaxSize(LOCAL_SCALE_SIZE*2, LOCAL_SCALE_SIZE*2, img);

        assertBitmapMinSize(LOCAL_SCALE_SIZE/2, LOCAL_SCALE_SIZE/2, img);

    }

    private final int NET_SCALE_SIZE = 100;

    private void testNetworkLoad(Uri uri) throws IOException, ImageCacheException{


        // ensure we don't have it in the cache
        final String origKey = imc.getKey(uri);
        assertNull(imc.getDrawable(origKey));

        final String scaledKey = imc.getKey(uri, NET_SCALE_SIZE, NET_SCALE_SIZE);
        assertNull(imc.getDrawable(scaledKey));

        final Drawable img = imc.getImage(uri, NET_SCALE_SIZE, NET_SCALE_SIZE);

        assertNotNull(img);

        assertBitmapMaxSize(NET_SCALE_SIZE*2, NET_SCALE_SIZE*2, img);

        assertBitmapMinSize(NET_SCALE_SIZE/2, NET_SCALE_SIZE/2, img);

        // ensure that it's stored in the disk cache
        assertNotNull(imc.get(origKey));
        assertNotNull(imc.get(scaledKey));

    }

    public void testNetworkLoad() throws ClientProtocolException, IOException, ImageCacheException {
        testClear();

        testNetworkLoad(Uri.parse("http://mobile-server.mit.edu/~stevep/logo_start_locast1.png"));
    }

    public void testNetworkLoadLarge() throws ClientProtocolException, IOException, ImageCacheException {
        testClear();

        testNetworkLoad(Uri.parse("http://mobile-server.mit.edu/~stevep/large_logo.png"));
    }
}
