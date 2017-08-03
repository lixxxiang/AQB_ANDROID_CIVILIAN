package com.cgwx.yyfwptz.lixiang.aqb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


public class MineActivity extends AppCompatActivity {


    Toolbar toolbar;
    String userTel;
    TextView tel;
    String string;
    Button logout;
    public  static MineActivity ma;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mine);
        ma =this;
        tel = (TextView) findViewById(R.id.tel);
        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("User",MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.commit();
                Intent intent = new Intent(MineActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            userTel = intent.getStringExtra("userTel");
            Log.e("userTel", userTel);
            String pre = userTel.substring(0, 3);
            String post = userTel.substring(7, 11);
            string = pre + "****" + post;
            Log.e("ore",pre+post);

        }
        tel.setText(string);
        initToolbar(R.id.toolbar, R.id.toolbar_title, "安全宝");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MineActivity.this.finish();
//                Intent intent = new Intent(MineActivity.this, LoginActivity.class);
//                startActivity(intent);
            }
        });
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        toolbar = (Toolbar) findViewById(id);
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
        textView.setTextSize(17);
        textView.setTextColor(R.color.color5F);
        toolbar.setBackgroundColor(Color.WHITE);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }


}
