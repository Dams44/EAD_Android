package org.miage.placesearcher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by alexmorel on 04/01/2018.
 */

public class PlaceDetailActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO = 42;

    @BindView(R.id.activity_detail_place_street)
    TextView mPlaceStreet;

    @BindView(R.id.activity_detail_place_pic)
    ImageView mPlacePic;

    //QR code
    @BindView(R.id.imageview_QRcode)
    ImageView mQRcode;

    private String mPlaceStreetValue;
    //QR code avec coordonnées GPS
    private double mPlaceLatitute;
    private double mPlaceLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);
        mPlaceStreetValue = getIntent().getStringExtra("placeStreet");
        mPlaceStreet.setText(mPlaceStreetValue);

        mPlaceLatitute = getIntent().getDoubleExtra("Latitude",0.0);
        mPlaceLongitude = getIntent().getDoubleExtra("Longitude",0.0);

    }

    @OnClick(R.id.activity_detail_place_street)
    public void clickedOnPlaceStreet() {
        finish();
    }

    @OnClick(R.id.activity_detail_button_search)
    public void clickedOnGoogleSearch() {
        // Open browser using an Intent
        Uri url = Uri.parse("https://www.google.fr/search?q=" + mPlaceStreetValue);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, url);
        startActivity(launchBrowser);
    }

    @OnClick(R.id.activity_detail_button_share)
    public void clickedOnShare() {
        // Open share picker using an Intent
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai découvert " + mPlaceStreetValue + " grâce à Place Searcher !");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @OnClick(R.id.activity_detail_button_galery)
    public void clickedOnPickFromGalery() {
        // Open galery picker using an Intent
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    //Création du QR code de la place, le QR code contient les coordonées GPS
    @OnClick(R.id.activity_detail_QR_code)
    public void onClickOnGenerateQRcode() {
        if (!TextUtils.isEmpty(mPlaceStreetValue)) {
            mQRcode.setImageBitmap(generate_QRCode_Bitmap("geo:"+mPlaceLatitute+','+mPlaceLongitude, 600, 600));
        }
    }

    //Transforme du texte en un QR code
    private Bitmap generate_QRCode_Bitmap(String content,int width,int height){
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        //pixels[i * width + j] = 0x000000;
                        pixels[i * width + j] = 0x0080FF;
                    } else {
                        pixels[i * width + j] = 0xffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
            //According to the official document, string: number of colors in the array between rows (must be > = width or < = - width)
            //Namely Bitmap.createBitmap The third parameter in the method needs to be greater than width.
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        // If we get a result from the SELECT_PHOTO query
        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    // Get the selected image as bitmap
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);

                        // Set the bitmap to the picture
                        mPlacePic.setImageBitmap(selectedImageBitmap);
                    } catch (FileNotFoundException e) {
                        // Silent catch : image will not be displayed
                    }

                }
        }
    }
}
