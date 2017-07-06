package kr.co.anylogic.joystick;

import android.app.Activity;


import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.util.DisplayMetrics;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.Button;
import android.widget.VideoView;

import kr.co.anylogic.joystick.JoystickEvents;

import java.io.IOException;

public class JoystickHandlerActivity extends Activity implements JoystickEvents{

    // private Window win;
    private LayoutInflater inflater;
    private RelativeLayout mRelativeLayout = null;
    private VideoView mVideoView;

    private String videoSrc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        // 
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            return;
        }

        Bundle extras  = getIntent().getExtras();
        if (extras != null) {
            videoSrc = extras.getString("VIDEO_URL");
        } else {
            finishWithError();
        }

        Log.d("FLP","gigaeyesActivity videoSrc"+videoSrc);
        Toast.makeText(getApplicationContext(),"gigaeyesActivity videoSrc:"+videoSrc,Toast.LENGTH_SHORT).show();

        setContentView(R.layout.activity_main);

        mRelativeLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                width, height);

        params.leftMargin = 0;
        params.topMargin = 0;

        Uri uri = Uri.parse(videoSrc);
        mVideoView = new VRVideoView(this, uri);

        mRelativeLayout.addView(mVideoView, params);

        overLay();
    }

    public void overLay(){
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rlTop = (RelativeLayout)inflater.inflate(R.layout.over, null);
        getWindow().addContentView(rlTop, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView iv = (ImageView)findViewById(R.id.img_joystick);
        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                Context ct = getApplicationContext();
                int dpx = pxToDp((int)x);
                int dpy = pxToDp((int)y);


                decision(dpx, dpy);
                return false;
            }
        });
    }

    void decision(int dpx, int dpy){
        ImageView iv = (ImageView)findViewById(R.id.img_joystick);
        int imgW = iv.getWidth();
        int ww = pxToDp(imgW);

        int hw = ww/2;

        int r1 = hw * hw;
        int r2 = (hw*3/4) * (hw*3/4);
        int r3 = (hw*5/8) * (hw*5/8);

        int pl = (dpx - hw)*(dpx - hw) + (dpy - hw)*(dpy - hw);

        if(pl <= r1 && pl > r2){
            // 네방향
            if(dpy - dpx > 0){              // left, down
                if(dpy + dpx - ww > 0) {   // down
                    down();
                }else{                      // left
                    left();
                }
            }else{              // right, up
                if(dpy + dpx - ww > 0) {   // right
                    right();
                }else{                      // up
                    up();
                }
            }

        }else if(pl < r3){
            // zoomIn, zoomOut
            if(dpy - hw > 0){
                zoomOut();
            }else{
                zoomIn();
            }
        }
    }

    void up(){
        Toast.makeText(getApplicationContext(), "up button clicked!", Toast.LENGTH_SHORT).show();
        webView.sendJavascript("moveUp");
    }

    void down(){
        Toast.makeText(getApplicationContext(), "down button clicked!", Toast.LENGTH_SHORT).show();
    }

    void left(){
        Toast.makeText(getApplicationContext(), "left button clicked!", Toast.LENGTH_SHORT).show();
    }

    void right(){
        Toast.makeText(getApplicationContext(), "right button clicked!", Toast.LENGTH_SHORT).show();
    }

    void zoomIn(){
        Toast.makeText(getApplicationContext(), "zoomIn button clicked!", Toast.LENGTH_SHORT).show();
    }

    void zoomOut(){
        Toast.makeText(getApplicationContext(), "zoomOut button clicked!", Toast.LENGTH_SHORT).show();
    }

    private int pxToDp(int px) {
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int dp = Math.round(px / scale);
        return dp;
    }

    private int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
    
    public boolean onTouchEvent(MotionEvent event) {
        if(mVideoView != null){
            mVideoView.setTouchEvent(event);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void finishWithError() {
        setResult(100);
        finish();
    }


}
