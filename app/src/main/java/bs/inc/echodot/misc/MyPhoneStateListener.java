package bs.inc.echodot.misc;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

/**
 * Created by shravan on 8/3/18.
 */


public class MyPhoneStateListener extends PhoneStateListener {

    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;
    int mSignalStrength = 0;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        mSignalStrength = signalStrength.getGsmSignalStrength();
        mSignalStrength = (2 * mSignalStrength) - 113; // -> dBm
    }
}
