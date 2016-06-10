package com.mariusiliescu.carcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Marius on 04.06.2016.
 */
public class CarControllWithButtons extends Activity {
    final static  int NR_ORI=10;

    int o=0,x=0,y=0,z=0;
    private String address = null;
    private BluetoothAdapter myBT=null;
    private BluetoothSocket socket = null;
    private boolean connected = false;
    private ProgressDialog progress;
    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    boolean isGoingForward = false, isGoingBackward = false, isGoingLeft = false, isGoingRight = false;

    private MediaPlayer horn;
    private MediaPlayer tickTock;

    boolean semnalizeazaStanga = false;
    boolean semnalizeazaDreapta = false;
    boolean avariiOn = false;

    private AnimationDrawable btnAvarieAnimatie;
    private AnimationDrawable btnSemnStangaAnimatie;
    private AnimationDrawable btnSemnDreaptaAnimatie;
    private Animation btnSetttingsAnimation;

    private ToggleButton btnAvarii;
    private ToggleButton btnSemnStanga;
    private ToggleButton btnSemnDreapta;
    private ImageButton showMenu;

    private ImageButton left ;
    private ImageButton right;
    private ImageButton up ;
    private ImageButton down ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_controll_with_buttons);

        enterImmersiveMode(true);

        Intent i = getIntent();
        address = i.getStringExtra(BluetoothListFragment.EXTRA_ADDRESS);

        new ConnectBT().execute();


        horn = MediaPlayer.create(this, R.raw.horn);
        tickTock = MediaPlayer.create(this, R.raw.semnalizare);
        tickTock.setLooping(true);

        left = (ImageButton) findViewById(R.id.btnMoveLeft);
        right = (ImageButton) findViewById(R.id.btnMoveRight);
        up = (ImageButton) findViewById(R.id.btnMoveForward);
        down = (ImageButton) findViewById(R.id.btnMoveBackward);


        btnAvarii = (ToggleButton) findViewById(R.id.btnAvarii);
        btnSemnStanga = (ToggleButton) findViewById(R.id.btnSemnStanga);
        btnSemnDreapta = (ToggleButton) findViewById(R.id.btnSemnDreapta);

        btnAvarii.setBackgroundResource(R.drawable.ic_toggle_animation);
        btnAvarieAnimatie = (AnimationDrawable) btnAvarii.getBackground();
        btnSemnStanga.setBackgroundResource(R.drawable.semnalizare_animatie_stanga);
        btnSemnStangaAnimatie = (AnimationDrawable) btnSemnStanga.getBackground();
        btnSemnDreapta.setBackgroundResource(R.drawable.semnalizare_animatie_dreapta);
        btnSemnDreaptaAnimatie = (AnimationDrawable) btnSemnDreapta.getBackground();
        btnSetttingsAnimation = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.refresh_bt_anim);

        showMenu= (ImageButton) findViewById(R.id.btnSettings);

        btnAvarii.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controlLedAvarii(isChecked);
            }
        });

        btnSemnStanga.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controlLedSemnalizareStanga(isChecked);
            }
        });

        btnSemnDreapta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controlLedSemnalizareDreapta(isChecked);
            }
        });

        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(o==0) {
                        up.setImageResource(R.drawable.turn_up_pressed);
                        Log.d("movement", "up pressed");
                        isGoingForward = true;
                        if (isGoingBackward) {
                            isGoingBackward = false;
                            btControllOprireDinMers();
                        }
                        if (isGoingForward) {
                            btControllMergeInainte();
                        }
                    }
                    o++;
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    o=0;
                    up.setImageResource(R.drawable.turn_up_unpressed);
                    isGoingForward=false;
                    btControllOprireDinMers();
                }
                return true;
            }
        });

        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(x==0) {
                        down.setImageResource(R.drawable.turn_down_pressed);
                        Log.d("movement", "down pressed");
                        isGoingBackward = true;
                        if (isGoingForward) {
                            isGoingForward = false;
                            btControllOprireDinMers();
                        }
                        if (isGoingBackward) {
                            btControllMergeInapoi();
                        }
                    }
                    x++;
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    x=0;
                    down.setImageResource(R.drawable.turn_down_unpressed);
                    isGoingBackward=false;
                    btControllOprireDinMers();
                }
                return true;
            }
        });

        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(y==0) {
                        left.setImageResource(R.drawable.turn_left_pressed);
                        Log.d("movement", "left pressed");
                        isGoingLeft = true;
                        if (isGoingRight) {
                            isGoingRight = false;
                            btControllCentrareDirectie();
                        }
                        if (isGoingLeft) {
                            btControllDirectieStanga();
                        }
                    }
                    y++;
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    y=0;
                    left.setImageResource(R.drawable.turn_left_unpressed);
                    isGoingLeft=false;
                    btControllCentrareDirectie();
                }
                return true;
            }
        });

        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(z==0) {
                        right.setImageResource(R.drawable.turn_right_pressed);
                        Log.d("movement", "right pressed");
                        isGoingRight = true;
                        if (isGoingLeft) {
                            isGoingLeft = false;
                            btControllCentrareDirectie();
                        }
                        if (isGoingRight) {
                            btControllDirectieDreapta();
                        }
                    }
                    z++;
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    z=0;
                    right.setImageResource(R.drawable.turn_right_unpressed);
                    isGoingRight=false;
                    btControllCentrareDirectie();
                }
                return true;
            }
        });

        showMenu = (ImageButton) findViewById(R.id.btnSettings);
        showMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showMenu.startAnimation(btnSetttingsAnimation);
                PopupMenu dropDownMenu = new PopupMenu(getApplicationContext(), showMenu);
                dropDownMenu.getMenuInflater().inflate(R.menu.settings_menu, dropDownMenu.getMenu());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Window w = getWindow(); // in Activity's onCreate() for instance
                    w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }

                dropDownMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        if(menuItem.getItemId() == R.id.dropdown_menu_back){
                            if(tickTock.isPlaying()) {
                                tickTock.stop();
                                tickTock = MediaPlayer.create(CarControllWithButtons.this, R.raw.semnalizare);
                                tickTock.setLooping(true);
                            }
                            Disconnect();
                            finish();
                        } if(menuItem.getItemId() == R.id.dropdown_menu_exit){

                            AlertDialog.Builder builder = new AlertDialog.Builder(CarControllWithButtons.this);
                            builder.setMessage("Are you sure you want to exit?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Disconnect();
                                            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                                            homeIntent.addCategory( Intent.CATEGORY_HOME );
                                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(homeIntent);
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            System.exit(0);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();

                        }else if(menuItem.getItemId() == R.id.dropdown_menu_g_control){
                            Disconnect();
                            Intent intent = new Intent(CarControllWithButtons.this, CarControlWithGiroscope.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(BluetoothListFragment.EXTRA_ADDRESS,address);

                            if(tickTock.isPlaying()) {
                                tickTock.stop();
                                tickTock = MediaPlayer.create(CarControllWithButtons.this, R.raw.semnalizare);
                                tickTock.setLooping(true);
                            }
                            startActivity(intent);
                        }
                        return true;
                    }
                });
                dropDownMenu.show();
            }
        });
    }

    private void controlLedSemnalizareStanga(Boolean esteButonulApasat){
        if(esteButonulApasat) {
            if(!tickTock.isPlaying()) {
                tickTock.start();
            }
            semnalizeazaStanga = true;
            btnSemnStangaAnimatie.start();
            btControllLedSemnalizareStangaON();
            if(semnalizeazaDreapta){
                btControllLedSemnalizareStangaOFF();
                btControllLedSemnalizareDreaptaON();
                semnalizeazaDreapta = false;
                btnSemnDreaptaAnimatie.stop();
                btnSemnDreapta.setBackgroundDrawable(null);
                btnSemnDreapta.setBackgroundResource(R.drawable.semnalizare_animatie_dreapta);
                btnSemnDreaptaAnimatie = (AnimationDrawable) btnSemnDreapta.getBackground();
            }
        }else {
            if(tickTock.isPlaying()) {
                tickTock.stop();
                tickTock = MediaPlayer.create(this, R.raw.semnalizare);
                tickTock.setLooping(true);
            }
            btControllLedSemnalizareStangaOFF();
            semnalizeazaStanga = false;
            btnSemnStangaAnimatie.stop();
            btnSemnStanga.setBackgroundDrawable(null);
            btnSemnStanga.setBackgroundResource(R.drawable.semnalizare_animatie_stanga);
            btnSemnStangaAnimatie = (AnimationDrawable) btnSemnStanga.getBackground();
        }
    }

    private void controlLedSemnalizareDreapta(Boolean esteButonulApasat) {
        if(esteButonulApasat) {
            if(!tickTock.isPlaying())
                tickTock.start();
            semnalizeazaDreapta = true;
            btnSemnDreaptaAnimatie.start();
            btControllLedSemnalizareDreaptaON();
            if(semnalizeazaStanga){
                btControllLedSemnalizareDreaptaOFF();
                btControllLedSemnalizareStangaON();
                semnalizeazaStanga = false;
                btnSemnStangaAnimatie.stop();
                btnSemnStanga.setBackgroundDrawable(null);
                btnSemnStanga.setBackgroundResource(R.drawable.semnalizare_animatie_stanga);
                btnSemnStangaAnimatie = (AnimationDrawable) btnSemnStanga.getBackground();
            }
        }else {
            if(tickTock.isPlaying()) {
                tickTock.stop();
                tickTock = MediaPlayer.create(this, R.raw.semnalizare);
                tickTock.setLooping(true);
            }
            btControllLedSemnalizareDreaptaOFF();
            semnalizeazaDreapta = false;
            btnSemnDreaptaAnimatie.stop();
            btnSemnDreapta.setBackgroundDrawable(null);
            btnSemnDreapta.setBackgroundResource(R.drawable.semnalizare_animatie_dreapta);
            btnSemnDreaptaAnimatie = (AnimationDrawable) btnSemnDreapta.getBackground();
        }
    }

    private void controlLedAvarii(Boolean esteButonulApasat){
        if(esteButonulApasat) {
            if(avariiOn){
                ledAvariiOff();
            }else {
                ledAvariiOn();
            }
        }else {
            ledAvariiOff();
        }
    }
    private void ledAvariiOn(){
        avariiOn = true;

        if(!tickTock.isPlaying())
            tickTock.start();

        semnalizeazaDreapta = true;
        semnalizeazaStanga = true;



        btnSemnStangaAnimatie.stop();
        btnSemnStanga.setBackgroundDrawable(null);
        btnSemnStanga.setBackgroundResource(R.drawable.semnalizare_animatie_stanga);
        btnSemnStangaAnimatie = (AnimationDrawable) btnSemnStanga.getBackground();

        btnSemnDreaptaAnimatie.stop();
        btnSemnDreapta.setBackgroundDrawable(null);
        btnSemnDreapta.setBackgroundResource(R.drawable.semnalizare_animatie_dreapta);
        btnSemnDreaptaAnimatie = (AnimationDrawable) btnSemnDreapta.getBackground();

        btnAvarieAnimatie.start();
        btnSemnDreaptaAnimatie.start();
        btnSemnStangaAnimatie.start();


        btControllLedAvariiON();
        btControllLedSemnalizareDreaptaON();
        btControllLedSemnalizareStangaON();

        btnSemnStanga.setClickable(false);
        btnSemnDreapta.setClickable(false);
    }
    private void ledAvariiOff(){

        avariiOn=false;


        if(tickTock.isPlaying()) {
            tickTock.stop();
            tickTock = MediaPlayer.create(this, R.raw.semnalizare);
            tickTock.setLooping(true);
        }

        semnalizeazaDreapta = false;
        semnalizeazaStanga = false;

        btnAvarieAnimatie.stop();
        btnAvarii.setBackgroundDrawable(null);
        btnAvarii.setBackgroundResource(R.drawable.ic_toggle_animation);
        btnAvarieAnimatie = (AnimationDrawable) btnAvarii.getBackground();


        btnSemnDreaptaAnimatie.stop();
        btnSemnDreapta.setBackgroundDrawable(null);
        btnSemnDreapta.setBackgroundResource(R.drawable.semnalizare_animatie_dreapta);
        btnSemnDreaptaAnimatie = (AnimationDrawable) btnSemnDreapta.getBackground();

        btnSemnStangaAnimatie.stop();
        btnSemnStanga.setBackgroundDrawable(null);
        btnSemnStanga.setBackgroundResource(R.drawable.semnalizare_animatie_stanga);
        btnSemnStangaAnimatie = (AnimationDrawable) btnSemnStanga.getBackground();

        btControllLedAvariiOFF();
        btControllLedSemnalizareDreaptaOFF();
        btControllLedSemnalizareStangaOFF();

        btnSemnStanga.setClickable(true);
        btnSemnDreapta.setClickable(true);
    }

    private void enterImmersiveMode(boolean hasFocus){
        View decorView = getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        enterImmersiveMode(hasFocus);
    }

    private class ConnectBT extends AsyncTask
    {
        boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!MainActivity.isInDemoMod) {
                progress = ProgressDialog.show(CarControllWithButtons.this, "Se conecteaza...", "Te rugam asteapta");
            }
        }

        @Override
        protected Object doInBackground(Object[] params) {

            try {

                if (socket != null || !connected) {
                    myBT = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = myBT.getRemoteDevice(address);
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    socket.connect();
                }
            }
            catch (IOException e)
            {
                connectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if(!connectSuccess)
            {
                if (!MainActivity.isInDemoMod) {
                    ShowText("Conexiune esuata. Incearca din nou.");
                    finish();
                }
            }
            else
            {
                ShowText("Conectat");
                connected = true;
            }
            if (!MainActivity.isInDemoMod) {
                progress.dismiss();
            }
        }
    }
    public void ShowText(String text) {

        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    //bt controll
    //LEDURI
    private void btControllLedSemnalizareStangaON () {
        if (!MainActivity.isInDemoMod){
            if (socket != null)
                try {
                    for (int i = 0; i < 10; i++)
                        socket.getOutputStream().write("SN".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllLedSemnalizareStangaOFF () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    for (int i = 0; i < NR_ORI; i++)
                        socket.getOutputStream().write("SF".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllLedSemnalizareDreaptaON () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    for (int i = 0; i < NR_ORI; i++)
                        socket.getOutputStream().write("DN".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllLedSemnalizareDreaptaOFF () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    for (int i = 0; i < NR_ORI; i++)
                        socket.getOutputStream().write("DF".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllLedAvariiON () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    for (int i = 0; i < NR_ORI; i++)
                        socket.getOutputStream().write("AN".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllLedAvariiOFF () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    for (int i = 0; i < NR_ORI; i++)
                        socket.getOutputStream().write("AF".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }

    //SERVOMOTOR
    private void btControllMergeInainte () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    socket.getOutputStream().write("MU".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllMergeInapoi () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    socket.getOutputStream().write("MD".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllDirectieStanga () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    socket.getOutputStream().write("ML".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllDirectieDreapta () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    socket.getOutputStream().write("MR".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllCentrareDirectie () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    socket.getOutputStream().write("CD".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void btControllOprireDinMers () {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    socket.getOutputStream().write("OM".getBytes());
                } catch (IOException e) {
                    ShowText("Eroare");
                }
        }
    }
    private void Disconnect() {
        if (!MainActivity.isInDemoMod) {
            if (socket != null)
                try {
                    socket.close();
                } catch (Exception e) {
                    ShowText("Eroare");
                }
        }
    }
}
