package bs.inc.echodot;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import bs.inc.echodot.fragments.DisplayMain;
import bs.inc.echodot.fragments.MapFragment;
import bs.inc.echodot.fragments.SettingsFragment;
import bs.inc.echodot.fragments.WEBSS;
import bs.inc.echodot.libraries.BGService;

public class MainActivity extends AppCompatActivity {

    SharedPreferences prefs;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        /*View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        loadFragment(new DisplayMain());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        prefs= getSharedPreferences("bs.inc.MyService", MODE_PRIVATE);
        editor = prefs.edit();
        editor.putBoolean("run", true);
        editor.apply();

        startService(new Intent(MainActivity.this, BGService.class));
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadFragment(new DisplayMain());
                    return true;
                //case R.id.navigation_chatbot:
                //  return true;
                case R.id.navigation_maps:
                    if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED);
                    TelephonyManager Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    GsmCellLocation cellLocation = (GsmCellLocation) Tel.getCellLocation();
                    String networkOperator = Tel.getNetworkOperator();
                    if (!TextUtils.isEmpty(networkOperator)) {
                        loadFragment(new MapFragment());
                    }
                    else
                        Toast.makeText(getApplicationContext(),"No cell signal so cannot load maps",Toast.LENGTH_SHORT).show();
                    return true;

                case R.id.navigation_settings:
                    loadFragment(new SettingsFragment());
                    return true;

                case R.id.navigation_chatbot:
                    Fragment fr = new WEBSS();
                    Bundle ar = new Bundle();
                    ar.putString("url", "https://console.dialogflow.com/api-client/demo/embedded/8670b99d-5452-4eb3-939c-1480b1ad4b92");
                    fr.setArguments(ar);
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container_body, fr,"home");
                    fragmentTransaction.commit();
                    return true;
            }
            return false;
        }
    };



    public void loadFragment(Fragment fr)
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fr,"home");
        fragmentTransaction.commit();
    }

}
