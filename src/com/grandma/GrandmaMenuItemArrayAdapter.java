package com.grandma;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class GrandmaMenuItem {
	public String name;
	public String price;
	public byte[] picture;
}

public class GrandmaMenuItemArrayAdapter extends ArrayAdapter<GrandmaMenuItem> {
	private final Activity context;
	private final ArrayList<GrandmaMenuItem> grandmaMenuItems;
	private int resourceId;
	
	/**
	 * Constructor
	 * @param context - the application content
	 * @param resourceId - resourceId the ID of the resource/view
	 * @param grandmaMenuItems - friends the bound ArrayList
	 */
	public GrandmaMenuItemArrayAdapter(
			Activity context,
			int resourceId,
			ArrayList<GrandmaMenuItem> grandmaMenuItems) {
		super(context, resourceId, grandmaMenuItems);		
		this.context = context;
		this.grandmaMenuItems = grandmaMenuItems;
		this.resourceId = resourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if(rowView == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = vi.inflate(resourceId, null);
		}
		GrandmaMenuItem gmi = grandmaMenuItems.get(position);
		TextView rowTxt = (TextView)rowView.findViewById(R.id.rowtext_top);
		rowTxt.setText(gmi.name);
		//
		rowTxt = (TextView)rowView.findViewById(R.id.rowtext_bottom);
		rowTxt.setText(gmi.price);
		//
        ImageView dishLogo = (ImageView)rowView.findViewById(R.id.logoview);
        Resources res = context.getResources();
        int resourceId = res.getIdentifier(context.generatedString, "drawable", context.getPackageName() );
        imageView.setImageResource( resourceId );
        dishLogo.setImageDrawable(resourceId);
		return rowView;
	}
}