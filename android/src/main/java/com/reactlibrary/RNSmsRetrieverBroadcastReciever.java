package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RNSmsRetrieverBroadcastReciever extends BroadcastReceiver {
    private ReactApplicationContext mContext;
    private static final String EVENT = "com.RNSmsRetriever:otpReceived";

    public RNSmsRetrieverBroadcastReciever() {
        super();
    }

    public RNSmsRetrieverBroadcastReciever(ReactApplicationContext context) {
        mContext = context;
    }

    private void receiveMessage(String message) {

        if (mContext == null) {
            return;
        }

        if (!mContext.hasActiveCatalystInstance()) {
            return;
        }


        WritableNativeMap receivedMessage = new WritableNativeMap();

        receivedMessage.putString("message", message);

        mContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT, receivedMessage);
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println(intent);
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();

            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            System.out.println(status.getStatusCode());
            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    receiveMessage(message);

                    break;
                case CommonStatusCodes.TIMEOUT:

                    break;
            }
        }

    }

}