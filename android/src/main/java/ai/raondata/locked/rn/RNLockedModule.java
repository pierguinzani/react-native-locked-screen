package ai.raondata.locked.rn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Objects;
import android.app.KeyguardManager;


public class RNLockedModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }


    public RNLockedModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        registerBroadcastReceiver();
    }

    @Override
    public String getName() {
        return "RNLocked";
    }

    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WritableMap params = Arguments.createMap();
            String action = "";
            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                action = "ACTION_USER_PRESENT";
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                action = "ACTION_SCREEN_OFF";
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                action = "ACTION_SCREEN_ON";
            }
            params.putString("action", action);
            sendEvent(reactContext, "evt.rn.locked", params);

        }
    };

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        reactContext.registerReceiver(mScreenStateReceiver, filter);
    }

    @ReactMethod
    public void exitApp(@Nullable Callback callbackBeforeExit) {
        if (callbackBeforeExit != null) {
            callbackBeforeExit.invoke();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @ReactMethod
    public void isScreenLocked(Callback callback) {
        KeyguardManager keyguardManager = (KeyguardManager) reactContext.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isLocked = keyguardManager.isKeyguardLocked();
        callback.invoke(isLocked);
    }
}