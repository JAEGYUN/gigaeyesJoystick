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


public class JoystickHandlerActivity extends Activity implements JoystickEvents{

    // private Window win;
    private LayoutInflater inflater;
    private RelativeLayout mRelativeLayout = null;
    private VideoView mVideoView;
    private String videoSrc;
    private int activity_main;
    private int over;
    private int image_view;
    private int video_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            return;
        }

        Bundle extras  = getIntent().getExtras();
        if (extras != null) {
            videoSrc = extras.getString("VIDEO_URL");
            activity_main = extras.getInt("activity_main");
            Log.d("FLP","activity_main id: "+activity_main);
            over = extras.getInt("over");
            image_view = extras.getInt("image_view");
            video_view = extras.getInt("video_view");
        } else {
            finishWithError();
        }



        Log.d("FLP","JoystickHandlerActivity videoSrc"+videoSrc);
        Toast.makeText(getApplicationContext(),"JoystickHandlerActivity videoSrc:"+videoSrc,Toast.LENGTH_SHORT).show();

        // 뷰 설정 전에 호출...
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 메인ContentView 설정
        setContentView(activity_main);

        // Layout 설정
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mRelativeLayout = (RelativeLayout)inflater.inflate(over, null);

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

        mVideoView = (VideoView)findViewById(video_view);

        mVideoView.setVideoURI(uri);
        mVideoView.seekTo(0);
        mVideoView.start();


//      이미지View 설정
        overLay();
    }
    
    /**
     * 조이스틱 이미지 아이콘 레이어...
     */
    public void overLay(){

        ImageView iv = (ImageView)findViewById(image_view);
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
        ImageView iv = (ImageView)findViewById(image_view);
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
//        webView.sendJavascript("moveUp");
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


    private void finishWithError() {
        setResult(100);
        finish();
    }


}
