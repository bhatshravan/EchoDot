package bs.inc.echodot;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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
import android.widget.Toast;

import bs.inc.echodot.fragments.DisplayMain;
import bs.inc.echodot.fragments.MapFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment(new DisplayMain());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadFragment(new DisplayMain());
                    return true;
                case R.id.navigation_chatbot:
                    return true;
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
