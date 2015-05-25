package de.dhbw.smar;

import de.dhbw.smar.barcode.CameraPreview;
import net.sourceforge.zbar.Config;  
import net.sourceforge.zbar.Image;  
import net.sourceforge.zbar.ImageScanner;  
import net.sourceforge.zbar.Symbol;  
import net.sourceforge.zbar.SymbolSet;  
import android.app.Activity;  
import android.app.ProgressDialog;
import android.content.Intent;  
import android.content.pm.ActivityInfo;  
import android.hardware.Camera;  
import android.hardware.Camera.AutoFocusCallback;  
import android.hardware.Camera.PreviewCallback;  
import android.hardware.Camera.Size;  
import android.os.Bundle;  
import android.os.Handler;  
import android.widget.FrameLayout;  
import android.widget.TextView;
  
public class BarcodeScannerActivity extends Activity{  
  
 private Camera mCamera;  
    private CameraPreview mPreview;  
    private Handler autoFocusHandler;  
      
    ImageScanner scanner;  
    private boolean barcodeScanned = false;  
    private boolean previewing = true;
    
    static {
        System.loadLibrary("iconv");
    }
      
    public void onCreate(Bundle savedInstanceState)   
    {  
        setContentView(R.layout.activity_barcode_scanner);  
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  
          
        //getActionBar().hide();  
    
        autoFocusHandler = new Handler();  
        mCamera = getCameraInstance();  
        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        /*// Instance barcode scanner  
        scanner = new ImageScanner();  
        scanner.setConfig(0, Config.X_DENSITY, 400);  
        scanner.setConfig(0, Config.Y_DENSITY, 400);  
        scanner.setConfig(0, Config.ENABLE, 1);  
        scanner.setConfig(Symbol.EAN13, Config.ENABLE,1);  
        scanner.setConfig(Symbol.EAN8, Config.ENABLE,1);  
        scanner.setConfig(Symbol.UPCA, Config.ENABLE,1);  
        scanner.setConfig(Symbol.UPCE, Config.ENABLE,1);
        scanner.setConfig(Symbol.QRCODE, Config.ENABLE,1); */
          
        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);  
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);  
        preview.addView(mPreview);  
          
        super.onCreate(savedInstanceState);  
    }  
  
      
    /** A safe way to get an instance of the Camera object. */  
    public static Camera getCameraInstance()  
    {  
        Camera c = null;  
        try   
        {  
            c = Camera.open();  
        } catch (Exception e)  
        {  
          e.printStackTrace();
        }  
        return c;  
    }  
   
    private void releaseCamera()   
    {  
        if (mCamera != null)   
        {  
            previewing = false;  
            mCamera.setPreviewCallback(null);  
            mCamera.release();  
            mCamera = null;  
        }  
    }  
  
     
      
    PreviewCallback previewCb = new PreviewCallback()   
    {  
        public void onPreviewFrame(byte[] data, Camera camera)   
        {  
            Camera.Parameters parameters = camera.getParameters();  
            Size size = parameters.getPreviewSize();  
            Image barcode = new Image(size.width, size.height, "Y800");  
            barcode.setData(data);  
            int result = scanner.scanImage(barcode);  
            if (result != 0)   
            {  
                previewing = false;  
                mCamera.setPreviewCallback(null);  
                mCamera.stopPreview();  
                SymbolSet syms = scanner.getResults();  
                for (Symbol sym : syms)   
                {  
                    barcodeScanned = true;  
                      
                    Intent returnIntent = new Intent();  
                    returnIntent.putExtra("BARCODE", sym.getData());  
                    setResult(1,returnIntent);  
                    releaseCamera();
                    final TextView mTextView = (TextView) findViewById(R.id.tv_product);
                    mTextView.setText(sym.getData());
                    // finish();  
                }  
            }  
        }  
    };  
      
    // Mimic continuous auto-focusing  
    AutoFocusCallback autoFocusCB = new AutoFocusCallback()   
    {  
        public void onAutoFocus(boolean success, Camera camera)   
        {  
            autoFocusHandler.postDelayed(doAutoFocus, 1000);  
        }  
    };  
      
    private Runnable doAutoFocus = new Runnable()   
    {  
        public void run()   
        {  
            if (previewing)  
                mCamera.autoFocus(autoFocusCB);  
        }  
    };  
      
    public void onPause()   
    {  
        super.onPause();  
        releaseCamera();  
    }  
  
    @Override  
    public void onBackPressed() {  
       
     releaseCamera();  
     Intent intent = new Intent();  
        intent.putExtra("BARCODE","NULL");  
        setResult(RESULT_OK, intent);  
        super.onBackPressed();  
    }  
  
      
}  
