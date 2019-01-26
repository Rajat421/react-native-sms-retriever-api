
import { NativeModules, DeviceEventEmitter, Platform } from 'react-native';
const { RNSmsRetriever } = NativeModules;
const SmsRetriever = Symbol('SmsRetriever');


SmsRetriever = Platform.OS == "ios" ? {
    getOtp: () => new Promise,
    getHash: () => new Promise,
    addListener: (handler) =>
        DeviceEventEmitter
            .addListener('com.RNSmsRetriever:otpReceived', handler),

    removeListener: () => DeviceEventEmitter.removeAllListeners('com.RNSmsRetriever:otpReceived'),
} : {
        getOtp: RNSmsRetriever.getOtp,
        getHash: RNSmsRetriever.getHash,

        addListener: (handler) =>
            DeviceEventEmitter
                .addListener('com.RNSmsRetriever:otpReceived', handler),

        removeListener: () => DeviceEventEmitter.removeAllListeners('com.RNSmsRetriever:otpReceived'),
    }


export default SmsRetriever;
