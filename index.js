
import { NativeModules, DeviceEventEmitter, Platform } from 'react-native';
const { RNSmsRetriever } = NativeModules;


const SmsRetriever = Platform.OS == "ios" ? {
    getOtp: () => new Promise(),
    getHash: () => new Promise(),
    requestHint: () => new Promise(),
    addListener: (handler) =>
        DeviceEventEmitter
            .addListener('com.RNSmsRetriever:otpReceived', handler),

    removeListener: () => DeviceEventEmitter.removeAllListeners('com.RNSmsRetriever:otpReceived'),
} : {
        ...RNSmsRetriever,
        addListener: (handler) =>
            DeviceEventEmitter
                .addListener('com.RNSmsRetriever:otpReceived', handler),

        removeListener: () => DeviceEventEmitter.removeAllListeners('com.RNSmsRetriever:otpReceived'),
    }


export default SmsRetriever;
