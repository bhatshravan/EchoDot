package bs.inc.echodot.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bs.inc.echodot.R;
import bs.inc.echodot.misc.Cell;
import bs.inc.echodot.misc.CellMain;

/**
 * Created by shravan on 14/3/18.
 */

public class MapFragment extends Fragment {

    ProgressDialog progress;
    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    MapView mMapView;
    private GoogleMap googleMap;
    int carriercid=0,carrierlang=0,mcc=0,mnc=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        // For showing a move to my location button
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED);

        progress= new ProgressDialog(getActivity());
        progress.setMessage("Loading map...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.show();

        TelephonyManager Tel = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) Tel.getCellLocation();
        String networkOperator = Tel.getNetworkOperator();
        if (!TextUtils.isEmpty(networkOperator)) {
            carriercid = cellLocation.getCid();
            carrierlang = cellLocation.getLac() & 0xffff;
            mcc = Integer.parseInt(networkOperator.substring(0, 3));
            mnc = Integer.parseInt(networkOperator.substring(3));



            mMapView = (MapView) rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);

            mMapView.onResume(); // needed to get the map to display immediately

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            displayInMap();
        }
        else{
            Toast.makeText(getActivity(),"No signal",Toast.LENGTH_SHORT).show();
            progress.dismiss();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    double lat,longi;
    JSONObject jo2;
    public void displayInMap()
    {
        String url= "https://ap1.unwiredlabs.com/v2/process.php";

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        Gson gson = new Gson();

        CellMain cellmain= new CellMain();

        cellmain.setMcc(mcc);
        cellmain.setMnc(mnc);
        cellmain.setRadio("gsm");
        cellmain.setToken("9226357cb8dac2");
        cellmain.setAddress(1);
        cellmain.setId(918210281);

        List<Cell> cellList = new ArrayList();
        Cell cell=new Cell();

        cell.setCid(carriercid);
        cell.setLac(carrierlang);
        cell.setPsc(1);

        cellList.add(cell);
        cellmain.setCells(cellList);

        try {
            JsonObject gson2 = new JsonParser().parse(gson.toJson(cellmain)).getAsJsonObject();
            jo2 = new JSONObject(gson2.toString());
        }
        catch (JSONException e)
        {
            Log.e("MYAPP", "unexpected JSON exception", e);
            Toast.makeText(getActivity(),"Error loading map",Toast.LENGTH_SHORT).show();
        }

        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.POST, url, jo2, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status= response.getString("status");
                    if(status.equals("error"))
                    {
                        Toast.makeText(getActivity(),"Sorry there was an error",Toast.LENGTH_SHORT).show();
                    }
                    lat=response.getDouble("lat");
                    longi= response.getDouble("lon");
                    String address= response.getString("address");



                    mMapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap mMap) {
                            googleMap = mMap;

                            // For showing a move to my location button
                            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_PHONE_STATE)
                                    != PackageManager.PERMISSION_GRANTED);

                            googleMap.setMyLocationEnabled(true);

                            // For dropping a marker at a point on the Map
                            LatLng sydney = new LatLng(lat, longi);
                            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                            // For zooming automatically to the location of the marker
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(17).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    });

                    progress.dismiss();


                }
                catch (Exception e)
                {
                    Toast.makeText(getActivity(),"idiot",Toast.LENGTH_SHORT).show();
                    Log.e("meesponse ", "idiot: "+e.toString());
                }
                Log.e("Volley:Response ", ""+response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley:ERROR ", error.getMessage().toString());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}