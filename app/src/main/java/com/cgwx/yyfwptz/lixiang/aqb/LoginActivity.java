package com.cgwx.yyfwptz.lixiang.aqb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cgwx.yyfwptz.lixiang.entity.Constants;
import com.cgwx.yyfwptz.lixiang.entity.sendMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    Button getVcode;
    EditText tel;
    String userTel;
    public static final String POST_URL =  Constants.prefix + "mobile/common/sendMessage";
    private OkHttpClient client;
    Gson gson;
    public static LoginActivity la;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        la = this;
        SharedPreferences sp = getSharedPreferences("User", MODE_PRIVATE);

        if(sp.getString("userTel", null) != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userTel", sp.getString("userTel", null));
            intent.putExtra("userId", sp.getString("userId", null));
            startActivity(intent);
            finish();//关闭当前登录界面，否则在主界面按后退键还会回到登录界面
        }


        getVcode = (Button) findViewById(R.id.getVcode);
        tel = (EditText) findViewById(R.id.tel);
        tel.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

        getVcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userTel = tel.getText().toString();
                gson = new Gson();
                if (isMobileNO(userTel)) {
                    client = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .build();
                    RequestBody requestBodyPost = new FormBody.Builder()
                            .add("telephone", userTel)
                            .build();
                    Request requestPost = new Request.Builder()
                            .url(POST_URL)
                            .post(requestBodyPost)
                            .build();
                    client.newCall(requestPost).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Toast.makeText(LoginActivity.this, "验证码获取失败", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String string = response.body().string();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("return:", string);
                                    sendMessage userInfo = gson.fromJson(string, sendMessage.class);
                                    if (userInfo.getMeta().equals("success")) {
                                        Intent intent = new Intent(LoginActivity.this, VCodeActivity.class);
                                        intent.putExtra("userTel", userTel);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    });


                } else {
                    Toast.makeText(LoginActivity.this, "手机号不正确", Toast.LENGTH_SHORT).show();
                }

            }
        });

        tel.addTextChangedListener(mTextWatcher);


    }

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
//          mTextView.setText(s);//将输入的内容实时显示
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            getVcode.setBackgroundResource(R.drawable.orabtn);
            if (tel.getText().length() == 0) {
                getVcode.setBackgroundResource(R.drawable.gray);

            }

        }
    };


    public static boolean isMobileNO(String mobiles) {
        String telRegex = "[1][34578]\\d{9}";
        if (TextUtils.isEmpty(mobiles)) return false;
        else return mobiles.matches(telRegex);
    }

    public void readAccount() {
        SharedPreferences sp = getSharedPreferences("info", MODE_PRIVATE);
        String userTel = sp.getString("userTel", "");
    }
}
