package tech.iosd.benefit.DashboardFragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

import tech.iosd.benefit.R;
import tech.iosd.benefit.Services.GPSTracker;
import tech.iosd.benefit.Utils.Constants;

import static android.content.Context.LOCATION_SERVICE;

public class TrackMyActivityRun extends Fragment implements View.OnClickListener
{
    private Context ctx;
    private FragmentManager fm;
    private MapView mMapView;

    private GoogleMap googleMap;
    private View startLayout;
    private View pauseLayout;
    private View stopLayout;
    private View discardBtn;
    private View resumeBtn;
    private View doneBtn;

    private GPSTracker myService;
    private ArrayList<LatLng> points;
    private Polyline polyline;

    long startTime;
    boolean isServiceConnected = false;
    private LocationManager locationManager;
    private boolean isgoogleMap = false;
    //boolean mServiceBound = false;

    private TextView distance;
    double distaceBeforePause = 0;
    double distace_paused= 0;
    double lastDistance =0;
    private boolean fistPuase = true;
    private ProgressDialog progressDialog;

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GPSTracker.LocalBinder binder = (GPSTracker.LocalBinder) service;
            myService = binder.getService();
            GPSTracker.LocalBinder myBinder = (GPSTracker.LocalBinder) service;
            myService = myBinder.getService();
            myService.setmContext(ctx);
            Toast.makeText(getActivity().getApplicationContext(),"service connceted",Toast.LENGTH_LONG).show();

            locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

            isServiceConnected = true;

            if(isgoogleMap){

                    LatLng myLocation = new LatLng(myService.getLatitude(), myService.getLongitude());

                    CameraUpdate center=
                            CameraUpdateFactory.newLatLng(myLocation);
                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(30);

                    googleMap.moveCamera(center);
                    googleMap.animateCamera(zoom);
                    googleMap.setMyLocationEnabled(true);
                    progressDialog.hide();
                    //gpsTracker.showSettingsAlert();


            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            isServiceConnected = false;
        }
    };

    void bindService() {
        if (isServiceConnected == true)
            return;

        Thread t = new Thread(){
            public void run(){
                Intent i = new Intent(getContext(), GPSTracker.class);
                getContext().startService(i);

                getContext().bindService(i, sc, Context.BIND_AUTO_CREATE);
                startTime = System.currentTimeMillis();
                //isServiceConnected = true;
            }
        };

        t.start();


    }

    void unbindService() {
        if (isServiceConnected == false)
           return;
        Intent i = new Intent(getContext(), GPSTracker.class);
        getContext().stopService(i);
        getContext().unbindService(sc);
        isServiceConnected = false;
        Toast.makeText(getActivity().getApplicationContext(),"service disconnceted",Toast.LENGTH_LONG).show();

    }
    //This method leads you to the alert dialog box.
    void checkGps() {
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


            showGPSDisabledAlertToUser();
        }
    }

    //This method configures the Alert Dialog box.
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setMessage("Enable GPS to use application")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading GPS and Maps.");
        progressDialog.setCancelable(false);
        progressDialog.show();
        View rootView = inflater.inflate(R.layout.dashboard_track_my_activity_run, container, false);

        ctx = rootView.getContext();
        distance = rootView.findViewById(R.id.dashboard_track_my_activity_distance_textview);

        bindService();

        fm = getFragmentManager();

        startLayout = rootView.findViewById(R.id.dashboard_track_my_activity_running_start);
        pauseLayout = rootView.findViewById(R.id.dashboard_track_my_activity_running_pause);
        stopLayout = rootView.findViewById(R.id.dashboard_track_my_activity_running_stop);
        discardBtn = rootView.findViewById(R.id.dashboard_track_my_activity_running_discard);
        resumeBtn = rootView.findViewById(R.id.dashboard_track_my_activity_running_resume);
        doneBtn = rootView.findViewById(R.id.dashboard_track_my_activity_running_done);

        distance = (TextView)rootView.findViewById(R.id.dashboard_track_my_activity_distance_textview);

        rootView.findViewById(R.id.back_icon).setOnClickListener(this);
        discardBtn.setOnClickListener(this);
        resumeBtn.setOnClickListener(this);
        doneBtn.setOnClickListener(this);
        startLayout.setOnClickListener(this);
        pauseLayout.setOnClickListener(this);

        mMapView = rootView.findViewById(R.id.track_running_map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try
        {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap mMap)
            {
                isgoogleMap=true;
                googleMap = mMap;

                if (isServiceConnected){
                    progressDialog.hide();
                    LatLng myLocation = new LatLng(myService.getLatitude(), myService.getLongitude());

                    CameraUpdate center=
                            CameraUpdateFactory.newLatLng(myLocation);
                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(30);

                    googleMap.moveCamera(center);
                    googleMap.animateCamera(zoom);
                    googleMap.setMyLocationEnabled(true);
                }

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    googleMap.setMyLocationEnabled(true);

                }

               /* if(isServiceConnected ){
                    LatLng myLocation = new LatLng(myService.getLatitude(), myService.getLongitude());

                    CameraUpdate center=
                            CameraUpdateFactory.newLatLng(myLocation);
                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

                    googleMap.moveCamera(center);
                    googleMap.animateCamera(zoom);
                }*/


            }
        });


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

        if (isServiceConnected == true){
            //myService.stopLocationUpdates();
            unbindService();
            myService.onDestroy();

        }
        fm.popBackStack();



    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.back_icon:

                fm.popBackStack();
                break;
            case R.id.dashboard_track_my_activity_running_start:
            {
                startLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.VISIBLE);
                stopLayout.setVisibility(View.GONE);
                startRunning();
                break;
            }
            case R.id.dashboard_track_my_activity_running_pause:
            {
                startLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.GONE);
                stopLayout.setVisibility(View.VISIBLE);
                discardBtn.setVisibility(View.VISIBLE);

                myService.setPaused(true);
                break;
            }
            case R.id.dashboard_track_my_activity_running_resume:
            {
                //gpsTracker.setPaused(false);
                Toast.makeText(getActivity().getApplicationContext(),"rrere",Toast.LENGTH_LONG).show();
                startLayout.setVisibility(View.GONE);
                pauseLayout.setVisibility(View.VISIBLE);
                stopLayout.setVisibility(View.GONE);
                discardBtn.setVisibility(View.GONE);
                myService.setPaused(false);

                break;

            }

            case R.id.dashboard_track_my_activity_running_discard:
            {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                View mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_accept, null);
                TextView dialogMsg = mView.findViewById(R.id.dialog_message);
                Button dialogAccept = mView.findViewById(R.id.dialog_accept);
                Button dialogCancel = mView.findViewById(R.id.dialog_cancel);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                dialogMsg.setText(R.string.alert_discard_activity);
                dialogAccept.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        dialog.dismiss();
                        startLayout.setVisibility(View.VISIBLE);
                        pauseLayout.setVisibility(View.GONE);
                        stopLayout.setVisibility(View.GONE);
                        discardBtn.setVisibility(View.GONE);
                        myService.stoptacking();
                        distance.setText(String.valueOf(0));

                    }
                });
                dialogCancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override

                    public void onClick(View view)
                    {
                        dialog.dismiss();
                    }
                });
                break;
            }
        }
    }




    private void startRunning() {
            String stringLongitude = String.valueOf(myService.getLongitude());
            myService.setPaused(false);

            Toast.makeText(getActivity().getApplicationContext(),"Lat:"+stringLongitude+"\nLong"+stringLongitude,Toast.LENGTH_LONG).show();

            LatLng sydney = new LatLng(myService.getLatitude(), myService.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Starting Point").snippet("You started your running journey from this point."));

            CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(30).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(30);
        googleMap.animateCamera(zoom);

       /* points.clear();
        myService.setPoints(points);*/

        IntentFilter intentFilter = new IntentFilter(Constants.GPS_UPDATE);

        BroadcastReceiver mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
           //     Toast.makeText(context,"activity",Toast.LENGTH_LONG).show();

            /*

            Intent stopIntent = new Intent(MainActivity.this,
                        BroadcastService.class);
                stopService(stopIntent);
                */

                if (intent.getAction().equals(Constants.GPS_UPDATE)) {
                    //intent.getExtras();
                    redrawLine();
                    //Toast.makeText(getContext(),"activity",Toast.LENGTH_LONG).show();

                }
            }
        };
        getActivity().getApplicationContext().registerReceiver(mReceiver, intentFilter);





    }

    private void redrawLine(){

        points = myService.getPoints();

        double distance_number ;
        distance_number = SphericalUtil.computeLength(points);

        //lastDistance = distance_number - distace_paused;
        //distance_number = distance_number - distace_paused;
        //distace_total = distance_number;
        if(!myService.isPaused()){

            googleMap.clear();

            CameraUpdate center=
                    CameraUpdateFactory.newLatLng(new LatLng(myService.getLatitude(),myService.getLongitude()));

            googleMap.moveCamera(center);

            //distance.setText(String.format("%.2f", gpsTracker.getDistance()));


            PolylineOptions options = new PolylineOptions().width(8).color(Color.BLACK).geodesic(true);
            for (int i = 0; i < points.size(); i++) {
                LatLng point = points.get(i);
                options.add(point);
            }

            if(!fistPuase){
                distace_paused = distace_paused + (distance_number - distaceBeforePause);

            }
            fistPuase =true;
            polyline = googleMap.addPolyline(options); //add Polyline

            distance.setText(String.valueOf(distance_number-distace_paused));

        }else {
            if (fistPuase){
                fistPuase = false;
                distaceBeforePause = distance_number;
            }


        }


    }




}
