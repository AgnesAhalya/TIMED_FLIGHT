package com.example.data_usage_limit;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    boolean flag=false;
    final CountDownTimer[] timer = new CountDownTimer[1];
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Button ON = findViewById(R.id.ON);
            Button OFF = findViewById(R.id.OFF);

            if (isOnline()) {
                flag = true;
            }

            ON.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isOnline()) {
                        TextView info = findViewById(R.id.info);
                        info.setText("ONLINE");

                        timer[0] = getCountDownTimer();
                        timer[0].start();

                    }
                }
            });
            OFF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timer[0].cancel();
                    if (isOnline()) {
                        TextView info = findViewById(R.id.info);
                        info.setText("ONLINE ALERT DISABLED");
                    } else {
                        TextView info = findViewById(R.id.info);
                        info.setText("Data already disabled");
                    }
                }

            });
        }
        catch(Exception e)
        {
            TextView info = findViewById(R.id.info);
            info.setText(e.toString());
        }
    }
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
    public CountDownTimer  getCountDownTimer(){
        long totaltime=getTotaltime();

        CountDownTimer c=new CountDownTimer(totaltime, 1000) {
            TextView info =(TextView) findViewById(R.id.info);
            public void onTick(long millisUntilFinished) {
                if(!isOnline())
                {
                    timer[0].cancel();
                }
                else
                {
                    info.setText("Seconds : " + millisUntilFinished / 1000+" ");

                }

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onFinish() {

                try {
                    info.setText(Integer.toString(Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON)));
                   try{
                        boolean enabled = false;
                        if (Build.VERSION.SDK_INT >16) {
                            enabled = Settings.Global.getInt(getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
                        }
                        else {
                            enabled = Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
                        }
                        if(!enabled)
                        {
                            if (Build.VERSION.SDK_INT <=16) {
                                Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.AIRPLANE_MODE_ON,  0 );
                            }
                            else
                            {
                                Settings.Global.putInt(getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,  0 );
                            }
                            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                            intent.putExtra("state", !enabled);
                            getApplicationContext().sendBroadcast(intent);
                        }
                        else{
                            info.setText("Flight Mode enabled");
                        }

                    }
                    catch(Exception ex){

                            startActivityForResult(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS),0);

                            int secs = 5;
                            new CountDownTimer((secs + 1) * 1000, 2000) // Wait 5 secs, tick every 1 sec
                            {

                                @Override
                                public void onTick(long l) {

                                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
                                }

                                @Override
                                public final void onFinish() {
                                    List<ApplicationInfo> packages;
                                    PackageManager pm;
                                    pm = getPackageManager();
                                    //get a list of installed apps.
                                    packages = pm.getInstalledApplications(0);
                                    String str="";
                                    ActivityManager mActivityManager = (ActivityManager)getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
                                    String myPackage = getApplicationContext().getPackageName();
                                    for (ApplicationInfo packageInfo : packages) {
                                        if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                                            str=str+packageInfo.packageName+"\n";
                                            continue;
                                        }
                                        if(packageInfo.packageName.equals(myPackage)) {
                                            str=str+packageInfo.packageName+"\n";
                                            continue;
                                        }
                                        mActivityManager.killBackgroundProcesses(packageInfo.packageName);
                                        str=str+packageInfo.packageName+"\n";
                                    }
                                    info.setMovementMethod(new ScrollingMovementMethod());
                                    info.setText(str);
                                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15 * 1000);//default tim to be inserted
                                }
                            }.start();
                    }
                }
                catch (Exception e)
                {
                    info.setText(e.toString());
                }
            }
        };
        return c;
    }
    public long getTotaltime(){
        long totaltime=0 ;
        int hours = 0, minutes = 0, seconds = 0;

        EditText val = findViewById(R.id.val);
        String time = val.getText().toString();
        String[] sentences = time.split(":");


        if (sentences.length == 3) {
            hours = Integer.parseInt(sentences[0]);
            minutes = Integer.parseInt(sentences[1]);
            seconds = Integer.parseInt(sentences[2]);
        }
        if (sentences.length == 2) {
            minutes = Integer.parseInt(sentences[0]);
            seconds = Integer.parseInt(sentences[1]);
        }

        if (sentences.length == 1) {
            seconds = Integer.parseInt(sentences[0]);
        }

        totaltime = (seconds * 1000) + (minutes * 1000 * 60) + (hours * 1000 * 60 * 60);

        return totaltime;
    }
}

