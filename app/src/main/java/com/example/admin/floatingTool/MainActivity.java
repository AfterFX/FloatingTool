package com.example.admin.floatingTool;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    Context context;
    FloatWindow floatWindow;
    View contentView;

    Switch swAutoalign;
    Switch swModely;
    Switch swMove;
    Switch swNotification;

    boolean isAutoAlign;
    boolean isModality;
    boolean isMoveAble;
    boolean isNotification;

    NotificationManagerCompat notificationManagerCompat;
    Notification notification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        contentView = getContentView();



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
                initFloatWindow(contentView);
            }
        });

        swNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isNotification = isChecked;
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
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "button", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView myTextView1 = (TextView)view.findViewById (R.id.testukas);
                myTextView1.setText ("this is my textview1 test");

                if(isNotification){
                    notificationManagerCompat.notify(1, notification);
                }


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
