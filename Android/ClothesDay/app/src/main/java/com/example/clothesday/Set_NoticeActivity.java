package com.example.clothesday;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class Set_NoticeActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private Switch weather_switch, follow_switch;

    //프리퍼런스
    private SharedPreferences pref;
    private SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_notice);
        weather_switch = findViewById(R.id.set_notice_weather_switch);
        follow_switch = findViewById(R.id.set_notice_follow_switch);

        pref = getSharedPreferences("MEMBER",MODE_PRIVATE);
        edit = pref.edit();

        weather_switch.setChecked(pref.getBoolean("WEATHER_NOTIFICATION", false));
        follow_switch.setChecked(pref.getBoolean("FOLLOW_NOTIFICATION", false));

        weather_switch.setOnCheckedChangeListener(this);
        follow_switch.setOnCheckedChangeListener(this);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        pref = getSharedPreferences("MEMBER",MODE_PRIVATE);
        edit = pref.edit();

        switch (buttonView.getId()) {
            case R.id.set_notice_weather_switch: // 날씨 알림 수신 스위치
                if(isChecked) {
                        edit.putBoolean("WEATHER_NOTIFICATION", true);
                }
                else    {
                    edit.putBoolean("WEATHER_NOTIFICATION", false);
                }
                edit.commit();
                break;
            case R.id.set_notice_follow_switch: // 팔로우 알림 수신 스위치
                if(isChecked) {
                    edit.putBoolean("FOLLOW_NOTIFICATION", true);
                }
                else{
                    edit.putBoolean("FOLLOW_NOTIFICATION", false);
                }
                edit.commit();
                break;
        }
    }
}