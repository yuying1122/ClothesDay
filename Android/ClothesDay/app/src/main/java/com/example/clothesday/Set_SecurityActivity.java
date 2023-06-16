package com.example.clothesday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Set_SecurityActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView set_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_security);
        set_pass = findViewById(R.id.security_pwd);

        set_pass.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.security_pwd: // 비밀번호 변경 화면으로 이동
                Intent intent = new Intent(Set_SecurityActivity.this, Set_PasswordActivity.class);
                startActivity(intent);
                break;
        }
    }
}