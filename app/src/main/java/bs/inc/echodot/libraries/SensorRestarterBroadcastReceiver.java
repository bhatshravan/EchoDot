package bs.inc.echodot.libraries;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by shravan on 9/3/18.
 */

public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {
    Boolean run=true;
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs= context.getSharedPreferences("bs.inc.MyService", MODE_PRIVATE);
        run = prefs.getBoolean("run",true);
        //
        if(run) {
            Log.i(SensorRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
            context.startService(new Intent(context, BGService.class));
        }
        else
        {

        }
    }
}
