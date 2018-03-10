package bs.inc.echodot.libraries;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class BGService extends Service {
    public int counter = 0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    String carrierConnenctionType="";
    int mSignalStrength=0;
    String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_SMS, Manifest.permission.INTERNET};
    TelephonyManager Tel,telephonyManager;
    PhoneCustomStateListener2 MyListener;
    String carrierName="",carrierNetwork="";
    int carrierlang=0,carrierlong=0,carriercid=0,mcc=0,mnc=0;
    double mlat=0,mlang=0,malt=0,mspeed=0;
    boolean all=false,run=true;




    public BGService(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
    }

    public BGService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 30000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                } else {

                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mlat = location.getLatitude();
                                mlang = location.getLongitude();
                            }
                        }
                    });

                    MyListener = new PhoneCustomStateListener2();
                    Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    carrierName = Tel.getNetworkOperatorName();
                    Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                }
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
///            Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);

        }
            timer.cancel();
            timer = null;
        }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }





    class PhoneCustomStateListener2 extends PhoneStateListener {
        public static final int INVALID = Integer.MAX_VALUE;

        public int signalStrengthDbm = INVALID;
        public int signalStrengthAsuLevel = INVALID;

        public int signalSupport = 0, signalStrengthValue = 0;
        int where=0;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            Toast.makeText(getApplicationContext(),"Service was succesfully started",Toast.LENGTH_SHORT).show();

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    & run) {

            }
            else {

                Map messageMap = new HashMap();
                messageMap.put("deviceID", Tel.getDeviceId());

                List<CellInfo> cellInfoList = Tel.getAllCellInfo();
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {
                        // cast to CellInfoLte and call all the CellInfoLte methods you need

                        messageMap.put("TestDBM", ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm());
                        messageMap.put("RSRQ", ((CellInfoLte) cellInfo).getCellSignalStrength().getLevel());

                        if(Build.VERSION.SDK_INT >  26 ) {
                            messageMap.put("CQI", ((CellInfoLte) cellInfo).getCellSignalStrength().getCqi());
                            messageMap.put("RSRQ", ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrq());
                            messageMap.put("RSSNR", ((CellInfoLte) cellInfo).getCellSignalStrength().getRssnr());
                        }
                    }
                }

                signalStrengthDbm = getSignalStrengthByName(signalStrength, "getDbm");


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

                carrierNetwork = getNetworkClass(getApplicationContext());

                GsmCellLocation cellLocation = (GsmCellLocation) Tel.getCellLocation();
                carriercid = cellLocation.getCid();
                carrierlang = cellLocation.getLac() & 0xffff;
                String networkOperator = Tel.getNetworkOperator();
                if (!TextUtils.isEmpty(networkOperator)) {
                    mcc = Integer.parseInt(networkOperator.substring(0, 3));
                    mnc = Integer.parseInt(networkOperator.substring(3));
                }

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mlat = location.getLatitude();
                            mlang = location.getLongitude();
                        }
                    }
                });

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
                messageMap.put("Tester","Shravan" );

                DatabaseReference fireDB = FirebaseDatabase.getInstance().getReference().child("Signals");
                String push_id= fireDB.push().getKey();
                fireDB.child(push_id).setValue(messageMap);
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
}
    /*
    private FusedLocationProviderClient fusedLocationProviderClient;
    String carrierConnenctionType="";
    int mSignalStrength=0;
    String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_SMS, Manifest.permission.INTERNET};
    TelephonyManager Tel,telephonyManager;
    PhoneCustomStateListener MyListener;
    String carrierName="",carrierNetwork="";
    int carrierlang=0,carrierlong=0,carriercid=0,mcc=0,mnc=0;
    double mlat=0,mlang=0,malt=0,mspeed=0;
    boolean all=false,run=true;

    public BGService() {
        super("BGService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Toast.makeText(getApplicationContext(),"STARTED HERE",Toast.LENGTH_SHORT).show();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        else {

            Toast.makeText(getApplicationContext(),"STARTED HERE",Toast.LENGTH_SHORT).show();
            all=true;

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mlat = location.getLatitude();
                        mlang = location.getLongitude();
                    }
                }
            });

            MyListener = new PhoneCustomStateListener();
            Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            carrierName = Tel.getNetworkOperatorName();
            Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(all)
            Tel.listen(MyListener,PhoneStateListener.LISTEN_NONE);
    }


    class PhoneCustomStateListener extends PhoneStateListener {
        public static final int INVALID = Integer.MAX_VALUE;

        public int signalStrengthDbm = INVALID;
        public int signalStrengthAsuLevel = INVALID;

        public int signalSupport = 0, signalStrengthValue = 0;
        int where=0;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            Toast.makeText(getApplicationContext(),"Service was succesfully started",Toast.LENGTH_SHORT).show();

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    & run) {

            }
            else {

                Map messageMap = new HashMap();
                messageMap.put("deviceID", Tel.getDeviceId());

                List<CellInfo> cellInfoList = Tel.getAllCellInfo();
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {
                        // cast to CellInfoLte and call all the CellInfoLte methods you need

                        messageMap.put("TestDBM", ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm());
                        messageMap.put("RSRQ", ((CellInfoLte) cellInfo).getCellSignalStrength().getLevel());

                        if(Build.VERSION.SDK_INT >  26 ) {
                            messageMap.put("CQI", ((CellInfoLte) cellInfo).getCellSignalStrength().getCqi());
                            messageMap.put("RSRQ", ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrq());
                            messageMap.put("RSSNR", ((CellInfoLte) cellInfo).getCellSignalStrength().getRssnr());
                        }
                    }
                }

                signalStrengthDbm = getSignalStrengthByName(signalStrength, "getDbm");


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

                carrierNetwork = getNetworkClass(getApplicationContext());

                GsmCellLocation cellLocation = (GsmCellLocation) Tel.getCellLocation();
                carriercid = cellLocation.getCid();
                carrierlang = cellLocation.getLac() & 0xffff;
                String networkOperator = Tel.getNetworkOperator();
                if (!TextUtils.isEmpty(networkOperator)) {
                    mcc = Integer.parseInt(networkOperator.substring(0, 3));
                    mnc = Integer.parseInt(networkOperator.substring(3));
                }

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mlat = location.getLatitude();
                            mlang = location.getLongitude();
                        }
                    }
                });

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
                messageMap.put("Tester","Shravan" );

                DatabaseReference fireDB = FirebaseDatabase.getInstance().getReference().child("Signals");
                String push_id= fireDB.push().getKey();
                fireDB.child(push_id).setValue(messageMap);
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
}
*/