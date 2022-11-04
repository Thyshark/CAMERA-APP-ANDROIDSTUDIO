package com.example.camera;

import static android.os.Environment.getExternalStoragePublicDirectory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUIEST_CODE = 105;
    ImageView imageView;
Button button, button2;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView);
        button=findViewById(R.id.button);
        button2=findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                askpermission();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "you clicked on Gallery",Toast.LENGTH_SHORT).show();
            Intent gallery1= new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery1,GALLERY_REQUIEST_CODE);
            }
        });


    }

    private void askpermission() {


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},CAMERA_PERM_CODE);


        }else{
            dispatchTakePictureIntent();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();

            } else {
                Toast.makeText(this, "CAMERA PERMISSION REQUIRED", Toast.LENGTH_SHORT).show();
            }
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
       if(requestCode==CAMERA_REQUEST_CODE){
           if(resultCode== Activity.RESULT_OK){
               File f= new File(currentPhotoPath);
               imageView.setImageURI(Uri.fromFile(f));
               Log.d("tag","Absolute uri of image is " +Uri.fromFile(f));

               Intent gallary= new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
               Uri contentUri= Uri.fromFile(f);
               gallary.setData(contentUri);
               this.sendBroadcast(gallary);

           }
           //use this for just the display
           //Bitmap image= (Bitmap) data.getExtras().get("data");
          // imageView.setImageBitmap(image);
       }
        if(requestCode==GALLERY_REQUIEST_CODE){
            if(resultCode== Activity.RESULT_OK){
                Uri contentUri=data.getData();
                String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName="JPEG_ " + timeStamp+"."+getFileExt(contentUri);
                Log.d("tag","Gallery imager Uri " +imageFileName);
                imageView.setImageURI(contentUri);



            }
        }
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c= getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private File createImagefile()throws IOException{
        //create an image file name..
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName="JPEG" + timeStamp+"_";
      File storageDir=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image=File.createTempFile(
                imageFileName,/*prefix */
                "jpg",/*suffix*/
                storageDir/*directory*/
        );
        //save a file : path for use with Action_view intents
        currentPhotoPath=image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent(){
       Intent takePictureIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       //ensure that there's a camera activity to handle the intent
        if(takePictureIntent.resolveActivity(getPackageManager()) !=null){
            //create a file where the photo should go
            File photoFile=null;
            try{
                photoFile=createImagefile();

            }
            catch(IOException ex){
                //error occured while creating a file

            }
            //continue only if the file was successful created
            if(photoFile !=null){

                Uri photoURI= FileProvider.getUriForFile(this,"com.example.android.fileprovider" , photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(takePictureIntent,CAMERA_REQUEST_CODE);

            }
        }

    }
}