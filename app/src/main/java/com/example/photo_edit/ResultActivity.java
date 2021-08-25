package com.example.photo_edit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResultActivity extends AppCompatActivity {

    ImageView image;
    Uri uri;
    Button grayScale,AddText,savebtn;
    EditText editText;
    FileOutputStream outputStream=null;
    //Boolean toStart = true;
    //Canvas canvas;

    public Drawable drawable;
    Bitmap bitmap;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)

    //ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        image = findViewById(R.id.image);
        String s = getIntent().getStringExtra("gallery");
        grayScale = findViewById(R.id.grayScale);
        AddText = findViewById(R.id.AddText);
        editText = findViewById(R.id.editText);
        savebtn = findViewById(R.id.savebtn);
        uri = Uri.parse(s);
        Log.v("resting",s);

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            drawable = Drawable.createFromStream(inputStream,uri.toString());
        } catch (FileNotFoundException e){
            drawable = getResources().getDrawable(R.drawable.place);
        }
        bitmap = ((BitmapDrawable)drawable).getBitmap();
        image.setImageBitmap(bitmap);


        grayScale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap newbitmap = convertImage(bitmap);
                image.setImageBitmap(newbitmap);
            }
        });

        AddText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap processedBitmap = ProcessingBitmap();
                if (processedBitmap != null) {
                    image.setImageBitmap(processedBitmap);
                    Toast.makeText(getApplicationContext(),
                            "Done",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Something wrong in processing!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File filePath = Environment.getExternalStorageDirectory();
                File dir = new File(filePath.getAbsolutePath() + "/Photo Editor");
                dir.mkdir();
                String fileName = String.format("%d.jpg",System.currentTimeMillis());

                File file = new File(dir,fileName);
                Toast.makeText(getApplicationContext(),"Image Saved To Internal Storage!"
                        ,Toast.LENGTH_SHORT).show();
                try {
                    outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);

                    outputStream.flush();
                    outputStream.close();


                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(file));
                    sendBroadcast(intent);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            });
        }






    public  Bitmap convertImage(Bitmap original){

        int width;
        int height;
        height = original.getHeight();
        width = original.getWidth();
        Bitmap finalImage = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);

        Canvas c = new Canvas(finalImage);
        Paint paint = new Paint();
        ColorMatrix cm =new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(original,0,0,paint);
        return finalImage;

    }

    private Bitmap ProcessingBitmap(){
        Bitmap bm1 = null;
        Bitmap newBitmap = null;

        try {
            bm1 = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(uri));

            Bitmap.Config config = bm1.getConfig();
            if(config == null){
                config = Bitmap.Config.ARGB_8888;
            }

            newBitmap = Bitmap.createBitmap(bm1.getWidth(), bm1.getHeight(), config);
            Canvas newCanvas = new Canvas(newBitmap);

            newCanvas.drawBitmap(bm1, 0, 0, null);

            String captionString = editText.getText().toString();
            if(captionString != null){

                Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
                paintText.setColor(Color.BLUE);
                paintText.setTextSize(100);
                paintText.setStyle(Paint.Style.FILL);
                paintText.setShadowLayer(10f, 10f, 10f, Color.BLACK);

                Rect rectText = new Rect();
                paintText.getTextBounds(captionString, 0, captionString.length(), rectText);

                newCanvas.drawText(captionString,
                        0, rectText.height(), paintText);

                Toast.makeText(getApplicationContext(),
                        "drawText: " + captionString,
                        Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(getApplicationContext(),
                        "caption empty!",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return newBitmap;
    }


}
