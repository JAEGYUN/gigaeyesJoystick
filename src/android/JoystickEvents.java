package kr.co.anylogic.joystick;

public interface JoystickEvents{

    public static int DELAY_TIME = 4000;
    public static final String FAVORITES_ON = "Y";
    public static final String FAVORITES_OFF = "N";
    public static final String STREAM_VALID_STATUS = "Y";
    public static final String STREAM_INVALID_STATUS_ = "N";
    public static final String STATUS_SUCCESS = "S";
    public static final String STATUS_FAIL = "F";
    public static final String STATUS_EXCEPTION = "Z";
    public static final String ID = "id";
    public static final String LAYOUT = "layout";
    public static final String IMAGE = "drawable";


    static class image{
        public static final String ICO_CAMERA_OFF = "ico_cameraoff";
        public static final String ICO_CAMERA_ON = "ico_cameraon";
        public static final String ICO_CAPTURE = "ico_capture";
        public static final String ICO_STAR_ON = "ico_star";
        public static final String ICO_STAR_OFF = "ico_star_off";
    }

    static class button{
        public static final String BACK = "btn_back";
        public static final String CAPTURE = "btn_capture";
        public static final String ONOFF = "btn_onoff";
        public static final String VA = "btn_va";
        public static final String IOT = "btn_iot";
        public static final String STAR = "btn_star";

    }

    public static final String VIDEO_URL = "VIDEO_URL";
    public static final String VIDEO_TITLE = "TITLE";
    public static final String REC_STATUS = "REC_STATUS";
    public static final String FAVORITES = "IS_FAVORITES";


    public static final String MOVE_UP = "T:U";
    public static final String MOVE_DOWN = "T:D";
    public static final String MOVE_LEFT = "P:L";
    public static final String MOVE_RIGHT = "P:R";
    public static final String ZOOM_IN = "Z:I";
    public static final String ZOOM_OUT = "Z:O";
    public static final String STOP = "S";
    public static final String MAIN_LAYOUT = "gigaeyes_joystick";
    public static final String CONTROL_LAYOUT = "joystic_play_overlay";
    public static final String MAIN_CONTAINER = "main_container";
    public static final String TEXTURE_VIEW = "textureView";
    public static final String IMAGE_VIEW = "img_joystick";
    public static final String JOYSTIC_LAYOUT = "joystick_overlay";
    public static final String TITLE = "camName";
}