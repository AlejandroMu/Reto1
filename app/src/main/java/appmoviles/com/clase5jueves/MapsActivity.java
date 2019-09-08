package appmoviles.com.clase5jueves;


import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.text.DecimalFormat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import java.util.*;
import android.location.Address;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.view.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapLongClickListener {

    public static final double MIN_DISTANCIA=20.0;
    private GoogleMap mMap;
    private Polygon icesiArea;
    private Marker miUbicacion;

    private TextView sitioTV;
    private ArrayList<Marker> markers;
    private boolean addMarker;
    private Button addMarkerButton;
    private LatLng tmp;
    private EditText addtitle;
    private Button add;
    private Geocoder coder;
    private double distance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        distance=Double.MAX_VALUE;
        mapFragment.getMapAsync(this);
        coder=new Geocoder(this);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
        }, 11);

        add=findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String msm=addtitle.getText().toString();
                double d=distance(miUbicacion,tmp);
                if(d<distance){
                    distance=d;
                    sitioTV.setText("El marcador: "+msm+" es el más cercano");
                }
                Marker marker =  mMap.addMarker(new MarkerOptions().position(tmp).title(msm).snippet("Distancia a mi ubicación: "+d));
                addtitle.setVisibility(View.GONE);
                addMarkerButton.setVisibility(View.VISIBLE);
                add.setVisibility(View.GONE);
                addtitle.setText("");
                markers.add(marker);           
             }

        });


        addMarkerButton=findViewById(R.id.addMarker);
        addMarkerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addMarker=true;
                addMarkerButton.setVisibility(View.GONE);

            }
        });

        addtitle=findViewById(R.id.addTitle);
        sitioTV = findViewById(R.id.sitioTV);
        markers = new ArrayList<>();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        boolean newL=mMap==null;
        mMap = googleMap;
        // if(newL){
        //     for (int i = 0; i < markers.size(); i++) {
        //         mMap.addMarker();
        //     }
        // }
        mMap.setOnMapLongClickListener(this);
        LatLng icesi = new LatLng(3.341552, -76.529784);
        if(miUbicacion==null){
            miUbicacion = mMap.addMarker(new MarkerOptions().position(icesi).title("Icesi"));
        }
        addAdress(miUbicacion);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion.getPosition(), 15));

        //Solicitud de ubicación
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);        
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    public double distance(Marker a,LatLng b){
        double distance = Math.sqrt( Math.pow(a.getPosition().latitude-b.latitude,2) + Math.pow(a.getPosition().longitude-b.longitude,2) );
        distance = distance * 111.12 * 1000;
        DecimalFormat f=new DecimalFormat("#0.00000");
        return Double.parseDouble(f.format(distance));
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        miUbicacion.setPosition(pos);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        addAdress(miUbicacion);
        calcualateDistance();
    }

    public void calcualateDistance(){
        if(markers.size()>=1){
            Marker min=markers.get(0);
            distance=distance(min,miUbicacion.getPosition());
            for (int i = 1; i < markers.size(); i++) {
                Marker marker=markers.get(i);
                double tmp=distance(marker,miUbicacion.getPosition());
                marker.setSnippet("Distancia a mi ubicación: "+tmp+" metros");
                if(tmp<distance){
                    min=marker;
                    distance=tmp;
                }
            }
            String msm="Usted esta en: ";
            if(distance<MIN_DISTANCIA){
                msm="El marcador: ";
            }
            sitioTV.setText(msm+min.getTitle());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    //Si dejamos sostenido el dedo 1.5 s
    @Override
    public void onMapLongClick(LatLng latLng) {
        if(addMarker){
            addtitle.setVisibility(View.VISIBLE);
            add.setVisibility(View.VISIBLE);

            tmp=latLng;
            addMarker=false;
            
        }
    }

    public boolean addAdress(Marker marker){
        try {
            LatLng tmp=marker.getPosition();
            List<Address> address=coder.getFromLocation(tmp.latitude,tmp.longitude,1);
            if(address!=null){
                Address adr=address.get(0);
                String name=adr.getAddressLine(0);
                miUbicacion.setTitle(name);
                miUbicacion.setSnippet("Mi Ubicación");
            }
        return true;
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }   
    }
}
