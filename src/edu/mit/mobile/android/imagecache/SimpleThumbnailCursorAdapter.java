package edu.mit.mobile.android.imagecache;
/*
 * Copyright (C) 2011 MIT Mobile Experience Lab
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Like the SimpleCursorAdapter, but has support for setting thumbnails that can
 * be loaded using a wrapped {@link ImageLoaderAdapter}.
 *
 * Multiple image fields can be mapped to the same image view and the first one
 * with a valid image will be loaded.
 *
 * Additionally, it shows an indeterminate progress bar when the adapter is
 * null. This progress bar will go away once the adapter has been set.
 *
 * @author steve
 *
 */
public class SimpleThumbnailCursorAdapter extends SimpleCursorAdapter {

	private final Drawable defaultImages[];
	private final int[] mImageIDs;
	// XXX HACK the alternate images system is sorta a hack.
	private final HashMap<Integer, List<String>> mAlternateImages = new HashMap<Integer, List<String>>();

	private final Context mContext;

	private boolean mShowIndeterminate = false;

	/**
	 * All parameters are passed directly to {@link SimpleCursorAdapter}
	 * {@link #SimpleThumbnailCursorAdapter(Context, int, Cursor, String[], int[], int[], int)}
	 *
	 * @param context
	 * @param layout
	 * @param c
	 * @param from
	 *            You can load alternate images by specifying multiple from IDs
	 *            mapping to the same TO ID. This only works for images, as
	 *            listed below.
	 * @param to
	 * @param imageIDs
	 *            a list of ImageView IDs whose images will be loaded by this
	 *            adapter.
	 * @param flags
	 */
	public SimpleThumbnailCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int[] imageIDs, int flags) {
		super(context, layout, c, from, to, flags);

		mContext = context;

		for (final int imageID : imageIDs) {
			final List<String> alternates = new ArrayList<String>();
			mAlternateImages.put(imageID, alternates);
			for (int i = 0; i < to.length; i++) {
				if (to[i] == imageID) {
					alternates.add(from[i]);
				}
			}

			setIndeterminateLoading(c == null);
		}

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
				final List<String> alternates = mAlternateImages.get(id);
				if (alternates != null && alternates.size() > 1) {
					final Cursor c = getCursor();
					for (final String alternate : alternates) {
						final int idx = c.getColumnIndex(alternate);
						if (c.isNull(idx)) {
							continue;
						} else {
							// only set the first one that isn't null
							setViewImageAndTag(v, c.getString(idx),
									defaultImages[i]);
							break;
						}
					}
				} else {
					setViewImageAndTag(v, value, defaultImages[i]);
				}
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

	private void setIndeterminateLoading(boolean isLoading) {
		if (isLoading != mShowIndeterminate) {
			mShowIndeterminate = isLoading;
			notifyDataSetInvalidated();
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		if (mShowIndeterminate) {
			return 1;
		} else {
			return super.getCount();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mShowIndeterminate) {
			return LayoutInflater.from(mContext).inflate(R.layout.list_loading,
					parent, false);

		} else {
			// ensure that we don't reuse the indeterminate progress bar as a
			// real view
			if (convertView != null && convertView.getId() == R.id.progress) {
				convertView = null;
			}
			return super.getView(position, convertView, parent);
		}
	}

	@Override
	public Cursor swapCursor(Cursor c) {
		setIndeterminateLoading(c == null);
		return super.swapCursor(c);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		setIndeterminateLoading(cursor == null);
	}
}
