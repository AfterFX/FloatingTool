package com.example.admin.floatingTool;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.reflect.Method;

/**
 * Created by skyrin on 2017/3/16.
 */

public class FloatWindow {

    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private DisplayMetrics mDisplayMetrics;

    /**
     * The coordinates of the touch point relative to the upper left corner of the view
     */
    private float downX;
    private float downY;
    /**
     * The coordinates of the touch point relative to the upper left corner of the screen
     */
    private float rowX;
    private float rowY;
    /**
     * Floating window display marker
     */
    private boolean isShowing;
    /**
     * Drag minimum offset
     */
    private static final int MINIMUM_OFFSET = 5;

    private Context mContext;
    /**
     * Whether to automatically welt
     */
    private boolean autoAlign;
    /**
     * modal window
     */
    private boolean modality;
    /**
     * Is it draggable
     */
    private boolean moveAble;


    /**
     * transparency
     */
    private float alpha;

    /**
     * initial position
     */
    private int startX;
    private int startY;

    /**
     * View height
     */
    private int height;
    /**
     * View width
     */
    private int width;


    /**
     * Internally defined View, the parent View that handles event interception
     */
    private FloatView floatView;
    /**
     * Views that need to be suspended from the outside
     */
    private View contentView;

    private FloatWindow(With with) {
        this.mContext = with.context;
        this.autoAlign = with.autoAlign;
        this.modality = with.modality;
        this.contentView = with.contentView;
        this.moveAble = with.moveAble;
        this.startX = with.startX;
        this.startY = with.startY;
        this.alpha = with.alpha;
        this.height = with.height;
        this.width = with.width;

        initWindowManager();
        initLayoutParams();
        initFloatView();
    }

    private void initWindowManager() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //Gets a DisplayMetrics object that describes some information about the display, such as its size, density, and font scaling.
        mDisplayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void initFloatView() {
        floatView = new FloatView(mContext);
        if (moveAble) {
            floatView.setOnTouchListener(new WindowTouchListener());
        }
    }

    private void initLayoutParams() {
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (modality) {
            mLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        if (height!=WindowManager.LayoutParams.WRAP_CONTENT){
            mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
        if (width!=WindowManager.LayoutParams.WRAP_CONTENT){
            mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        }
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //Here mLayoutParams.type Not recommended for use TYPE_TOAST, because in some systems with lower versions there will be problems with drag exceptions, although it does not require permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //Floating window background brightness0~1，The larger the value, the darker the background, only if the flags are set WindowManager.LayoutParams.FLAG_DIM_BEHIND This property will take effect
        mLayoutParams.dimAmount = 0.0f;
        //Transparency of floating window0~1，The larger the value, the more opaque
        mLayoutParams.alpha = alpha;
        //The starting position of the floating window
        mLayoutParams.x = startX;
        mLayoutParams.y = startY;
    }

    /**
     * Add the form to the screen
     */
    @SuppressLint("NewApi")
    public void show() {
        if (!isAppOpsOn(mContext)) {
            return;
        }
        if (!isShowing()) {
            mWindowManager.addView(floatView, mLayoutParams);
            isShowing = true;
        }
    }

    /**
     * Whether the floating window is showing
     *
     * @return true if it's showing.
     */
    private boolean isShowing() {
        if (floatView != null && floatView.getVisibility() == View.VISIBLE) {
            return isShowing;
        }
        return false;
    }

    /**
     * Open the floating window settings page
     * Some third-party ROMs cannot be used directly{@link #openAppSettings(Context)}Skip to app details page
     *
     * @param context
     * @return true if it's open successful.
     */
    public static boolean openOpsSettings(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } else {
                return openAppSettings(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Open the app details page
     *
     * @param context
     * @return true if it's open success.
     */
    public static boolean openAppSettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Determine whether the floating window permission is enabled
     * Since android does not provide an api to jump directly to the floating window settings page, this method uses reflection to find related functions for jumping
     * Some third-party ROMs may not work
     *
     * @param context
     * @return true allow false forbid
     */
    public static boolean isAppOpsOn(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = (Integer) method.invoke(object, arrayOfObject1);
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {
            ex.getStackTrace();
        }
        return false;
    }

    /**
     * remove floating window
     */
    public void remove() {
        if (isShowing()) {
            floatView.removeView(contentView);
            mWindowManager.removeView(floatView);
            isShowing = false;
        }
    }

    /**
     * Used to get the height of the system status bar.
     *
     * @return Returns the height of the status bar in pixels.
     */
    private int getStatusBarHeight(Context ctx) {
        int identifier = ctx.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (identifier > 0) {
            return ctx.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }

    class FloatView extends FrameLayout {

        /**
         * record the pressed position
         */
        int interceptX = 0;
        int interceptY = 0;

        public FloatView(Context context) {
            super(context);
            //Here, since a ViewGroup cannot add a contentView that already has a Parent, it is necessary to first determine whether the contentView has a Parent
            //If there is, you need to remove the contentView first
            if (contentView.getParent() != null && contentView.getParent() instanceof ViewGroup) {
                ((ViewGroup) contentView.getParent()).removeView(contentView);
            }

            addView(contentView);
        }

        /**
         * Key code to resolve click and drag conflicts
         *
         * @param ev
         * @return
         */
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            //If this callback returns true, it means that the intercepted TouchEvent is handled by itself, and false means that the TouchEvent is not intercepted and dispatched to be handled by the child view.
            //Solution: If you are dragging the parent View, return true to call your own onTouch to change the position, and if it is a click, return false to respond to the click event of the child view.
            boolean isIntercept = false;
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    interceptX = (int) ev.getX();
                    interceptY = (int) ev.getY();
                    downX = ev.getX();
                    downY = ev.getY();
                    isIntercept = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //Clicking on view on some devices with higher dpi is easy to trigger ACTION_MOVE, so do a filter here
                    isIntercept = Math.abs(ev.getX() - interceptX) > MINIMUM_OFFSET && Math.abs(ev.getY() - interceptY) > MINIMUM_OFFSET;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            return isIntercept;
        }
    }

    class WindowTouchListener implements View.OnTouchListener {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            //Get the coordinates of the touch point relative to the upper left corner of the screen
            rowX = event.getRawX();
            rowY = event.getRawY() - getStatusBarHeight(mContext);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    actionDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    actionMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    actionUp(event);
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    actionOutSide(event);
                    break;
                default:
                    break;
            }
            return false;
        }

        /**
         * Finger click event outside the window
         *
         * @param event
         */
        private void actionOutSide(MotionEvent event) {
            //Since we added the FLAG_WATCH_OUTSIDE_TOUCH flag in layoutParams, this event will be responded when clicking outside the floating window
            //This can be used to extend the click outside the floating window to respond to events
        }

        /**
         * finger lift event
         *
         * @param event
         */
        private void actionUp(MotionEvent event) {
            if (autoAlign) {
                autoAlign();
            }
        }

        /**
         * drag event
         *
         * @param event
         */
        private void actionMove(MotionEvent event) {
            //The coordinates are always calculated under the drag event, and then the floating window position is updated
            updateLocation((rowX - downX), (rowY - downY));
        }

        /**
         * update location
         */
        private void updateLocation(float x, float y) {
            mLayoutParams.x = (int) x;
            mLayoutParams.y = (int) y;
            mWindowManager.updateViewLayout(floatView, mLayoutParams);
        }

        /**
         * finger press event
         *
         * @param event
         */
        private void actionDown(MotionEvent event) {
//            downX = event.getX();
//            downY = event.getY();
        }

        /**
         * automatic welt
         */
        private void autoAlign() {
            float fromX = mLayoutParams.x;

            if (rowX <= mDisplayMetrics.widthPixels / 2) {
                mLayoutParams.x = 0;
            } else {
                mLayoutParams.x = mDisplayMetrics.widthPixels;
            }

            //Here, ValueAnimator is used to smoothly calculate the value between the starting X coordinate and the ending X coordinate, and update the floating window position
            ValueAnimator animator = ValueAnimator.ofFloat(fromX, mLayoutParams.x);
            animator.setDuration(300);
            animator.addUpdateListener(animation -> {
                //This will return the calculated transition value between fromX ~ mLayoutParams.x
                float toX = (float) animation.getAnimatedValue();
                //We use this value directly to update the floating window position
                updateLocation(toX, mLayoutParams.y);
            });
            animator.start();
        }
    }

    public static class With {
        private Context context;
        private boolean autoAlign;
        private boolean modality;
        private View contentView;
        private boolean moveAble;
        private float alpha = 1f;

        /**
         * View height
         */
        private int height = WindowManager.LayoutParams.WRAP_CONTENT;
        /**
         * View width
         */
        private int width = WindowManager.LayoutParams.WRAP_CONTENT;

        /**
         * initial position
         */
        private int startX;
        private int startY;

        /**
         * @param context     context
         * @param contentView The view that needs to be floated
         */
        public With(Context context, @NonNull View contentView) {
            this.context = context;
            this.contentView = contentView;
        }

        /**
         * Whether to automatically welt
         *
         * @param autoAlign
         * @return
         */
        public With setAutoAlign(boolean autoAlign) {
            this.autoAlign = autoAlign;
            return this;
        }

        /**
         * Whether the modal window (whether the event can penetrate the current window)
         *
         * @param modality
         * @return
         */
        public With setModality(boolean modality) {
            this.modality = modality;
            return this;
        }

        /**
         * Is it draggable
         *
         * @param moveAble
         * @return
         */
        public With setMoveAble(boolean moveAble) {
            this.moveAble = moveAble;
            return this;
        }

        /**
         * set start position
         *
         * @param startX
         * @param startY
         * @return
         */
        public With setStartLocation(int startX, int startY) {
            this.startX = startX;
            this.startY = startY;
            return this;
        }

        public With setAlpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public With setHeight(int height) {
            this.height = height;
            return this;
        }

        public With setWidth(int width) {
            this.width = width;
            return this;
        }

        public FloatWindow create() {
            return new FloatWindow(this);
        }
    }
}