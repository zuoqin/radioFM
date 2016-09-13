package fm100.co.il.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by leonidangarov on 30/11/15.
 */public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    Context ctx;

    public DownloadImageTask(ImageView bmImage, Context c) {
        this.bmImage = bmImage;
        this.ctx = c;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        /*String filename = urldisplay.substring(urldisplay.lastIndexOf("/") + 1);
        String path = urldisplay.substring(0, urldisplay.lastIndexOf("/") + 1);
        try {
            filename = URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urldisplay = path + filename;*/
        //Log.i("ufo", urldisplay);
        Bitmap mIcon11 = null;
        try {
            InputStream in = new URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }
    public Bitmap blur(Bitmap image) {
        if (null == image || this.ctx == null) return null;

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(this.ctx);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        //Intrinsic Gausian blur filter
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(15);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(blur(result));
    }
}