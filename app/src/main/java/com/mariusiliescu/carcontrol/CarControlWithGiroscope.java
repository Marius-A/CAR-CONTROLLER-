package com.mariusiliescu.carcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Marius on 07.06.2016.
 */
public class CarControlWithGiroscope extends Activity {


    //recive bt signal
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    //---

    final static  int NR_ORI=5;

    boolean conStart = false;

    private String address = null;
    private BluetoothAdapter myBT = null;
    private BluetoothSocket socket = null;
    private boolean connected = false;
    private ProgressDialog progress;
    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private SensorManager mSensorManager;

    ImageView up;
    ImageView down;
    ImageView left;
    ImageView right;


    MediaPlayer horn;
    MediaPlayer tickTock;

    ImageButton showMenu;

    boolean semnalizeazaStanga = false;
    boolean semnalizeazaDreapta = false;
    boolean avariiOn = false;

    AnimationDrawable btnAvarieAnimatie;
    AnimationDrawable btnSemnStangaAnimatie;
    AnimationDrawable btnSemnDreaptaAnimatie;

    Animation btnSetttingsAnimation;

    ToggleButton btnAvarii;
    ToggleButton btnSemnStanga;
    ToggleButton btnSemnDreapta;

    boolean isGoingForward = false, isGoingBackward = false, isGoingLeft = false, isGoingRight = false, isNotGoingAnywhere = false;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_control_with_giroscope);

        Intent i = getIntent();
        address = i.getStringExtra(BluetoothListFragment.EXTRA_ADDRESS);
        Log.d("address:", address);


        new ConnectBT().execute();


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),400000);


        horn = MediaPlayer.create(this, R.raw.horn);
        tickTock = MediaPlayer.create(this, R.raw.semnalizare);
        tickTock.setLooping(true);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        up = (ImageView) findViewById(R.id.up);
        down = (ImageView) findViewById(R.id.down);
        left = (ImageView) findViewById(R.id.left);
        right = (ImageView) findViewById(R.id.right);

        enterImmersiveMode(true);

        btnSetttingsAnimation = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.refresh_bt_anim);

        btnAvarii = (ToggleButton) findViewById(R.id.btnAvarii);
        btnSemnStanga = (ToggleButton) findViewById(R.id.btnSemnStanga);
        btnSemnDreapta = (ToggleButton) findViewById(R.id.btnSemnDreapta);


        btnAvarii.setBackgroundResource(R.drawable.ic_toggle_animation);
        btnAvarieAnimatie = (AnimationDrawable) btnAvarii.getBackground();

        btnSemnStanga.setBackgroundResource(R.drawable.semnalizare_animatie_stanga);
        btnSemnStangaAnimatie = (AnimationDrawable) btnSemnStanga.getBackground();

        btnSemnDreapta.setBackgroundResource(R.drawable.semnalizare_animatie_dreapta);
        btnSemnDreaptaAnimatie = (AnimationDrawable) btnSemnDreapta.getBackground();


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


        btnAvarii.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controlLedAvarii(isChecked);
                if (isChecked) {
                    isNotGoingAnywhere = true;
                    up.setImageResource(R.drawable.up);
                    left.setImageResource(R.drawable.left);
                    right.setImageResource(R.drawable.right);
                    down.setImageResource(R.drawable.down);
                    isGoingBackward = false;
                    isGoingLeft = false;
                    isGoingRight = false;
                    isGoingForward = false;
                    Toast.makeText(getApplicationContext(), "State :" + isNotGoingAnywhere, Toast.LENGTH_SHORT).show();
                } else {
                    isNotGoingAnywhere = false;
                    Toast.makeText(getApplicationContext(), "State :" + isNotGoingAnywhere, Toast.LENGTH_SHORT).show();
                }
            }
        });

        showMenu = (ImageButton) findViewById(R.id.btnSettings2);
        showMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                showMenu.startAnimation(btnSetttingsAnimation);
                PopupMenu dropDownMenu = new PopupMenu(getApplicationContext(), showMenu);
                dropDownMenu.getMenuInflater().inflate(R.menu.settings_menu2, dropDownMenu.getMenu());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Window w = getWindow(); // in Activity's onCreate() for instance
                    w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }


                dropDownMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        if (menuItem.getItemId() == R.id.dropdown_menu_back2) {
                            if(tickTock.isPlaying()) {
                                tickTock.stop();
                                tickTock = MediaPlayer.create(CarControlWithGiroscope.this, R.raw.semnalizare);
                                tickTock.setLooping(true);
                            }
                            Disconnect();
                            finish();
                        }
                        if (menuItem.getItemId() == R.id.dropdown_menu_exit2) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(CarControlWithGiroscope.this);
                            builder.setMessage("Are you sure you want to exit?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                                            homeIntent.addCategory(Intent.CATEGORY_HOME);
                                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(homeIntent);
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            System.exit(0);
                                            Disconnect();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();

                        } else if (menuItem.getItemId() == R.id.dropdown_menu_b_control) {
                            if(tickTock.isPlaying()) {
                                tickTock.stop();
                                tickTock = MediaPlayer.create(CarControlWithGiroscope.this, R.raw.semnalizare);
                                tickTock.setLooping(true);
                            }
                            Disconnect();
                            Intent intent = new Intent(CarControlWithGiroscope.this, CarControllWithButtons.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(BluetoothListFragment.EXTRA_ADDRESS,address);
                            startActivity(intent);
                        }
                        return true;
                    }
                });
                dropDownMenu.show();
            }
        });

    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            if (!isNotGoingAnywhere) {

                checkForward(x, y, z);
                checkBackward(x, y, z);
                checkLeft(x, y, z);
                checkRight(x, y, z);

                checkStopForward(x, y, z);
                checkStopBackward(x, y, z);
                checkCenterWeel(x, y, z);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    private void checkStopForward(float x, float y, float z) {
        if (x > 4) {
            isGoingForward = false;
            up.setImageResource(R.drawable.up);
            if(conStart)
                btControllOprireDinMers();
        }
    }

    private void checkStopBackward(float x, float y, float z) {
        if (x > 6.0 && z > 4.0) {
            isGoingBackward = false;
            down.setImageResource(R.drawable.down);
            if(conStart)
                btControllOprireDinMers();
        }
    }

    private void checkForward(float x, float y, float z) {
        if ((!isNotGoingAnywhere && !isGoingBackward) && (x < 1.0 && z > 9.0)) {
            isGoingForward = true;
            up.setImageResource(R.drawable.pressed_up);
            btControllMergeInainte();
        }
    }

    private void checkLeft(float x, float y, float z) {
        if ((!isNotGoingAnywhere && !isGoingRight) && (y < -3.0)) {
            isGoingLeft = true;
            left.setImageResource(R.drawable.pressed_left);
            if(conStart)
                btControllDirectieStanga();
        }
    }

    private void checkRight(float x, float y, float z) {
        if ((!isNotGoingAnywhere && !isGoingLeft) && (y > 3.0)) {
            isGoingRight = true;
            right.setImageResource(R.drawable.pressed_right);
            if(conStart)
                btControllDirectieDreapta();
        }
    }

    private void checkBackward(float x, float y, float z) {
        if ((!isNotGoingAnywhere && !isGoingForward) && (x < 9.0 && z < 4.0)) {
            isGoingBackward = true;
            down.setImageResource(R.drawable.pressed_down);
            if(conStart)
                btControllMergeInapoi();
        }
    }

    private void checkCenterWeel(float x, float y, float z) {
        if (y > -3.0 && y < 3.0) {
            isGoingRight = false;
            isGoingLeft = false;
            left.setImageResource(R.drawable.left);
            right.setImageResource(R.drawable.right);
            if(conStart)
                btControllCentrareDirectie();
        }
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

    private class ConnectBT extends AsyncTask
    {
        boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!MainActivity.isInDemoMod) {
                progress = ProgressDialog.show(CarControlWithGiroscope.this, "Se conecteaza...", "Te rugam asteapta");
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


    void beginListenForData() throws IOException {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        readBufferPosition = 0;
        readBuffer = new byte[1024];
        mmOutputStream = socket.getOutputStream();
        mmInputStream = socket.getInputStream();
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if(data.equals("G")){
                                                ledAvariiOn();
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        thread.start();
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
    }
}

