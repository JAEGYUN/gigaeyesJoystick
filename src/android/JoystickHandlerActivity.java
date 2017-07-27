package kr.co.anylogic.joystick;

import android.app.Activity;
import android.content.res.Resources;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.graphics.SurfaceTexture;
import android.util.DisplayMetrics;
import android.content.Context;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;

import java.util.ArrayList;

public class JoystickHandlerActivity extends Activity implements IVLCVout.Callback, TextureView.SurfaceTextureListener  {

    // private Window win;
    LayoutInflater inflater;

    private String packageName;
    private Resources res;

    private String videoSrc = "";
    private String cctvName = "";

//    VideoView mVideoView;
//  이전버전에서는 videoView를 사용했으나, libvlc를 사용하면서 TextureView로 변경함.
    private LibVLC libvlc;
    private MediaPlayer mediaPlayer;
    private TextureView textureView;
//    RelativeLayout vaLayer;
    RelativeLayout mRelativeLayout;


    private static String TAG = "JoystickHandlerActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.packageName = getApplication().getPackageName();
        this.res = getApplication().getResources();
        int main_layout = res.getIdentifier(JoystickEvents.MAIN_LAYOUT, "layout", this.packageName);
        int joystick_overlay = res.getIdentifier(JoystickEvents.JOYSTIC_LAYOUT, "layout", this.packageName);
        int texture_view = res.getIdentifier(JoystickEvents.TEXTURE_VIEW, "id", this.packageName);
        
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            return;
        }

        Bundle extras  = getIntent().getExtras();
        if (extras != null) {
            this.videoSrc = extras.getString("VIDEO_URL");
            this.cctvName = extras.getString("TITLE");
        } else {
            finishWithError();
        }
        
        Log.d(TAG,"videoSrc : "+this.videoSrc);
        // Toast.makeText(getApplicationContext(),"JoystickHandlerActivity videoSrc:"+videoSrc,Toast.LENGTH_SHORT).show();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        this.setContentView(main_layout);

        textureView = (TextureView) findViewById(texture_view);
        textureView.requestFocus();

        textureView.setSurfaceTextureListener(this);

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

//      이미지View 설정
        overLay();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

//      TextureView에서 SurfaceTexture를 사용할 준비가 되어있음을 의미
//      SurfaceTexture는 HardwareLayer를 가져오는 메소드 내부에서 생성되며 ,
//      onSurfaceTextureAvailableonSurfaceTextureAvailable는 생성 후 호출된다
        Log.v(TAG, "Surface is start");
        createPlayer();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
//        TextureView의 width, height 변경시 호출되며 SurfaceTexture의 Buffer Size가 바뀌었음을 의미한다
        Log.v(TAG, "Surface is changed");
    }


    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//      SurfaceTextView가 종료될 때 플레이어를 release 한다
        Log.v(TAG, "Surface is destroyed");


        if(mediaPlayer == null){
            return true;

        }

        Log.d(TAG,"Player is released!!!");
        mediaPlayer.release();
        mediaPlayer = null;

        return true;

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        int main_container = res.getIdentifier(JoystickEvents.MAIN_CONTAINER, "id", this.packageName);
        View layoutMainView = (View)this.findViewById(main_container);

        Log.w("Layout Width - ", String.valueOf(layoutMainView.getWidth()));
        Log.w("Layout Height - ", String.valueOf(layoutMainView.getHeight()));

    }


    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {

    }

    @Override //VLC 레이아웃 설정
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {

    }

    @Override  //하드웨어 가속 에러시 플레이어 종료
    public void onHardwareAccelerationError(IVLCVout vout) {
        releasePlayer();
        Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    //VLC 플레이어 실행
    private void createPlayer() {
        releasePlayer();
        try {
            if (this.videoSrc.length() > 0) {
                Toast toast = Toast.makeText(this, this.videoSrc, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }

            // Create LibVLC
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
//            options.add("--aout=opensles");
            options.add("--rtsp-tcp"); // time stretching
            options.add("-vvv"); // verbosity
            libvlc = new LibVLC(options);

            textureView.setKeepScreenOn(true);

            // Create media player
            mediaPlayer = new MediaPlayer(libvlc);

            // Set up video output
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.setVideoView(textureView);
            vout.addCallback(this);
            vout.attachViews();
            Uri url = Uri.parse(this.videoSrc);
            Media m = new Media(libvlc, url);
            mediaPlayer.setMedia(m);
            mediaPlayer.play();

        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    //플레이어 종료
    private void releasePlayer() {
        Log.d(TAG, "player release!!!");
        if (libvlc == null)
            return;
        mediaPlayer.stop();
        final IVLCVout vout = mediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        libvlc.release();
        libvlc = null;

    }

    public void overLay(){
        int image_view = res.getIdentifier(JoystickEvents.IMAGE_VIEW, "id", this.packageName);

        ImageView iv = (ImageView)findViewById(image_view);
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

        int image_view = res.getIdentifier(JoystickEvents.IMAGE_VIEW, "id", this.packageName);

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
                    GigaeyesJoystick.move(JoystickEvents.MOVE_DOWN);
                }else{                      // left
                    GigaeyesJoystick.move(JoystickEvents.MOVE_LEFT);
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
