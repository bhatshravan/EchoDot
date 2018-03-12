package bs.inc.echodot;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import bs.inc.echodot.libraries.BGService;

/**
 * Created by shravan on 11/3/18.
 */

public class Settings extends AppCompatActivity {
    EditText ed;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private BGService mSensorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ed=findViewById(R.id.ed1);

    }
    public void click(View view)
    {
        if(!TextUtils.isEmpty(ed.getText()))
        prefs= getSharedPreferences("bs.inc.MyService", MODE_PRIVATE);
        editor = prefs.edit();

        int n= Integer.parseInt(ed.getText().toString()) * 1000;
        Intent mServiceIntent = new Intent(Settings.this, BGService.class);
        if (isMyServiceRunning(BGService.class)) {
            stopService(new Intent(Settings.this,BGService.class));
        }
        editor.putInt("runtime",n);
        editor.apply();
        startService(mServiceIntent);
        finish();

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

}
