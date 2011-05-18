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
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class SimpleThumbnailCursorAdapter extends SimpleCursorAdapter {

	private final Drawable defaultImages[];
	private final int[] mImageIDs;

	public SimpleThumbnailCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int[] imageIDs, int flags) {
		super(context, layout, c, from, to, flags);

		final View v = LayoutInflater.from(context).inflate(layout, null, false);
		defaultImages = new Drawable[imageIDs.length];

		mImageIDs = imageIDs;

		for (int i = 0; i < mImageIDs.length; i++){
			final ImageView thumb = (ImageView)v.findViewById(imageIDs[i]);
			defaultImages[i] = thumb.getDrawable();
		}
	}

	@Override
	public void setViewImage(ImageView v, String value) {
		final int id = v.getId();
		for (int i = 0; i < mImageIDs.length; i++){
			if (id == mImageIDs[i]){
				v.setImageDrawable(defaultImages[i]);
				if (value != null && value.length() > 0){
					v.setTag(Uri.parse(value));
				}else{
					v.setTag(null);
				}
			}
		}
	}
}