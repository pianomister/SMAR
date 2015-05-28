package de.dhbw.smar.helpers;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomSpinner extends ArrayAdapter<UnitHelper> {
	
	Context context;
	ArrayList<UnitHelper> units;
	
	public CustomSpinner(Context context, int textViewResourceId, ArrayList<UnitHelper> units) {
		super(context, textViewResourceId, units);
		this.context = context;
		this.units = units;
	}
	
	public int getLength() {
		return this.units.size();
	}
	
	public UnitHelper getUnit(int pos) {
		return units.get(pos);
	}
	
	public long getItemID(int pos) {
		return pos;	
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        
        label.setText(units.get(position).getName());
        return label;
	}
	
	@Override
    public View getDropDownView(int position, View convertView,
            ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(units.get(position).getName());

        return label;
    }
	
}
