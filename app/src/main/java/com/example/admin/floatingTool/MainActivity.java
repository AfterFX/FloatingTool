package com.example.admin.floatingTool;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    Context context;
    FloatWindow floatWindow;
    View contentView;
    EditText editHrs, editMin, editSec;
    Button buttonSubmit;

    CountDownTimer mCountDownTimer;

    Switch swAutoalign;
    Switch swModely;
    Switch swMove;
    Switch swNotification;
    Switch swPopupVercical, swVerticalBottom;

    boolean isAutoAlign;
    boolean isModality;
    boolean isMoveAble;
    boolean isNotification;
    boolean isPopupVertical, isVerticalBottom;
    int hourValue, minValue, secValue = 0;

    NotificationManagerCompat notificationManagerCompat;
    Notification notification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;


        //saved data
        SharedPreferences userDetails = context.getSharedPreferences("CoolPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = userDetails.edit();
        Number hour = userDetails.getInt("hour", 0);
        Number min = userDetails.getInt("min", 0);
        Number sec = userDetails.getInt("sec", 0);
        boolean StoredAutoAlign = userDetails.getBoolean("isAutoAlignOn", false);
        boolean StoredNotification = userDetails.getBoolean("isNotificationOn", false);
        boolean StoredPopUpVertical = userDetails.getBoolean("isPopupVerticalOn", false);
        boolean StoredVerticalBottom = userDetails.getBoolean("isVerticalBottomOn", false);
        boolean StoredMoveAble = userDetails.getBoolean("isMoveAbleOn", false);
        boolean StoredModality = userDetails.getBoolean("isModality", false);
        isAutoAlign = StoredAutoAlign;
        isNotification = StoredNotification;
        isPopupVertical = StoredPopUpVertical;
        isVerticalBottom = StoredVerticalBottom;
        isMoveAble = StoredMoveAble;
        isModality = StoredModality;
        hourValue = (int) hour; minValue = (int) min; secValue = (int) sec;
        Switch cb = (Switch) findViewById(R.id.sw_autoalign);
        Switch cb1 = (Switch) findViewById(R.id.sw_notification);
        Switch cb2 = (Switch) findViewById(R.id.sw_popupVertical);
        Switch cb3 = (Switch) findViewById(R.id.sw_popUpVertical2);
        Switch cb4 = (Switch) findViewById(R.id.sw_move);
        Switch cb5 = (Switch) findViewById(R.id.sw_modely);
        cb.setChecked(Boolean.parseBoolean(String.valueOf(StoredAutoAlign)));
        cb1.setChecked(Boolean.parseBoolean(String.valueOf(StoredNotification)));
        cb2.setChecked(Boolean.parseBoolean(String.valueOf(StoredPopUpVertical)));
        cb3.setChecked(Boolean.parseBoolean(String.valueOf(StoredVerticalBottom)));
        cb4.setChecked(Boolean.parseBoolean(String.valueOf(StoredMoveAble)));
        cb5.setChecked(Boolean.parseBoolean(String.valueOf(StoredModality)));
        //saved data end


        contentView = getContentView();
        TextView viewHrs = findViewById (R.id.time_hrs);
        TextView viewMin = findViewById (R.id.time_min);
        TextView viewSec = findViewById (R.id.time_sec);
        viewHrs.setText(String.valueOf(hourValue));
        viewMin.setText(String.valueOf(minValue));
        viewSec.setText(String.valueOf(secValue));


        //Notification system
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("myCh", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "myCh")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle(getTitle())
                .setContentText("Hey! Time is over");
        notification = builder.build();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        //Notification system end


        editHrs = (EditText) findViewById(R.id.time_hrs);
        editMin = (EditText) findViewById(R.id.time_min);
        editSec = (EditText) findViewById(R.id.time_sec);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        // Attaching OnClick listener to the submit button
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get text from EditText name view

               if(editHrs.getText().toString().matches("") || editMin.getText().toString().matches("") || editSec.getText().toString().matches("")){
                   Toast.makeText(context, "Please enter number", Toast.LENGTH_SHORT).show();
               }else{
                   String hrs = editHrs.getText().toString();
                   String min = editMin.getText().toString();
                   String sec = editSec.getText().toString();
                   hourValue = Integer.parseInt(hrs);
                   minValue = Integer.parseInt(min);
                   secValue = Integer.parseInt(sec);

                   edit.putInt("hour", Integer.parseInt(hrs));
                   edit.putInt("min", Integer.parseInt(min));
                   edit.putInt("sec", Integer.parseInt(sec));
                   edit.commit();

                   if (mCountDownTimer != null) {
                       mCountDownTimer.cancel();
                       mCountDownTimer.onFinish();
                   }
                   Toast.makeText(context, hrs + ":" + min + ":" + sec, Toast.LENGTH_SHORT).show();
               }
            }
        });

        findViewById(R.id.btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FloatWindow.isAppOpsOn(MainActivity.this)) {
                    FloatWindow.openOpsSettings(MainActivity.this);
                    return;
                }
                initFloatWindow(contentView);
            }
        });

        findViewById(R.id.btn_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatWindow != null) {
                    floatWindow.remove();
                }
            }
        });

        findViewById(R.id.btn_ops).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatWindow.openOpsSettings(MainActivity.this);
            }
        });

        swAutoalign = (Switch) findViewById(R.id.sw_autoalign);
        swModely = (Switch) findViewById(R.id.sw_modely);
        swMove = (Switch) findViewById(R.id.sw_move);
        swNotification = (Switch) findViewById(R.id.sw_notification);
        swPopupVercical = (Switch) findViewById(R.id.sw_popupVertical);
        swVerticalBottom = (Switch) findViewById(R.id.sw_popUpVertical2);

        swAutoalign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAutoAlign = isChecked;
                //save value
                edit.putBoolean("isAutoAlignOn", isChecked);
                edit.commit();
                initFloatWindow(contentView);
            }
        });
        swModely.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isModality = isChecked;
                //save value
                edit.putBoolean("isModality", isChecked);
                edit.commit();
                initFloatWindow(contentView);
            }
        });
        swMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMoveAble = isChecked;
                //save value
                edit.putBoolean("isMoveAbleOn", isChecked);
                edit.commit();
                initFloatWindow(contentView);
            }
        });

        swNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isNotification = isChecked;
                //save value
                edit.putBoolean("isNotificationOn", isChecked);
                edit.commit();
            }
        });

        swPopupVercical.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isPopupVertical = isChecked;
                //save value
                edit.putBoolean("isPopupVerticalOn", isChecked);
                edit.commit();

                contentView = getContentView();
                initFloatWindow(contentView);
            }
        });
        swVerticalBottom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isVerticalBottom = isChecked;
                //save value
                edit.putBoolean("isVerticalBottomOn", isChecked);
                edit.commit();

                contentView = getContentView();
                initFloatWindow(contentView);
            }
        });
    }

    private void RestartApp() {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, 10000, mPendingIntent);
        System.exit(0);
    }

    /**
     * initialize floatWindow
     *
     * @param view
     */
    private void initFloatWindow(View view) {
        if (floatWindow != null) {
            floatWindow.remove();
            floatWindow = null;
        }
        floatWindow = new FloatWindow.With(context, view)
                .setAutoAlign(isAutoAlign)
                .setModality(isModality)
                .setMoveAble(isMoveAble)
//                .setStartLocation(100, 100)
                .create();
        floatWindow.show();
    }

    /**
     * Create a view that needs to be floated
     *
     * @return
     */
    private View getContentView() {

        View fv_test = LayoutInflater.from(context).inflate(R.layout.fv_test, null);
        final View ll_menu = fv_test.findViewById(R.id.ll_btn);

        TextView txt_timer = (TextView)fv_test.findViewById (R.id.timer);


        LinearLayout popUp = (LinearLayout) fv_test.findViewById(R.id.ll_btn);
        LinearLayout popUp1 = (LinearLayout) fv_test.findViewById(R.id.popup);

        PopUpVertical(popUp);
        VerticalBottom(popUp1);


        fv_test.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCountDownTimer == null) {
                    Toast.makeText(context, "there is nothing to stop", Toast.LENGTH_SHORT).show();
                }else{
                    mCountDownTimer.cancel();
                    Toast.makeText(context, "time stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });
        fv_test.findViewById(R.id.timer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCountDownTimer == null) {
//                    mCountDownTimer.cancel();
                    mCountDownTimer = null;
                    mCountDownTimer = timer(txt_timer);
                }else{
                    mCountDownTimer.cancel();
                    mCountDownTimer = null;
                    mCountDownTimer = timer(txt_timer);
                }
                mCountDownTimer.start();
                Toast.makeText(context, "timing started", Toast.LENGTH_SHORT).show();
            }
        });
        fv_test.findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatWindow.remove();
            }
        });
        fv_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_menu.getVisibility() == View.VISIBLE) {
                    ll_menu.setVisibility(View.GONE);
                } else {
                    ll_menu.setVisibility(View.VISIBLE);
                }
            }
        });
        return fv_test;
    }

    private void VerticalBottom(LinearLayout popUp1) {
        if(isVerticalBottom){
            popUp1.setOrientation(LinearLayout.VERTICAL);
        }else{
            popUp1.setOrientation(LinearLayout.HORIZONTAL);
        }
    }

    private void PopUpVertical(LinearLayout popUp) {
        if(isPopupVertical){
            popUp.setOrientation(LinearLayout.VERTICAL);
        }else{
            popUp.setOrientation(LinearLayout.HORIZONTAL);
        }

    }

    private CountDownTimer timer(TextView txt_timer) {
        int th = 1000*60*60*hourValue;
        int tm = 1000*60*minValue;
        int ts = 1000*secValue;
        return new CountDownTimer(th+tm+ts, 1000) {
            public void onTick(long duration) {
                //tTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext resource id
                // Duration
                long Mmin = (duration / 1000) / 60;
                long Ssec = (duration / 1000) % 60;
                if (Ssec < 10) {
                    txt_timer.setText("" + Mmin + ":0" + Ssec);
                } else txt_timer.setText("" + Mmin + ":" + Ssec);
            }

            public void onFinish() {
                txt_timer.setText("Start");
                if(isNotification){
                    notificationManagerCompat.notify(1, notification);
                }
            }
        };
    }

//    public class MyCountDownTimer extends CountDownTimer {
////        MyCountDownTimer myCountDownTimer = new MyCountDownTimer(1000*1*10, 1000); // call this class
//        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
//            super(millisInFuture, countDownInterval);
//        }
//        @Override
//        public void onTick(long millisUntilFinished) {
//            int progress = (int) (millisUntilFinished/1000);
//            Log.d("test check", String.valueOf(progressBar.getMax()-progress));
//            progressBar.setProgress(progressBar.getMax()-progress);
//        }
//        @Override
//        public void onFinish() {
////            finish();
//              cancel();
//        }
//    }

    private View getBtn() {
        Button mBtn = new Button(this);
        mBtn.setText("floating button");
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "click", Toast.LENGTH_SHORT).show();
            }
        });
        return mBtn;
    }
}
