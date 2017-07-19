package kr.co.anylogic.joystick;


import android.app.Application;
import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;


import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class echoes a string called from JavaScript.
 */
public class GigaeyesJoystick extends CordovaPlugin {

    private static CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Application app = cordova.getActivity().getApplication();
        String package_name = app.getPackageName();
        Resources res = app.getResources();

        if (action.equals("coolMethod")) {
            this.coolMethod(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("watch")) {
            GigaeyesJoystick.callbackContext = callbackContext;
            String videoUrl = args.getString(0);
            Context context = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(context, JoystickHandlerActivity.class);
            intent.putExtra("VIDEO_URL", videoUrl);
            Log.d("FLP", "Adicionaod extra: " + videoUrl);
            cordova.startActivityForResult(this, intent, 0);
            return true;
        }

        return false;

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
}
