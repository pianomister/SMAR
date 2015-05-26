package de.dhbw.smar;

import java.io.File;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import de.dhbw.smar.helpers.FileHelper;
import de.dhbw.smar.svg.SVGObject;

public class SVGTestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_svgtest);
		
		//TODO remove: test for SVG
        String stringSVG = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\"><svg width=\"600\" height=\"180\" viewBox=\"0 0 600 180\" style=\"width:100%;height: auto;\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><title>Shelf 'Softgetränke' (ID: 2, last updated: '22.04.2015 02:01:35')</title><defs><style type=\"text/css\">		<![CDATA[		text {fill: #333;font-family:Roboto;font-size: 8px;}		rect {fill:#ccc; stroke:#777; stroke-width: 2px;}		.section {fill:#ddd; stroke:#555; stroke-width: 1px; opacity:.8;}		.selected {fill:#16a082;}		]]></style></defs><rect id=\"shelf2\" x=\"0\" y=\"0\" width=\"600\" height=\"180\" /><rect id=\"section1\" x=\"110\" y=\"0\" width=\"110\" height=\"60\" class=\"section\"/><text x=\"115\" y=\"15\">1: Likör</text><rect id=\"section2\" x=\"110\" y=\"60\" width=\"110\" height=\"120\" class=\"section\"/><text x=\"115\" y=\"75\">2: Aufstrich</text><rect id=\"section4\" x=\"0\" y=\"0\" width=\"110\" height=\"180\" class=\"section\"/><text x=\"5\" y=\"15\">4: Test-Sektion</text><rect id=\"section8\" x=\"220\" y=\"0\" width=\"110\" height=\"180\" class=\"section\"/><text x=\"225\" y=\"15\">8: Wein</text></svg>";
        SVGObject objectSVG = new SVGObject(stringSVG);
        
        ImageView svgImage = (ImageView) findViewById(R.id.svgTest2);
        // disable hardware acceleration - causes errors (incompatibility)
        svgImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        Drawable d = objectSVG.getDrawable();
        if(d != null)
        	svgImage.setImageDrawable(d);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.svgtest, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void writeFile() {
    	
        String sFilename = "test.txt";
        String sFilename2 = "test.txt";
        String sString = "Schreib mal rein hier :)";
        String sString2 = "Versuch zwei";
        
        FileHelper.writeFile(this, sFilename, sString);
        File result = FileHelper.readFile(this, sFilename);
        System.out.println("TEST1: " + FileHelper.getFileContents(result));
        
        FileHelper.writeFile(this, sFilename2, sString2);
        File result2 = FileHelper.readFile(this, sFilename2);
        System.out.println("TEST2: " + FileHelper.getFileContents(result2));
        
    }
}
