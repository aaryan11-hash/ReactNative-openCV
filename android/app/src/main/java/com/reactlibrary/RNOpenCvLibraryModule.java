package com.reactlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.opencv.imgcodecs.Imgcodecs;




public class RNOpenCvLibraryModule extends ReactContextBaseJavaModule {
  private final ReactApplicationContext reactContext;
  private String TAG="APP";

  public RNOpenCvLibraryModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNOpenCvLibrary";
  }

  @ReactMethod
  public void checkForBlurryImage(
    String imageAsBase64,
    Callback errorCallback,
    Callback successCallback
  ) {

    try {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inDither = true;
      options.inPreferredConfig = Bitmap.Config.ARGB_8888;

      byte[] decodedString = Base64.decode(imageAsBase64, Base64.DEFAULT);
      Bitmap image = BitmapFactory.decodeByteArray(
        decodedString,
        0,
        decodedString.length
      );

      //      Bitmap image = decodeSampledBitmapFromFile(imageurl, 2000, 2000);
      int l = CvType.CV_8UC1; //8-bit grey scale image
      Mat matImage = new Mat();
      Utils.bitmapToMat(image, matImage);
      Mat matImageGrey = new Mat();
      Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

      Bitmap destImage;
      destImage = Bitmap.createBitmap(image);
      Mat dst2 = new Mat();
      Utils.bitmapToMat(destImage, dst2);
      Mat laplacianImage = new Mat();
      dst2.convertTo(laplacianImage, l);
      Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
      Mat laplacianImage8bit = new Mat();
      laplacianImage.convertTo(laplacianImage8bit, l);

      Bitmap bmp = Bitmap.createBitmap(
        laplacianImage8bit.cols(),
        laplacianImage8bit.rows(),
        Bitmap.Config.ARGB_8888
      );
      Utils.matToBitmap(laplacianImage8bit, bmp);
      int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
      bmp.getPixels(
        pixels,
        0,
        bmp.getWidth(),
        0,
        0,
        bmp.getWidth(),
        bmp.getHeight()
      );
      int maxLap = -16777216; // 16m
      for (int pixel : pixels) {
        if (pixel > maxLap) maxLap = pixel;
      }

      int soglia = -6118750;
      // int soglia = -8118750;
      if (maxLap <= soglia) {
        System.out.println("is blur image");
      }

      successCallback.invoke(maxLap <= soglia);
    } catch (Exception e) {
      errorCallback.invoke(e.getMessage());
    }
  }
  public int safeLongToInt(long l) {
    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
        throw new IllegalArgumentException
            (l + " cannot be cast to int without changing its value.");
    }
    return (int) l;
}
    @ReactMethod
    public void meanBlurMethod(String imageAsBase64, Callback errorCallback,
    Callback successCallback){
     Log.d(TAG,imageAsBase64);
    Log.d(TAG,"OpenCv MBM Line 111");
        //ImageView ivImage, ivImageProcessed;
        Mat src=new Mat();
        System.out.println("Entering java func");
        int DELAY_CAPTION = 1500;
        int DELAY_BLUR = 100;
        int MAX_KERNEL_LENGTH = 31;
        
        //ImageView ivImage, ivImageProcessed;
          Mat dst;
  
        try{
          Log.d(TAG,"136");
          BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // byte[] decoded = Base64.getDecoder().decode(imageAsBase64);
          
        byte[] decodedString = Base64.decode(imageAsBase64, Base64.DEFAULT);
        // Mat mat = Imgcodecs.imdecode(new MatOfByte(decodedString), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        // Mat mat = new Mat(width, height, CvType.CV_8UC3);
        // mat.put(0, 0, decodedString);
        Bitmap image = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
       
        Utils.bitmapToMat(image, src);
        
        dst = new Mat(src.rows(), src.cols(), src.type());
        
       
  
        //Applying GaussianBlur on the Image
         //for (int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2) {
          Imgproc.GaussianBlur(src, dst, new Size(i,i), 0);
         //}
  
  
        Bitmap finalImage = Bitmap.createBitmap(dst.cols(),
        dst.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(dst, finalImage);
        Bitmap bitmap = (Bitmap) finalImage;
        bitmap = Bitmap.createScaledBitmap(bitmap, 600,500, false);

  
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        
        
        //byte[] img = dst.data().get(byte[].class);
        Log.d(TAG,encoded.toString());
        Log.d(TAG,"str"+encoded);
        successCallback.invoke(encoded);
  
        }catch(Exception e){
          Log.e(TAG,"error "+e.getStackTrace());
            errorCallback.invoke(e.getStackTrace().toString());
        }
    }
  }