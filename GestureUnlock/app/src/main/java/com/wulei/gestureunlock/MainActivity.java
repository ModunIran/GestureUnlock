package com.wulei.gestureunlock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private UnlockView mUnlockView;
    private String pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnlockView= (UnlockView) findViewById(R.id.unlock);
        mUnlockView.setMode(UnlockView.CREATE_MODE);
        mUnlockView.setGestureListener(new UnlockView.CreateGestureListener() {
            @Override
            public void onGestureCreated(String result) {
                pwd=result;
                Toast.makeText(MainActivity.this,"Set Gesture Succeeded!",Toast.LENGTH_SHORT).show();
                mUnlockView.setMode(UnlockView.CHECK_MODE);
            }
        });
        mUnlockView.setOnUnlockListener(new UnlockView.OnUnlockListener() {
            @Override
            public boolean isUnlockSuccess(String result) {
                if(result.equals(pwd)){
                    return true;
                }else{
                    return false;
                }
            }

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this,"Check Succeeded!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure() {
                Toast.makeText(MainActivity.this,"Check Failed!",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
