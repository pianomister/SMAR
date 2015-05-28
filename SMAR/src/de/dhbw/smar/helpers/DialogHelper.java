package de.dhbw.smar.helpers;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import de.dhbw.smar.R;

public class DialogHelper extends DialogFragment{
	
	CustomSpinner spinnerAdapter;
	
	public interface ShareDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String amount, int index, String textOfSpinner);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	

	
    ShareDialogListener mListener;
    private Handler mResponseHandler;
    
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
           mListener = (ShareDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ShareDialogListener");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	super.onCreateDialog(savedInstanceState);
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	builder.setTitle("Select unit and amount");
    	
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.unit_amount_layout, null); 
        
        //get the TextView and the Spinner
        final Spinner spinner = (Spinner)v.findViewById(R.id.spinner_unit);
        final EditText amount = (EditText)v.findViewById(R.id.txt_amount_unit);
        
        ArrayList<String> all_units_list = getArguments().getStringArrayList("all_units");
        
        
        //too add
        //all posibilities
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, all_units_list);

        spinner.setAdapter(spinnerAdapter);
        
        builder.setView(v)
                
               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	   String give_amount =  amount.getText().toString();
                       mListener.onDialogPositiveClick(DialogHelper.this, give_amount, spinner.getSelectedItemPosition(), spinner.getSelectedItem().toString());
                     
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogNegativeClick(DialogHelper.this);
                       DialogHelper.this.getDialog().cancel();
                   }
               });      
        return builder.create();
}
}
