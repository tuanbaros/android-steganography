package tuannt.simple.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import tuannt.simple.steganography.DecodeRequest;
import tuannt.simple.steganography.EncodeRequest;
import tuannt.simple.steganography.Stegy;
import tuannt.simple.steganography.StegyCallback;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE_ENCODE = 100;
    private static final int SELECT_PICTURE_DECODE = 101;
    private ImageView imageView;
    private TextView textMessage;
    private Uri filePath;
    private Bitmap originImage, newImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_view);
        textMessage = findViewById(R.id.text_message);
        checkAndRequestPermissions();


        findViewById(R.id.button_pick).setVisibility(View.GONE);

        findViewById(R.id.button_encode).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_ENCODE);
        });

        findViewById(R.id.button_decode).setOnClickListener(view -> {
//            Intent intent = new Intent();
//            intent.setType("image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_DECODE);
            showDialog("Decoding");
            DecodeRequest decodeRequest = new DecodeRequest(newImage, "tuan");
            Stegy.decode(MainActivity.this, decodeRequest, new StegyCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    textMessage.setText(data);
                    textMessage.post(() -> {
                        hideDialog();
                    });
                }

                @Override
                public void onError(String error) {
                    textMessage.setText(error);
                    textMessage.post(() -> hideDialog());
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Image set to imageView
        if (requestCode == SELECT_PICTURE_ENCODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();
            try {
                originImage = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                imageView.setImageBitmap(originImage);

                showDialog("Encoding");
                EncodeRequest encodeRequest = new EncodeRequest(originImage, "street", "tuan");
                Stegy.encode(MainActivity.this, encodeRequest, new StegyCallback<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap data) {
//                        saveToInternalStorage(data);
                        textMessage.setText("success");
                        newImage = data;
                        imageView.setImageBitmap(data);
                        textMessage.post(() -> hideDialog());
                    }

                    @Override
                    public void onError(String error) {
                        Log.i(">>>>", error);
                        textMessage.setText(error);
                        textMessage.post(() -> hideDialog());
                        Toast.makeText(getBaseContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
//                Log.d(TAG, "Error : " + e);
            }
        }

        if (requestCode == SELECT_PICTURE_DECODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();
            try {
                originImage = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                imageView.setImageBitmap(originImage);

                showDialog("Decoding");
                DecodeRequest decodeRequest = new DecodeRequest(originImage, "tuan");
                Stegy.decodeStringFromImage(MainActivity.this, decodeRequest, new StegyCallback<String>() {
                    @Override
                    public void onSuccess(String data) {
                        textMessage.setText(data);
                        textMessage.post(() -> {
                            hideDialog();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        textMessage.setText(error);
                        textMessage.post(() -> hideDialog());
                    }
                });
            } catch (IOException e) {
//                Log.d(TAG, "Error : " + e);
            }
        }

    }

    private void checkAndRequestPermissions() {
        int permissionWriteStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int ReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (ReadPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 1);
        }
    }

    private void saveToInternalStorage(Bitmap bitmap) {
        OutputStream fOut;
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "Encoded" + ".PNG"); // the File to save ,
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ProgressDialog progressDialog;
    private void showDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message + ", Please Wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}