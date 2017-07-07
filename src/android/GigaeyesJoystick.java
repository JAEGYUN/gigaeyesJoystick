package kr.co.anylogic.joystick;


import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * This class echoes a string called from JavaScript.
 */
public class GigaeyesJoystick extends CordovaPlugin {

    private CallbackContext callbackContext;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Application app = cordova.getActivity().getApplication();
        String package_name = app.getPackageName();
        Resources res = app.getResources();

        int activity_main = res.getIdentifier("activity_main","layout",package_name);
        Log.d("FLP","activity_main id: "+activity_main);
        int over = res.getIdentifier("over","layout",package_name);

        int video_view = res.getIdentifier("videoView","id",package_name);
        int image_view = res.getIdentifier("img_joystick","id",package_name);

        if (action.equals("coolMethod")) {
            this.coolMethod(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("watch")) {
            this.callbackContext = callbackContext;
            String videoUrl = args.getString(0);
            Context context = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(context, JoystickHandlerActivity.class);
            intent.putExtra("VIDEO_URL", videoUrl);
            intent.putExtra("activity_main", activity_main);
            intent.putExtra("over", over);
            intent.putExtra("image_view", image_view);
            intent.putExtra("video_view", video_view);
            Log.d("FLP","Adicionaod extra: "+videoUrl);
            cordova.startActivityForResult(this, intent, 0);
            return true;
        }else if (action.equals("move")) {
            this.callbackContext = callbackContext;
            String moveType = args.getString(0);
            Context context = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(context, JoystickHandlerActivity.class);
            intent.putExtra("MOVE_TYPE", moveType);
            Log.d("FLP","Adicionaod extra: "+moveType);
            cordova.startActivityForResult(this, intent, 0);
            return true;
        }

        return false;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("FLP","Result: "+resultCode);

        if (resultCode == Activity.RESULT_CANCELED || resultCode == Activity.RESULT_OK)  {
            Log.d("FLP", "OK");
            callbackContext.success();
        } else {
            Log.d("FLP", "error");
            callbackContext.error("Failed");
        }
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
