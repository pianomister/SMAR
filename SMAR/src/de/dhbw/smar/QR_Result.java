package de.dhbw.smar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.zxing.integration.IntentIntegrator;
import com.google.zxing.integration.IntentResult;

public class QR_Result extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qr__result);
		IntentIntegrator Intent = new IntentIntegrator(this);
		Intent.initiateScan();
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult Result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (Result != null) {
			TextView articleNumber = (TextView)findViewById(R.id.tv_result_article);
			articleNumber.setText(Result.getContents());
			
		}
	}
	
}
