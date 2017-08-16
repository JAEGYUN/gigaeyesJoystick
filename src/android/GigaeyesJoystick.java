package kr.co.anylogic.joystick;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;


import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.co.anylogic.mediaplayer.GigaeyesPlayer;

/**
 * This class echoes a string called from JavaScript.
 */
public class GigaeyesJoystick extends CordovaPlugin {

    private static CallbackContext callbackContext;
    private static String camId;
    private static String TAG = "GigaeyesJoystick";
    //    private static
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("coolMethod")) {
            this.coolMethod(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("watch")) {
            GigaeyesJoystick.callbackContext = callbackContext;
            String videoUrl = args.getString(0);
            String title = args.getString(1);
            this.camId = args.getString(2);
            String record_status = args.getString(3);
            String favorites = args.getString(4);

//            if(args.length()>1){
//                title = args.getString(1);
//            }
            Context context = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(context, JoystickHandlerActivity.class);
            intent.putExtra(JoystickEvents.VIDEO_URL, videoUrl);
            intent.putExtra(JoystickEvents.VIDEO_TITLE, title);
            intent.putExtra(JoystickEvents.REC_STATUS, record_status);
            intent.putExtra(JoystickEvents.FAVORITES, favorites);
            Log.d(TAG, "Adicionaod extra: " + videoUrl);
            cordova.startActivityForResult(this, intent, 0);
            return true;
        }

        return false;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG,"Result: "+resultCode);

        if (resultCode == Activity.RESULT_CANCELED || resultCode == Activity.RESULT_OK)  {
            Log.d(TAG, "OK");
//            callbackContext.success();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "ok");
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        } else {
            Log.d(TAG, "error");
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

    static void move(String moveType){
        if(callbackContext != null){

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, moveType);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        }
    }

    /**
     * 즐겨찾기
     */
    static void setFavorites(String favorites) {
        if(callbackContext != null){
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, favorites);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);


        }

    }
}
