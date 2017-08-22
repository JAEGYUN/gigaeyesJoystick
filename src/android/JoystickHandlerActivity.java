package kr.co.anylogic.joystick;

import android.app.Activity;
import android.content.res.Resources;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.net.Uri;
import android.util.Log;
import android.graphics.SurfaceTexture;
import android.util.DisplayMetrics;
import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import kr.co.anylogic.mediaplayer.GigaeyesPlayerActivity;

public class JoystickHandlerActivity extends Activity implements IVLCVout.Callback, TextureView.SurfaceTextureListener, GestureDetector.OnGestureListener {

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
    RelativeLayout rlTop;
    private static ImageView loadingView;

    boolean clickedFlag = false;

    private static final int SWIPE_MIN_DISTANCE = 160;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    int mode = NONE;

    private GestureDetectorCompat gestureScanner;

    int posX1 = 0, posX2=0, posY1=0, posY2=0;

    float oldDist = 1f;
    float newDist = 1f;

    public static boolean favFlag = false;
    public static boolean onoffFlag = true;

    private static String TAG = "JoystickHandlerActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gestureScanner = new GestureDetectorCompat(this.getBaseContext(), this);

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

            this.onoffFlag = extras.getString(JoystickEvents.REC_STATUS) != null
                    && JoystickEvents.STREAM_VALID_STATUS.equals(extras.getString(JoystickEvents.REC_STATUS));
            this.favFlag = extras.getString(JoystickEvents.FAVORITES) != null
                    && JoystickEvents.FAVORITES_ON.equals(extras.getString(JoystickEvents.FAVORITES));

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
        int loading_view = res.getIdentifier("iv", JoystickEvents.ID, this.packageName);
        int loading_gif = res.getIdentifier("loading", "raw", this.packageName);
        loadingView = (ImageView) findViewById(loading_view);
//        loadingView.setImageResource(GigaeyesConstants.image);

        Glide.with(this).load(loading_gif).into(loadingView);
//        textureView.setOnTouchListener(this);
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
        View layoutMainView = this.findViewById(main_container);

//        Log.w("Layout Width - ", String.valueOf(layoutMainView.getWidth()));
//        Log.w("Layout Height - ", String.valueOf(layoutMainView.getHeight()));

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
            mediaPlayer.setEventListener(mPlayerListener);
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

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        try {
////            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
////                return false;
//
//            // right to left swipe
//            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
////                Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "MOVE LEFT");
//                GigaeyesJoystick.move(JoystickEvents.MOVE_LEFT);
//            }
//            // left to right swipe
//            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
////                Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "MOVE RIGHT");
//                GigaeyesJoystick.move(JoystickEvents.MOVE_RIGHT);
//            }
//            // down to up swipe
//            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
////                Toast.makeText(getApplicationContext(), "Swipe up", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "MOVE UP");
//                GigaeyesJoystick.move(JoystickEvents.MOVE_UP);
//            }
//            // up to down swipe
//            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
////                Toast.makeText(getApplicationContext(), "Swipe down", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "MOVE DOWN");
//                GigaeyesJoystick.move(JoystickEvents.MOVE_DOWN);
//            }
//        } catch (Exception e) {
//
//        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int act = event.getAction();
        String strMsg = "";

        switch(act & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
//                드래그 (첫번째 손가락 터치
                posX1 = (int) event.getX();
                posY1 = (int) event.getY();
                Log.d(TAG, "zoom mode = DRAG");
                mode = DRAG;
                setOverlay(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG){ //DRAG MOVE
                    posX2 = (int) event.getX();
                    posY2 = (int) event.getY();

                    if(Math.abs(posX2 -posX1)>SWIPE_MIN_DISTANCE || Math.abs(posY2 - posY1)>SWIPE_MIN_DISTANCE){
                        if(posX1-posX2>20){
                            Log.d(TAG, "MOVE RIGHT");
                            GigaeyesJoystick.move(JoystickEvents.MOVE_RIGHT);
                        }else if(posX2-posX1>SWIPE_MIN_DISTANCE){
                            Log.d(TAG, "MOVE LEFT");
                            GigaeyesJoystick.move(JoystickEvents.MOVE_LEFT);
                        }else if(posY1-posY2>SWIPE_MIN_DISTANCE){
                            Log.d(TAG, "MOVE DOWN");
                            GigaeyesJoystick.move(JoystickEvents.MOVE_DOWN);
                        }else if(posY2-posY1>SWIPE_MIN_DISTANCE){
                            Log.d(TAG, "MOVE UP");
                            GigaeyesJoystick.move(JoystickEvents.MOVE_UP);
                        }

                        posX1 = posX2;
                        posY1 = posY2;

                    }
                }else if(mode == ZOOM){ // Pinch...
                    newDist = spacing(event);
                    if(newDist - oldDist > SWIPE_MIN_DISTANCE){
                        oldDist = newDist;
                        Log.d(TAG, "ZOOM IN");
                        GigaeyesJoystick.move(JoystickEvents.ZOOM_IN);
                    }else if(oldDist - newDist > SWIPE_MIN_DISTANCE){
                        oldDist = newDist;
                        Log.d(TAG, "ZOOM OUT");
                        GigaeyesJoystick.move(JoystickEvents.ZOOM_OUT);
                    }
                }
                break;
            case MotionEvent.ACTION_UP: // 첫번째 손가락을 떼었을 경우



            case MotionEvent.ACTION_POINTER_UP: //두번째 손가락을 떼었을 경우
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // 두번째 손가락 터치 (핀치 줌)
                mode = ZOOM;
                newDist = spacing(event);
                oldDist = spacing(event);
                break;
            case MotionEvent.ACTION_CANCEL:

            default:
                    break;
        }
//        return gestureScanner.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private float spacing(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x*x+y*y);
    }
    @Override
    public void onLongPress(MotionEvent e) {
        Toast mToast = Toast.makeText(getApplicationContext(), "Long Press", Toast.LENGTH_SHORT);
        mToast.show();

    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Toast.makeText(getApplicationContext(), "SCROLL", Toast.LENGTH_SHORT);
        return true;
    }

    public void onShowPress(MotionEvent e) {
        Toast.makeText(getApplicationContext(), "SHOW", Toast.LENGTH_SHORT);
    }

    public boolean onSingleTapUp(MotionEvent e) {
        Toast.makeText(getApplicationContext(), "SINGLE TAP", Toast.LENGTH_SHORT);
        return true;
    }

    public boolean onDown(MotionEvent e) {
        Toast.makeText(getApplicationContext(), "DOWN", Toast.LENGTH_SHORT);
        return true;
    }
//
//    public boolean onTouch(View v, MotionEvent event) {
//
//        if(event.getAction() == MotionEvent.ACTION_DOWN){
//            setOverlay(true);
//            return true;
//        }
//        return false;
//    }

    Runnable mNavHider = new Runnable() {
        @Override
        public void run() {
            setOverlay(false);
        }
    };

    void setOverlay(boolean visible) {

//   버튼 타이틀 바 overlay 설정.
        if (!visible) {
            ((ViewGroup) rlTop.getParent()).removeView(rlTop);
//            ((ViewGroup) backLayout.getParent()).removeView(backLayout);
            clickedFlag = false;
        }

        if (visible) {
            if(clickedFlag)
                return;

            // addContentView를 호출한다.
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            int back_layout = res.getIdentifier(GigaeyesConstants.BACK_LAYOUT, GigaeyesConstants.LAYOUT, this.packageName);
            int control_overlay = res.getIdentifier(JoystickEvents.CONTROL_LAYOUT, JoystickEvents.LAYOUT, this.packageName);
            int title = res.getIdentifier(JoystickEvents.TITLE, JoystickEvents.ID, this.packageName);
            int btn_on_off = res.getIdentifier(JoystickEvents.button.ONOFF, JoystickEvents.ID, this.packageName);
            int btn_star = res.getIdentifier(JoystickEvents.button.STAR, JoystickEvents.ID, this.packageName);
            int ico_camera_on = res.getIdentifier(JoystickEvents.image.ICO_CAMERA_ON, JoystickEvents.IMAGE, this.packageName);
            int ico_camera_off = res.getIdentifier(JoystickEvents.image.ICO_CAMERA_OFF, JoystickEvents.IMAGE, this.packageName);
            int ico_star_on = res.getIdentifier(JoystickEvents.image.ICO_STAR_ON, JoystickEvents.IMAGE, this.packageName);
            int ico_star_off = res.getIdentifier(JoystickEvents.image.ICO_STAR_OFF, JoystickEvents.IMAGE, this.packageName);

//            backLayout = (RelativeLayout)inflater.inflate(back_layout, null);
//            getWindow().addContentView(backLayout, new RelativeLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT));

            rlTop = (RelativeLayout)inflater.inflate(control_overlay, null);
            getWindow().addContentView(rlTop, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));


            TextView tv = (TextView)findViewById(title);
            tv.setText(this.cctvName);

            ImageButton btnOnoff = (ImageButton)findViewById(btn_on_off);
            if(onoffFlag) {
                btnOnoff.setImageResource(ico_camera_on);
            }else{
                btnOnoff.setImageResource(ico_camera_off);
            }
//            ImageButton btnStar = (ImageButton)findViewById(btn_star);
//            if(favFlag) {
//                btnStar.setImageResource(ico_star_on);
//            }else{
//                btnStar.setImageResource(ico_star_off);
//            }

            // iot_layer, va_layer view를 생성한다.



            enrollButton();

            Handler h = tv.getHandler();
            if (h != null) {
                h.removeCallbacks(mNavHider);
                h.postDelayed(mNavHider, JoystickEvents.DELAY_TIME);
            }
            clickedFlag = true;
        }
    }



    void enrollButton(){
//        버튼
        int btn_back = res.getIdentifier(JoystickEvents.button.BACK, JoystickEvents.ID, this.packageName);
        int btn_on_off = res.getIdentifier(JoystickEvents.button.ONOFF, JoystickEvents.ID, this.packageName);
        int btn_capture = res.getIdentifier(JoystickEvents.button.CAPTURE, JoystickEvents.ID, this.packageName);
        int btn_star = res.getIdentifier(JoystickEvents.button.STAR, JoystickEvents.ID, this.packageName);

        ImageButton btn1 = (ImageButton)findViewById(btn_back);
        btn1.setOnClickListener((new ImageButton.OnClickListener(){
            public void onClick(View v){
                clickBtn1();
            }
        }));

        ImageButton btn2 = (ImageButton)findViewById(btn_on_off);
        btn2.setOnClickListener((new ImageButton.OnClickListener(){
            public void onClick(View v){
                clickBtn2();
            }
        }));


//        ImageButton btn5 = (ImageButton)findViewById(btn_capture);
//        btn5.setOnClickListener((new ImageButton.OnClickListener(){
//            public void onClick(View v){
//                clickBtn5();
//            }
//        }));
//
//        ImageButton btn6 = (ImageButton)findViewById(btn_star);
//        btn6.setOnClickListener((new ImageButton.OnClickListener(){
//            public void onClick(View v){
//                clickBtn6();
//            }
//        }));
    }


    void clickBtn1(){
//      뒤로가기
        releasePlayer();
        finish();
    }

    void clickBtn2(){
//        Toast.makeText(this, "clickBtn2 Clicked!! 녹화중임을 표시합니다.", Toast.LENGTH_LONG).show();
    }


//    void clickBtn5(){
//        Log.d(TAG, "스크린샷을 저장합니다" );
//        getBitmap(textureView);
//
//
//    }
//    void clickBtn6(){
//        Log.d(TAG, "즐겨찾기를 설정합니다" );
//        int btn_star = res.getIdentifier(GigaeyesConstants.button.STAR, GigaeyesConstants.ID, this.packageName);
//        int ico_star_on = res.getIdentifier(GigaeyesConstants.image.ICO_STAR_ON, GigaeyesConstants.IMAGE, this.packageName);
//        int ico_star_off = res.getIdentifier(GigaeyesConstants.image.ICO_STAR_OFF, GigaeyesConstants.IMAGE, this.packageName);
//
//        ImageButton btnStar = (ImageButton)findViewById(btn_star);
//        if(favFlag) {
//            Toast.makeText(this, "즐겨찾기 해제.", Toast.LENGTH_LONG).show();
//            btnStar.setImageResource(ico_star_off);
//            GigaeyesPlayer.setFavorites(GigaeyesConstants.FAVORITES_OFF);
//        }else{
//            Toast.makeText(this, "즐겨찾기 등록", Toast.LENGTH_LONG).show();
//            btnStar.setImageResource(ico_star_on);
//            GigaeyesPlayer.setFavorites(GigaeyesConstants.FAVORITES_ON);
//        }
//
//        favFlag = !favFlag;
//    }

//    public void getBitmap(TextureView vv) {
//        Bitmap bm = vv.getBitmap();
//
//        Bitmap tarVABM = getVAOverlayBitmap();
//        Bitmap tarIoTBM = getIoTOverlayBitmap();
//
//
//        //캔버스를 통해 비트맵을 겹치기한다.
//        Canvas canvas = new Canvas(bm);
//
//        if(vaViewFlag) {
//            canvas.drawBitmap(tarVABM, new Matrix(), null);
//        }
//        if(iotViewFlag) {
//            canvas.drawBitmap(tarIoTBM, new Matrix(), null);
//        }
//
//
//        long now = System.currentTimeMillis();
//        Date date = new Date(now);
//        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
//        String strNow = sdfNow.format(date);
//
//        String mPath = Environment.getExternalStorageDirectory().toString()
//                + "/Pictures/" + strNow + ".png";
//
//        OutputStream fout ;
//        File imageFile = new File(mPath);
//
//        try {
//            fout = new FileOutputStream(imageFile);
//            bm.compress(Bitmap.CompressFormat.PNG, 100, fout);
//            fout.flush();
//            fout.close();
//            Toast.makeText(getApplicationContext(), "스크린샷이 저장되었습니다 : " + mPath, Toast.LENGTH_SHORT).show();
//
//        } catch (FileNotFoundException e) {
//            Log.e(TAG, "FileNotFoundException");
//            e.printStackTrace();
//        } catch (IOException e) {
//            Log.e(TAG, "IOException");
//            e.printStackTrace();
//        }
//
//    }

    public static void hideLoading(){
        JoystickHandlerActivity.loadingView.setVisibility(View.INVISIBLE);
    }

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<JoystickHandlerActivity> mOwner;

        public MyPlayerListener(JoystickHandlerActivity owner) {
            mOwner = new WeakReference<JoystickHandlerActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            JoystickHandlerActivity player = mOwner.get();

            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Opening:
                    Log.d(TAG,"loading bar open....");
//                    JoystickHandlerActivity.hideLoading();
                    break;
                case MediaPlayer.Event.Playing:
                    JoystickHandlerActivity.hideLoading();
                    Log.d(TAG,"loading bar close....");
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }
}
