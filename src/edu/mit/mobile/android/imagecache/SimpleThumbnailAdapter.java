package edu.mit.mobile.android.imagecache;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

public class SimpleThumbnailAdapter extends SimpleAdapter {
	private final Drawable defaultImages[];
	private final int[] mImageIDs;

	public SimpleThumbnailAdapter(Context context,
			List<? extends Map<String, ?>> data, int layout, String[] from,
			int[] to, int[]imageIDs) {
		super(context, data, layout, from, to);

		final View v = LayoutInflater.from(context)
				.inflate(layout, null, false);
		defaultImages = new Drawable[imageIDs.length];

		mImageIDs = imageIDs;

		for (int i = 0; i < mImageIDs.length; i++) {
			final ImageView thumb = (ImageView) v.findViewById(imageIDs[i]);
			defaultImages[i] = thumb.getDrawable();
		}

	}

	@Override
	public void setViewImage(ImageView v, String value) {
		final int id = v.getId();
		for (int i = 0; i < mImageIDs.length; i++) {
			if (id == mImageIDs[i]) {
				setViewImageAndTag(v, value, defaultImages[i]);
			}
		}
	}

	private void setViewImageAndTag(ImageView v, String value,
			Drawable defaultImage) {
		v.setImageDrawable(defaultImage);
		if (value != null && value.length() > 0) {
			v.setTag(Uri.parse(value));
		} else {
			v.setTag(null);
		}
	}

}
