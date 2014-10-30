package com.easyfileshare;

import java.util.ArrayList;

import com.easyfileshare.R;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<String> {
	  private final Context context;
	  private final ArrayList<String> values;

	  public ListAdapter(Context context, ArrayList<String> values) {
	    super(context, R.layout.contents_row, values);
	    this.context = context;
	    this.values = new ArrayList<String>();
	    for(int i=0; i<values.size(); i++)
	    {
	    	this.values.add(values.get(i));
	    }
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.contents_row, parent, false);
	    TextView contentText = (TextView) rowView.findViewById(R.id.contentText);
	    contentText.setText(values.get(position));
	    return rowView;
	  }
}
