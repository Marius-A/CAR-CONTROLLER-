package com.mariusiliescu.carcontrol;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Marius on 06.06.2016.
 */
public class BluetoothListFragment extends Fragment{
    private ListView deviceList;
    private FloatingActionButton fab ;

    public static final String EXTRA_ADDRESS="address";
    private BluetoothAdapter btAdapter = null;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.device_list_layout, container, false);

        final Animation refreshAnim = AnimationUtils.loadAnimation(getActivity() , R.anim.refresh_bt_anim);

        fab = (FloatingActionButton) this.getActivity().findViewById(R.id.fab);

        deviceList = (ListView) rootView.findViewById(R.id.listView);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        reqBTConnection();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.startAnimation(refreshAnim);
                reqBTConnection();
            }
        });
        return rootView;
    }

    private void reqBTConnection(){
        if(btAdapter == null)
        {
            ShowText("Nu exista BT pe dispozitiv");
        }
        else
        {
            if(btAdapter.isEnabled())
            {
                getPairedDevices();
            }
            else
            {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn,0);
                getPairedDevices();
            }
        }
    }

    public void ShowText(String text) {
        Toast.makeText(getActivity(),text,Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getPairedDevices()
    {
        pairedDevices = btAdapter.getBondedDevices();
        List<ListViewItem> list = new ArrayList();

        if(pairedDevices.size() != 0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                Log.d("bluetooth dev type"," "+bt.getBluetoothClass().toString());
                if(bt.getBluetoothClass().toString().equals(getString(R.string.keyboard_bt_category)))
                    list.add(new ListViewItem(R.drawable.keyboard_bt_icon,bt.getName(),bt.getAddress()));
                else if(bt.getBluetoothClass().toString().equals(getString(R.string.phone_bt_category)))
                    list.add(new ListViewItem(R.drawable.phone_bt_icon,bt.getName(),bt.getAddress()));
                else if(bt.getBluetoothClass().toString().equals(getString(R.string.headset_bt_category)))
                    list.add(new ListViewItem(R.drawable.headset_bt_icon,bt.getName(),bt.getAddress()));
                else
                    list.add(new ListViewItem(R.drawable.arduino_bt_icon,bt.getName(),bt.getAddress()));
            }


        }
        else
        {
            ShowText("Nu exista dispozitive imperecheate");
        }

        CustomListViewAdapter customListViewAdapter = new CustomListViewAdapter(getActivity(),list);
        deviceList.setAdapter(customListViewAdapter);
        deviceList.setOnItemClickListener(onItemClickListener);

    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

            //adresa MAC
            final String address = ((TextView) v.findViewById(R.id.txtadress)).getText().toString();

            ImageView devImage = (ImageView) v.findViewById(R.id.imgThumbnail);
            final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh_bt_anim);
            devImage.startAnimation(anim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Intent i;
                    if (CarControlMenuSettings.is_btn_control_by_btns_checked){
                        i = new Intent(getActivity(), CarControllWithButtons.class);
                    }else{
                        i = new Intent(getActivity(), CarControlWithGiroscope.class);
                    }
                    i.putExtra(EXTRA_ADDRESS,address);
                    startActivity(i);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    };
}
