
package com.reactlibrary;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.auth.api.identity.*;
import com.google.android.gms.auth.api.Auth;
import android.app.PendingIntent;
import android.app.Activity;
import android.content.Context;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import java.util.ArrayList;
import java.util.Arrays;

public class RNSmsRetrieverModule extends ReactContextBaseJavaModule implements  GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LifecycleEventListener {

	private static final String TAG = "RNSmsRetriever";
	private static final Integer RESOLVE_HINT = 10001;
	private GoogleApiClient apiClient;
	private ReactApplicationContext context;

	private Promise requestHintCallback;
	private Promise verifyDeviceCallback;
	private WritableMap map;

	private BroadcastReceiver mReceiver;
	private boolean isReceiverRegistered = false;


	private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
		@Override
		public void onActivityResult(Activity activity,int requestCode, int resultCode, Intent data) {
			Log.d(TAG,"callback");
			if (requestCode == RESOLVE_HINT) {
				if (resultCode == Activity.RESULT_OK) {
					try {
						String phoneNumber = Identity.getSignInClient(activity).getPhoneNumberFromIntent(data);
						requestHintCallback.resolve(phoneNumber);
					} catch (Exception e) {
						requestHintCallback.reject("Error getting phone number hint", e.toString());
					}
				}
			}
		}

	};

	public RNSmsRetrieverModule(ReactApplicationContext reactContext) {
		super(reactContext);
		reactContext.addActivityEventListener(mActivityEventListener);
		context=reactContext;
		mReceiver=new RNSmsRetrieverBroadcastReciever(reactContext);
		getReactApplicationContext().addLifecycleEventListener(this);
		registerReceiverIfNecessary(mReceiver);
	}
	@Override
	public void onConnected(Bundle connectionHint) {
		// Connected to Google Play services!
		// The good stuff goes here.
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection has been interrupted.
		// Disable any UI components that depend on Google APIs
		// until onConnected() is called.
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// This callback is important for handling errors that
		// may occur while attempting to connect with Google.
		//
		// More about this in the next section.
	}

	@Override
	public String getName() {
		return TAG;
	}

	@ReactMethod
	public void requestHint(Promise requestHintSuccess) {
		Activity currentActivity = getCurrentActivity();
		requestHintCallback = requestHintSuccess;


		if (currentActivity == null) {
			requestHintCallback.reject("No Activity Found");
			return;
		}

		GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();
		
		Identity.getSignInClient(currentActivity).getPhoneNumberHintIntent(request)
			.addOnSuccessListener(result -> {
				try {
					currentActivity.startIntentSenderForResult(result.getIntentSender(), RESOLVE_HINT, null, 0, 0, 0);
				} catch (Exception e) {
					requestHintCallback.reject("Error starting intent", e.toString());
				}
			})
			.addOnFailureListener(error -> {
				requestHintCallback.reject("Error getting phone number hint", error.toString());
			});
	}
	@ReactMethod
	public void getOtp(Promise verifyDeviceSuccess){
		verifyDeviceCallback=verifyDeviceSuccess;
		SmsRetrieverClient client = SmsRetriever.getClient(context);
		Task<Void> task = client.startSmsRetriever();
		System.out.println(task);

		task.addOnSuccessListener(new OnSuccessListener<Void>() {

			@Override
			public void onSuccess(Void aVoid) {
				Log.e(TAG, "started sms listener");

				verifyDeviceCallback.resolve(true);
			}
		});

		task.addOnFailureListener(new OnFailureListener() {


			@Override
			public void onFailure(@NonNull Exception e) {
				verifyDeviceCallback.reject(e);
			}

		});


	}
	@ReactMethod
	public void getHash(Promise promise) {
		try {
			SignatureHelper helper = new SignatureHelper(context);
			ArrayList<String> signatures = helper.getAppSignatures();
			WritableArray arr = Arguments.createArray();
			for (String s : signatures) {
				arr.pushString(s);
			}
			promise.resolve(arr);
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	private void registerReceiverIfNecessary(BroadcastReceiver receiver) {
		if (getCurrentActivity() == null) return;
		try {
			getCurrentActivity().registerReceiver(
					receiver,
					new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
			);
			Log.d(TAG, "Receiver Registered");
			isReceiverRegistered = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void unregisterReceiver(BroadcastReceiver receiver) {
		if (isReceiverRegistered && getCurrentActivity() != null && receiver != null) {
			try {
				getCurrentActivity().unregisterReceiver(receiver);
				Log.d(TAG, "Receiver UnRegistered");
				isReceiverRegistered = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void onHostResume() {
		registerReceiverIfNecessary(mReceiver);
	}

	@Override
	public void onHostPause() {
		unregisterReceiver(mReceiver);
	}

	@Override
	public void onHostDestroy() {
		unregisterReceiver(mReceiver);
	}

}


