package kr.co.anylogic.joystick;



import android.app.Activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.util.DisplayMetrics;
import android.content.Context;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

public class JoystickHandlerActivity extends Activity {

    // private Window win;
    LayoutInflater inflater;

    private String packageName;
    private Resources res;

    VideoView mVideoView;
    RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.packageName = getApplication().getPackageName();
        this.res = getApplication().getResources();
        int main_layout = res.getIdentifier("gigaeyes_joystick", "layout", this.packageName);
        int joystick_overlay = res.getIdentifier("joystick_overlay", "layout", this.packageName);
        int videoView = res.getIdentifier("videoView", "id", this.packageName);
        
        String videoSrc ;

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            return;
        }

        Bundle extras  = getIntent().getExtras();
        if (extras != null) {
            videoSrc = extras.getString("VIDEO_URL");
        } else {
            finishWithError();
        }
        
        Log.d("FLP","JoystickHandlerActivity videoSrc"+videoSrc);
        // Toast.makeText(getApplicationContext(),"JoystickHandlerActivity videoSrc:"+videoSrc,Toast.LENGTH_SHORT).show();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.setContentView(activity_main);

//      Layout 설정
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mRelativeLayout = (RelativeLayout)inflater.inflate(joystick_overlay, null);
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                width, height);

        params.leftMargin = 0;
        params.topMargin = 0;

        getWindow().addContentView(mRelativeLayout, params);

//      동영상View 설정
        Uri uri = Uri.parse(videoSrc);
 
        mVideoView = (VideoView)findViewById(videoView);

        mVideoView.setVideoURI(uri);
        mVideoView.seekTo(0);
        mVideoView.start();

//      이미지View 설정
        overLay();
    }

    public void overLay(){

        int img_joystick = res.getIdentifier("img_joystick", "id", this.packageName);

        ImageView iv = (ImageView)findViewById(img_joystick);
        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                int dpx = pxToDp((int)x);
                int dpy = pxToDp((int)y);


                decision(dpx, dpy);
                return false;
            }
        });
    }

    void decision(int dpx, int dpy){

        int img_joystick = res.getIdentifier("img_joystick", "id", this.packageName);

        ImageView iv = (ImageView)findViewById(img_joystick);
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
                    GigaeyesJoystick.move(JoystickEvents.MOVE_DOWN);
                }else{                      // left
                    GigaeyesJoystick.move(JoystickEvents.MOVE_LEFT;
                }
            }else{              // right, up
                if(dpy + dpx - ww > 0) {   // right
                    GigaeyesJoystick.move(JoystickEvents.MOVE_RIGHT);
                }else{                      // up
                    GigaeyesJoystick.move(JoystickEvents.MOVE_UP);
                }
            }

        }else if(pl < r3){
            // zoomIn, zoomOut
            if(dpy - hw > 0){
                GigaeyesJoystick.move(JoystickEvents.ZOOM_OUT);
            }else{
                GigaeyesJoystick.move(JoystickEvents.ZOOM_IN);
            }
        }
    }

    private int pxToDp(int px) {
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int dp = Math.round(px / scale);
        return dp;
    }


    private void finishWithError() {
        setResult(100);
        finish();
    }


}
