package bs.inc.echodot.fragments;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import bs.inc.echodot.R;
import bs.inc.echodot.libraries.BGService;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by shravan on 14/3/18.
 */

public class SettingsFragment extends Fragment {

    EditText ed;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private BGService mSensorService;

    public SettingsFragment() {    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    Button sett;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings, container, false);

        ed=view.findViewById(R.id.ed1);
        sett=view.findViewById(R.id.settingbutton);

        sett.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setclick();
            }
        });

        return view;
    }

    public void setclick()
    {
        if(!TextUtils.isEmpty(ed.getText()))
            prefs= getActivity().getSharedPreferences("bs.inc.MyService", MODE_PRIVATE);
        editor = prefs.edit();

        int n= Integer.parseInt(ed.getText().toString()) * 1000;
        Intent mServiceIntent = new Intent(getActivity(), BGService.class);
        if (isMyServiceRunning(BGService.class)) {
            getActivity().stopService(new Intent(getActivity(),BGService.class));
        }
        editor.putInt("runtime",n);
        editor.apply();
        getActivity().startService(mServiceIntent);

        Toast.makeText(getActivity(),"Service started succesfullt",Toast.LENGTH_SHORT).show();
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
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
