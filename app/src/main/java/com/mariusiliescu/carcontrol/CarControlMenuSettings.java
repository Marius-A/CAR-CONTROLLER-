package com.mariusiliescu.carcontrol;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.RadioButton;

/**
 * Created by Marius on 06.06.2016.
 */
public class CarControlMenuSettings extends Fragment{

    static  boolean is_btn_control_by_btns_checked = false;

    private RadioButton btn_control_by_acc;
    private RadioButton btn_control_by_btns;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.controll_settings, container, false);

        final Animation refreshAnim = AnimationUtils.loadAnimation(getActivity() , R.anim.refresh_bt_anim);

        btn_control_by_acc = (RadioButton) rootView.findViewById(R.id.radio_acc);
        btn_control_by_btns = (RadioButton) rootView.findViewById(R.id.radio_btn);

        btn_control_by_acc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    btn_control_by_acc.startAnimation(refreshAnim);
                    btn_control_by_acc.setButtonDrawable(R.drawable.acc_control_checked);
                    is_btn_control_by_btns_checked = false;
                }else{
                    btn_control_by_acc.setButtonDrawable(R.drawable.acc_control_unchecked);
                    is_btn_control_by_btns_checked = true;
                }
            }
        });
        btn_control_by_btns.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    btn_control_by_btns.startAnimation(refreshAnim);
                    btn_control_by_btns.setButtonDrawable(R.drawable.btn_controll_checked);
                    is_btn_control_by_btns_checked = true;
                }else{
                    btn_control_by_btns.setButtonDrawable(R.drawable.btn_controll_unchecked);
                    is_btn_control_by_btns_checked = false;
                }
            }
        });


        return  rootView;
    }
}
