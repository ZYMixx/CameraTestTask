package com.zymixx.cameratesttask;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.zymixx.cameratesttask.Event.*;
import static android.content.ContentValues.TAG;



import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

public  class EnterInAccount extends AppCompatActivity  {
    FrameLayout frameLayout;
    EditText userEmail;
    EditText userPassword;
    Button enterButton;
    ImageView siteButton;
    static ArrayList<CameraInfo> arrayOfCamInfo = new ArrayList<>();
    String status;
    Boolean connect;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.account_login);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        frameLayout = findViewById(R.id.frame);


      //  Intent intent = new Intent(EnterInAccount.this, MainActivity.class);
       // startActivity(intent);

        EventBus.getDefault().register(this);
        userEmail = findViewById(R.id.etUserEmail);
        userPassword = findViewById(R.id.etUserPassword);
        enterButton = findViewById(R.id.enterButton);
        siteButton = findViewById(R.id.site_button);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new LoginUser());
            }
        });

    }

    @Subscribe (threadMode = ThreadMode.BACKGROUND)
    public void  startLoginUser (LoginUser loginUser) {
            String userName = String.valueOf(userEmail.getText());
            String password = String.valueOf(userPassword.getText());
            String respons;
            try {
                if (userName.equals("1")) {userName = "test_task";}
                if (password.equals("1")) {password = "test_task";}
                URL url = new URL("https://ms1.dev.camdrive.org/mobile/api_native/login/?username="+ userName + "&password="+ password);
                respons = WorkAPI.getResponsFromURL(url);
                status = WorkAPI.parsing(respons);
                System.out.println(respons);
            } catch (IOException e) {
                Log.e(TAG, "startLoginUser: ", e);
            }
            if (status.equals(String.valueOf(1))){
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                connect = true;
            } else {connect = false;}
        }
    }





