package com.example.admin.floatingTool;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
    EditText editName, editPassword;
    TextView result;
    Button buttonSubmit, buttonReset;

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
    Number timerValue;

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
        boolean StoredNotification = userDetails.getBoolean("isNotificationOn", false);
        boolean StoredPopUpVertical = userDetails.getBoolean("isPopupVerticalOn", false);
        boolean StoredVerticalBottom = userDetails.getBoolean("isVerticalBottomOn", false);
        boolean StoredMoveAble = userDetails.getBoolean("isMoveAbleOn", false);
        isNotification = StoredNotification;
        isPopupVertical = StoredPopUpVertical;
        isVerticalBottom = StoredVerticalBottom;
        isMoveAble = StoredMoveAble;
        timerValue = hour;
        Switch cb = (Switch) findViewById(R.id.sw_notification);
        Switch cb1 = (Switch) findViewById(R.id.sw_popupVertical);
        Switch cb2 = (Switch) findViewById(R.id.sw_popUpVertical2);
        Switch cb3 = (Switch) findViewById(R.id.sw_move);
        cb.setChecked(Boolean.parseBoolean(String.valueOf(StoredNotification)));
        cb1.setChecked(Boolean.parseBoolean(String.valueOf(StoredPopUpVertical)));
        cb2.setChecked(Boolean.parseBoolean(String.valueOf(StoredVerticalBottom)));
        cb3.setChecked(Boolean.parseBoolean(String.valueOf(StoredMoveAble)));
        //saved data end


        contentView = getContentView();
        TextView editName1 = findViewById (R.id.editName);
        editName1.setText(String.valueOf(timerValue));


        //Notification system
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("myCh", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "myCh")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("First Notification")
                .setContentText("This is the body of message");
        notification = builder.build();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        //Notification system end


        editName  = (EditText) findViewById(R.id.editName);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        // Attaching OnClick listener to the submit button
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get text from EditText name view
                String name = editName.getText().toString();
                edit.putInt("hour", Integer.parseInt(name));
                edit.commit();
                Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
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
                initFloatWindow(contentView);
            }
        });
        swModely.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isModality = isChecked;
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

        View view = LayoutInflater.from(context).inflate(R.layout.fv_test, null);
        final View ll_menu = view.findViewById(R.id.ll_btn);

        TextView txt_timer = (TextView)view.findViewById (R.id.hrs);
        CountDownTimer mCountDownTimer = timer(txt_timer);

        LinearLayout popUp = (LinearLayout) view.findViewById(R.id.ll_btn);
        LinearLayout popUp1 = (LinearLayout) view.findViewById(R.id.popup);

        PopUpVertical(popUp);
        VerticalBottom(popUp1);


        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountDownTimer.cancel();
                Toast.makeText(context, "button", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountDownTimer.start();
                Toast.makeText(context, "button2", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatWindow.remove();
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_menu.getVisibility() == View.VISIBLE) {
                    ll_menu.setVisibility(View.GONE);
                } else {
                    ll_menu.setVisibility(View.VISIBLE);
                }
            }
        });
        return view;
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
        return new CountDownTimer(1000*1*4, 1000) {
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
                txt_timer.setText("00:00");
                if(isNotification){
                    notificationManagerCompat.notify(1, notification);
                }
            }
        };
    }

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
