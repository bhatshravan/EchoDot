package bs.inc.echodot.fragments;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.anastr.speedviewlib.SpeedView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bs.inc.echodot.R;
import bs.inc.echodot.libraries.BGService;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by shravan on 14/3/18.
 */

public class DisplayMain extends Fragment {

    public static final int INVALID = Integer.MAX_VALUE;
    Intent mServiceIntent;
    private BGService mSensorService;
    Context ctx;
    private MaterialDialog materialDialog;
    public int signalStrengthDbm = INVALID;
    public int signalStrengthAsuLevel = INVALID;
    public Context getCtx() {
        return ctx;
    }
    private FusedLocationProviderClient fusedLocationProviderClient;
    String carrierConnenctionType="";
    TextView currentSignalView;
    Button knowBtn;
    int mSignalStrength=0;
    String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_SMS, Manifest.permission.INTERNET};
    TelephonyManager Tel,telephonyManager;
    PhoneCustomStateListener MyListener;
    String carrierName="",carrierNetwork="";
    int carrierlang=0,carrierlong=0,carriercid=0,mcc=0,mnc=0;
    double mlat=0,mlang=0,malt=0,mspeed=0;
    SpeedView speedView;
    boolean all=false,run=true;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Map messageMap;


    public DisplayMain() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        messageMap = new HashMap();
        prefs= getActivity().getSharedPreferences("bs.inc.MyService", MODE_PRIVATE);
        editor = prefs.edit();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
        else {

            all=true;
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mlat = location.getLatitude();
                        mlang = location.getLongitude();
                        malt = location.getAltitude();
                        mspeed = location.getSpeed();
                    }
                }
            });

            currentSignalView = view.findViewById(R.id.textVIew);
            speedView = (SpeedView) view.findViewById(R.id.speedView);
            knowBtn = (Button) view.findViewById(R.id.btnModal);
            knowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCustomDialog();
                }
            });

            speedView.setMaxSpeed(140);
            speedView.speedTo(0);


            MyListener = new PhoneCustomStateListener();
            Tel = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            carrierName = Tel.getNetworkOperatorName();
            Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    run=true;

                    Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                }
            }, 3000);

        }

        mSensorService = new BGService(getActivity());
        mServiceIntent = new Intent(getActivity(), mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            editor.putBoolean("run", true);
            editor.apply();
            getActivity().startService(mServiceIntent);
        }


        currentSignalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().stopService(new Intent(getActivity(),BGService.class));
                SharedPreferences prefs= getActivity().getSharedPreferences("bs.inc.MyService", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("run", false);
                editor.apply();
                Toast.makeText(getActivity(),"Stopped service",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    private void showCustomDialog() {
        materialDialog = new MaterialDialog.Builder(getActivity())
                .title("More information")
                .icon(getResources().getDrawable(R.mipmap.ic_launcher))
                .positiveText("Dismiss")
                .content("Carrier: " + carrierName +
                        "\nDBM: " + String.valueOf(signalStrengthDbm) +
                        "\nASU: " + String.valueOf(signalStrengthAsuLevel) +
                        "\nNetwork type: " + carrierNetwork +
                        "\nCell Tower Type: " + carrierConnenctionType +
                        "\nCell CID: " + carriercid +
                        "\nCell Latitude: " + carrierlang +
                        "\nMCC: " + mcc +
                        "\nMNC: " + mnc +
                        "\nMyLatitude: " + mlat +
                        "\nMyLongitute: " + mlang )
                .positiveText("OK")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Toast.makeText(getApplicationContext(), "hehehe", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    class PhoneCustomStateListener extends PhoneStateListener {
        public static final int INVALID = Integer.MAX_VALUE;

        public int signalSupport = 0, signalStrengthValue = 0;
        int where=0;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);


            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    & run) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else {


                messageMap.put("deviceID", Tel.getDeviceId());

                List<CellInfo> cellInfoList = Tel.getAllCellInfo();
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {
                        // cast to CellInfoLte and call all the CellInfoLte methods you need

                        messageMap.put("TestDBM", ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm());
                        messageMap.put("RSRQ", ((CellInfoLte) cellInfo).getCellSignalStrength().getLevel());
                        messageMap.put("TAC", ((CellInfoLte) cellInfo).getCellSignalStrength().getTimingAdvance());

                        if(Build.VERSION.SDK_INT >  25 ) {
                            messageMap.put("CQI", ((CellInfoLte) cellInfo).getCellSignalStrength().getCqi());
                            messageMap.put("RSRQ", ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrq());
                            messageMap.put("RSSNR", ((CellInfoLte) cellInfo).getCellSignalStrength().getRssnr());
                        }
                    }
                }

                signalStrengthDbm = getSignalStrengthByName(signalStrength, "getDbm");
                speedView.speedTo(Math.abs(signalStrengthDbm));


                signalStrengthAsuLevel = getSignalStrengthByName(signalStrength, "getAsuLevel");

                if (signalStrength.isGsm()) {
                    where = 1;
                } else {
                    where = 2;
                    signalStrengthValue = signalStrength.getCdmaDbm();
                }


                String ssignal = signalStrength.toString();

                String[] parts = ssignal.split(" ");
                int dbm = 0;

                carrierNetwork = getNetworkClass(getActivity());

                GsmCellLocation cellLocation = (GsmCellLocation) Tel.getCellLocation();
                carriercid = cellLocation.getCid();
                carrierlang = cellLocation.getLac() & 0xffff;
                String networkOperator = Tel.getNetworkOperator();
                if (!TextUtils.isEmpty(networkOperator)) {
                    mcc = Integer.parseInt(networkOperator.substring(0, 3));
                    mnc = Integer.parseInt(networkOperator.substring(3));
                }

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mlat = location.getLatitude();
                            mlang = location.getLongitude();
                        }
                    }
                });

                currentSignalView.setText("Carrier: " + carrierName +
                        "\nDBM: " + String.valueOf(signalStrengthDbm) +
                        "\nASU: " + String.valueOf(signalStrengthAsuLevel) +
                        //    "\nTest DBM: "+ dbm+
                        "\nNetwork type: " + carrierNetwork +
                        //       "\nCell Tower Type: " + carrierConnenctionType +
                        //       "\nCell CID: " + carriercid +
                        //       "\nCell Latitude: " + carrierlang +
                        //       "\nMCC: " + mcc +
                        //       "\nMNC: " + mnc +
                        "\nMyLatitude: " + mlat +
                        "\nMyLongitute: " + mlang
                );

                messageMap.put("Carrier",carrierName );
                messageMap.put("DBM",signalStrengthDbm );
                messageMap.put("ASU",signalStrengthAsuLevel );
                messageMap.put("NetworkType",carrierNetwork );
                messageMap.put("CellTowerType",carrierConnenctionType );
                messageMap.put("CellId",carriercid );
                messageMap.put("LAC",carrierlang );
                messageMap.put("MCC",mcc );
                messageMap.put("MNC",mnc );
                messageMap.put("MyLatitude",mlat );
                messageMap.put("MyLongitude",mlang );
                messageMap.put("Time", ServerValue.TIMESTAMP);

                DatabaseReference fireDB = FirebaseDatabase.getInstance().getReference().child("TEST_FOR_SHRAVAN_NEVER_CHECK_OR_REFER_THIS_GET_IT_OR_YOU_WILL_DIE").child("Main");;
                String push_id= fireDB.push().getKey();
                //  fireDB.child(push_id).setValue(messageMap);
                messageMap.clear();
                //Toast.makeText(getApplicationContext(),"Updated in firebase",Toast.LENGTH_SHORT).show();
            }
        }

        private int getSignalStrengthByName(SignalStrength signalStrength, String methodName)
        {
            try
            {
                Class classFromName = Class.forName(SignalStrength.class.getName());
                java.lang.reflect.Method method = classFromName.getDeclaredMethod(methodName);
                Object object = method.invoke(signalStrength);
                return (int)object;
            }
            catch (Exception ex)
            {
                return INVALID;
            }
        }
    }

    //Git hub integration test

    public String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                carrierConnenctionType="GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                carrierConnenctionType="EDGE";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                carrierConnenctionType="CDMA";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                carrierConnenctionType="1xRTT";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                carrierConnenctionType="IDEN";
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                carrierConnenctionType="UMTS";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                carrierConnenctionType="EVDO";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                carrierConnenctionType="HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                carrierConnenctionType="HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                carrierConnenctionType="HSPA";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                carrierConnenctionType="EVDO_B";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                carrierConnenctionType="EHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                carrierConnenctionType="HSPAP";
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                carrierConnenctionType="LTE";
                return "4G";
            default:
                return "Unknown";
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    // you may now do the action that requires this permission
                } else {
                    // permission denied
                }
                return;
            }

        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if(all)
            Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(all)
            Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public void onDestroy()
    {
        try {
            if (isMyServiceRunning(mSensorService.getClass()))
                getActivity().stopService(mServiceIntent);
            if (all)
                Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
        }
        catch (Exception e){

        }
        super.onDestroy();
    }

}