package bs.inc.echodot.libraries;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by shravan on 9/3/18.
 */

public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(SensorRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, BGService.class));;
    }
}
