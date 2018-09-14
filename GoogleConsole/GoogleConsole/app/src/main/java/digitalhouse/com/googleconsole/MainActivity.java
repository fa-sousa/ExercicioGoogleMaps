package digitalhouse.com.googleconsole;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.SupportActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    private Location mLastKnownLocation; //caso nao consiga pegar a localizacao eu pego a ultima
    private FusedLocationProviderClient mFusedLocationProviderClient;//esse cara vai pegar a posição em real time do meu celular
    private static final String TAG = MainActivity.class.getSimpleName();//nas tags de log eu coloco o nome da activity isso é comum
    private boolean mLocationPermissionGranted;//armazena se o cara me deu permissao ou nao
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);//aqui temos a latitude e longitude fixas
    private static final int DEFAULT_ZOOM = 15;//faz o efeito do zoom da camera

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);//para poder ativar o butterknife temos que acrescentar esta linha

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //abaixo é o jeito de pegar um id do mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    //esse cara é o primeiro metodo executado quando nossa activity é chamada
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //estas 3 chamadas abaixo são para executar os metodos que ainda vou criar com estes nomes
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();

    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Erro", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            //perguntar primeiro se tenho permissão
            //Se mLocationPermissionGranted = true - Tenho permissão
            if (mLocationPermissionGranted) {
                //Pegando a logalicação do cara com um Task
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation(); //pegando a ultima localização do usuário
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();

                            //aqui vou criar o metodo latitude e longitude
                            LatLng voce = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(voce).title("calango master"));
                            //aqui eu vou com a camera até a "casa de alguem"
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(voce));
                        } else {
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e("Eu falei !!", e.getMessage());

        }

    }

    //esta anotacao abaixo é do butter knife
    @OnClick (R.id.button2)
    public void findHugo(){

       // BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.common_google_signin_btn_icon_light);
        LatLng hugo = new LatLng(-34, 151);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(hugo)
                .title("Hugo está aqui")
                .snippet("Milho arroz");
                //.icon(icon);

        mMap.addMarker(markerOptions);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(hugo)
                .zoom(16)
                .bearing(90)
                .tilt(30)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

}
