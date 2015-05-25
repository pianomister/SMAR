package de.dhbw.smar.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import de.dhbw.smar.R;

public class DialogHelper extends DialogFragment{
	
	public interface ShareDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String amount);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.unit_amount_layout, null); 
        //final EditText name_place =    (EditText)v.findViewById(R.id.sharePlaceName);
        final EditText amount = (EditText)v.findViewById(R.id.txt_amount);
        builder.setView(v)
                
               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                      // String name = name_place.getText().toString();
                       String samount = amount.getText().toString();
                       mListener.onDialogPositiveClick(DialogHelper.this, samount);

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
