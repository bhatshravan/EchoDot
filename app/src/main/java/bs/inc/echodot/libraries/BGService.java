package bs.inc.echodot.libraries;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
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
import java.util.concurrent.TimeUnit;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;


public class BGService extends Service {
    public int counter = 0,counter2=0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    String carrierConnenctionType="";
    int mSignalStrength=0;
    String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_SMS, Manifest.permission.INTERNET};
    TelephonyManager Tel,telephonyManager;
    //    PhoneCustomStateListener2 MyListener;
    String carrierName="",carrierNetwork="";
    int carrierlang=0,carrierlong=0,carriercid=0,mcc=0,mnc=0;
    double mlat=0,mlang=0,malt=0,mspeed=0;
    boolean all=false,run=true,timerover=false;
    SharedPreferences prefs;



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
        Intent broadcastIntent = new Intent(getApplicationContext(),SensorRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;
    int period=20000;
    public void startTimer() {

        prefs= getSharedPreferences("bs.inc.MyService", MODE_PRIVATE);
      //  int period = prefs.getInt("runtime",10*60000);
         period = prefs.getInt("runtime",20000);

        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, period); //
    }

    public static final int INVALID = Integer.MAX_VALUE;

    public int signalStrengthDbm = INVALID;
    public int signalStrengthAsuLevel = INVALID;
    Float myspeed=null;

    public int signalSupport = 0, signalStrengthValue = 0;
    int where=0;


    public void initializeTimerTask() {
        prefs= getSharedPreferences("bs.inc.MyService", MODE_PRIVATE);


        new SpeedTestTask().execute();

        timerTask = new TimerTask() {
            public void run() {
                period = prefs.getInt("runtime",20000);

                if(counter%3==0)
                    new SpeedTestTask().execute();

                SharedPreferences prefs= getSharedPreferences("bs.inc.MyService", MODE_PRIVATE);
                run = prefs.getBoolean("run",true);
                timerover=false;

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        ) {
                }
                else if(!run || counter>10) {
                    stopService(new Intent(getApplicationContext(),BGService.class));
                }
                else{
                    try {

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

                        Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        carrierName = Tel.getNetworkOperatorName();
                        //Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                Tel.listen(new PhoneStateListener() {
                                    @Override
                                    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                                        super.onSignalStrengthsChanged(signalStrength);

                                        Log.i("In timer","Running!!");

                                        //Speedtest
                                        //Test implementation
                                        if(!timerover) {


                                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                                                    != PackageManager.PERMISSION_GRANTED
                                                    || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                                    || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                                    & run) {

                                            }

                                            Map messageMap = new HashMap();
                                            messageMap.put("deviceID", Tel.getDeviceId());

                                            List<CellInfo> cellInfoList = Tel.getAllCellInfo();
                                            for (CellInfo cellInfo : cellInfoList) {
                                                if (cellInfo instanceof CellInfoLte) {
                                                    // cast to CellInfoLte and call all the CellInfoLte methods you need

                                                    messageMap.put("TestDBM", ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm());
                                                    messageMap.put("RSRQ", ((CellInfoLte) cellInfo).getCellSignalStrength().getLevel());

                                                    if (Build.VERSION.SDK_INT > 26) {
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

                                            long millis =System.currentTimeMillis();
                                            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                                                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                                                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));


                                            messageMap.put("DeviceTime",hms);
                                            try {
                                                messageMap.put("speed", myspeed);
                                                messageMap.put("speedinKBps", myspeed / 1000);
                                            }
                                            catch (Exception e)
                                            {
                                                //eror
                                            }
                                            messageMap.put("Carrier", carrierName);
                                            messageMap.put("DBM", signalStrengthDbm);
                                            messageMap.put("ASU", signalStrengthAsuLevel);
                                            messageMap.put("NetworkType", carrierNetwork);
                                            messageMap.put("CellTowerType", carrierConnenctionType);
                                            messageMap.put("CellId", carriercid);
                                            messageMap.put("LAC", carrierlang);
                                            messageMap.put("MCC", mcc);
                                            messageMap.put("MNC", mnc);
                                            messageMap.put("MyLatitude", mlat);
                                            messageMap.put("MyLongitude", mlang);
                                            messageMap.put("Time", ServerValue.TIMESTAMP);
                                            messageMap.put("Tester", "ttert");
                                            messageMap.put("Timer", counter);

                                            DatabaseReference fireDB = FirebaseDatabase.getInstance().getReference().child("TEST_FOR_SHRAVAN_NEVER_CHECK_OR_REFER_THIS_GET_IT_OR_YOU_WILL_DIE").child("service");
                                            String push_id = fireDB.push().getKey();

                                            fireDB.child(push_id).setValue(messageMap);
                                            Log.i("MY IN TIMER","Pushed man");

                                            timerover=true;
                                        }

                                    }



                                }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

                                try {
                                    //Thread.sleep(20000);
                                    Log.i("TAGGYBOY","ENDED IN SUCC??");
                                }
                                catch (Exception e)
                                {
                                    Log.e("TAGGYBOY","ENDED IN FAILURE");
                                }
                                Looper.loop();

                            }
                        }).start();
                    }
                    catch (Exception e)
                    {
                        Log.e("OOPY",e.toString());
                    }
                }
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    class SpeedTestTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            SpeedTestSocket speedTestSocket = new SpeedTestSocket();

            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(SpeedTestReport report) {
                    // called when download/upload is finished
                    Log.v("speedtest", "[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
                    String myspeed2=String.valueOf(report.getTransferRateBit());
                    myspeed=Float.valueOf(myspeed2);
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                }
            });

            speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso");

            return null;
        }
    }
}