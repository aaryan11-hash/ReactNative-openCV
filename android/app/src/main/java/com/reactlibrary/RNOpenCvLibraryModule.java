package com.reactlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.logging.Logger;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class RNOpenCvLibraryModule extends ReactContextBaseJavaModule {
  private final ReactApplicationContext reactContext;

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
    public void meanBlurMethod(String imageAsBase64, Callback errorCallback, Callback successCallback){
        System.out.println("Entering java func");
        //ImageView ivImage, ivImageProcessed;
        Mat src;
        
        try {

            //loaded the binary image string
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            byte[] decodedString = Base64.decode(imageAsBase64, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            
            src = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
            Utils.bitmapToMat(image,src);
            System.out.println("123 JAVA");
            Imgproc.blur(src,src,new Size(3,3));

            Bitmap processedImage = Bitmap.createBitmap(src.cols(),src.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(src,processedImage);
            // ivImage.setImageBitmap(selectedImage);
            // ivImageProcessed.setImageBitmap(processedImage);
            
            //APPROACH 1
            // int length = (int) (src.total()*src.elemSize());
            // byte buffer[] = new byte[length];
            // int converted = src.get(0, 0, buffer);
        
            //APPROACH 2
            byte[] imageInBytes = new byte[(safeLongToInt(src.total())) * src.channels()];
            src.get(0, 0, imageInBytes);
            String s=java.util.Base64.getEncoder().encodeToString(imageInBytes);
            System.out.println(s);
            successCallback.invoke(s);
        
        }catch(Exception e){

            errorCallback.invoke(e.getMessage());
        }

       

    }
  }