package com.mariusiliescu.carcontrol;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Marius on 06.06.2016.
 */
public class CreditsFragment extends Fragment {

    ImageView img;
    ImageView img2;
    ImageView img3;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.credits_layout, container, false);

        img = (ImageView)rootView.findViewById(R.id.android_link);
        img.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://developer.android.com"));
                startActivity(intent);
            }
        });

        img2 = (ImageView)rootView.findViewById(R.id.photoshoplink);
        img2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("http://www.adobe.com/ro/products/photoshop.html"));
                startActivity(intent);
            }
        });

        img3 = (ImageView)rootView.findViewById(R.id.arduinolink);
        img3.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.arduino.cc/"));
                startActivity(intent);
            }
        });

        return  rootView;
    }
}
