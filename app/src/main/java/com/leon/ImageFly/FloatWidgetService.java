package com.leon.ImageFly;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.FileNotFoundException;


public class FloatWidgetService extends Service {

    boolean isLocked = false;
    WindowManager.LayoutParams params = null;
    private WindowManager mWindowManager;
    private View mFloatingWidget;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
//==================================================================================================WIDGET_PARAMS==============

        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 100;
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            assert mWindowManager != null;
            mWindowManager.addView(mFloatingWidget, params);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 100;
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            assert mWindowManager != null;
            mWindowManager.addView(mFloatingWidget, params);
        }


//==================================================================================================INITIALIZING=================

        RelativeLayout r = mFloatingWidget.findViewById(R.id.container);
        ImageView image = mFloatingWidget.findViewById(R.id.image);
        ImageButton exit = mFloatingWidget.findViewById(R.id.button);
        ImageButton changeImage = mFloatingWidget.findViewById(R.id.imageButton2);
        ImageButton scaleButton = mFloatingWidget.findViewById(R.id.scale);
        ImageButton rotateButton = mFloatingWidget.findViewById(R.id.rotate);
        ImageButton lockButton = mFloatingWidget.findViewById(R.id.lock);

        exit.bringToFront();
        changeImage.bringToFront();
        scaleButton.bringToFront();
        rotateButton.bringToFront();
        lockButton.bringToFront();

//==================================================================================================LISTENER===================

        changeImage.setOnClickListener(v -> {
            if (image.getVisibility() == View.VISIBLE) {
                image.setVisibility(View.GONE);
                scaleButton.setVisibility(View.GONE);
                r.setBackgroundResource(R.color.gray);
                params.x = 0;
                params.y = 0;
                mWindowManager.updateViewLayout(mFloatingWidget, params);
                changeImage.setBackgroundResource(R.drawable.visible);
            } else {
                image.setVisibility(View.VISIBLE);
                scaleButton.setVisibility(View.VISIBLE);

                r.setBackgroundResource(R.color.durchsichtig);
                changeImage.setBackgroundResource(R.drawable.invisible);
            }

        });

        exit.setOnClickListener(v -> stopSelf());

        rotateButton.setOnClickListener(v -> image.setRotation(image.getRotation() + 90));

        lockButton.setOnClickListener(view -> {
            if (isLocked) {
                isLocked = false;
                lockButton.setBackgroundResource(R.drawable.lock1);
            } else {
                isLocked = true;
                lockButton.setBackgroundResource(R.drawable.lock2);
            }
        });

        r.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isLocked) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            r.setBackgroundResource(R.color.colorbg);
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (image.getVisibility() == View.GONE) {
                                r.setBackgroundResource(R.color.gray);
                            } else {
                                r.setBackgroundResource(R.color.durchsichtig);
                            }
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(mFloatingWidget, params);
                            return true;
                    }
                }
                return false;
            }
        });

        scaleButton.setOnTouchListener(new View.OnTouchListener() {
            private int X;
            private int Y;
            private float TouchX;
            private float TouchY;
            private float LastTouchX;
            private float LastTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (!isLocked) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            r.setBackgroundResource(R.color.colorbg);

                            return true;
                        case MotionEvent.ACTION_UP:

                            r.setBackgroundResource(R.color.durchsichtig);
                            image.setVisibility(View.VISIBLE);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            image.setVisibility(View.INVISIBLE);
                            android.view.ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
                            layoutParams.height = (int) (event.getRawY());
                            layoutParams.width = (int) (event.getRawX());
                            r.updateViewLayout(image, layoutParams);

                            return true;

                    }
                }

                return false;
            }
        });
//==================================================================================================GET_IMAGE=================

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getApplicationContext().openFileInput("temp_bitmap"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        image.setImageBitmap(bitmap);
    }
//============================================================================================================================

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingWidget != null) mWindowManager.removeView(mFloatingWidget);
    }

}

