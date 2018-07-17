package com.dnp.app.petlost;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

    private ImageView photoImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView idTextView;

    private GoogleApiClient googleApiClient;
    private GoogleMap mMap;

    private Marker currentLocationMarker;
    private LatLng currentLocationLatLong;
    private DatabaseReference mDatabase;

    static MainActivity act;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //photoImageView = (ImageView) findViewById(R.id.photoImageView);
        //nameTextView = (TextView) findViewById(R.id.nameTextView);
        //emailTextView = (TextView) findViewById(R.id.emailTextView);
        //idTextView = (TextView) findViewById(R.id.idTextView);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        act = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(act, AddPetActivity.class);
                act.startActivity(intent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();

                Toast toast = Toast.makeText(getApplicationContext(), "Actividad principal", Toast.LENGTH_SHORT);
                toast.show();
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //startGettingLocations();
        //mDatabase = FirebaseDatabase.getInstance().getReference();
        //getMarkers();

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////// METODOS LOGIN ///////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {

            GoogleSignInAccount account = result.getSignInAccount();
            //nameTextView.setText(account.getDisplayName());
            Log.i("name",account.getDisplayName());
            //emailTextView.setText(account.getEmail());
            Log.i("email",account.getEmail());
            //idTextView.setText(account.getId());
            Log.i("id",account.getId());
            //Glide.with(this).load(account.getPhotoUrl()).into(photoImageView);
        } else {
            goLogInScreen();
        }
    }

    private void goLogInScreen() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logOut(View view) {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    goLogInScreen();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.not_close_session, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void revoke(View view) {
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    goLogInScreen();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.not_revoke, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////// METODOS MENU ////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_agregar:
                //intent = new Intent(this, AddPetActivity.class);
                //intent.putExtra(String "id", String idTextView.getText());
                intent = new Intent(this, MainActivity2.class);
                this.startActivity(intent);
                Log.i("ActionBar", "Agregar!");
                return true;
            case R.id.action_eliminar:
                intent = new Intent(this, DeletePetActivity.class);
                //intent.putExtra(String "id", String idTextView.getText());
                this.startActivity(intent);
                Log.i("ActionBar", "Eliminar!");;
                return true;
            case R.id.action_salir:
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            goLogInScreen();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.not_close_session, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.i("ActionBar", "Salir!");;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////// METODOS MAPA ////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng arequipa = new LatLng(-16.398910, -71.536908);
        //mMap.addMarker(new MarkerOptions().position(arequipa).title("Dueño"));
        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(12.0f).target(arequipa).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        startGettingLocations();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        getMarkers();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////// METODOS LOCALIZACION ////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onLocationChanged(Location location) {
        mMap.clear();
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        //Agregar marker
        currentLocationLatLong = new LatLng(location.getLatitude(), location.getLongitude());

        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLocationLatLong);
        markerOptions.title("Pet");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationMarker = mMap.addMarker(markerOptions);*/

        //Mover camara a nueva posicion
        //CameraPosition cameraPosition = new CameraPosition.Builder().zoom(20.0f).target(currentLocationLatLong).build();
        //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //Envio de ubicacion a la bd
        /*LocationData locationData = new LocationData(location.getLatitude(), location.getLongitude());
        mDatabase.child("location").child(String.valueOf(new Date().getTime())).setValue(locationData);*/
        //

        Toast toast = Toast.makeText(getApplicationContext(), "Cambio Localizacion", Toast.LENGTH_SHORT);
        toast.show();

        MarkerOptions markerOptions2 = new MarkerOptions();
        markerOptions2.position(currentLocationLatLong);
        markerOptions2.title("Dueño");
        markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentLocationMarker = mMap.addMarker(markerOptions2);

        getMarkers();
    }

    public void startGettingLocations() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean canGetLocation = true;
        int ALL_PERMISION_RESULT = 101;
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; //metros
        long MIN_TIME_BW_UPDATES = 1000 * 5; //milisegundos

        ArrayList<String> permissions = new ArrayList<>();
        ArrayList<String> permissionsToRequest;

        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        //Verifica que el gps e internet esten activos, sino que los encieda el usuario
        if (!isGPS && !isNetwork) {
            showSettingsAlert();
        } else {
            //verifica permisos
            //verifica permisos para ultimas versiones
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISION_RESULT);
                    canGetLocation = false;
                }
            }
        }

        //Verifica si los permisos de FINE y COURSE LOCATION fueron concedidos
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show();
            return;
        }

        //Empieza a solicitar la actualizacion de la ubicacion
        if (canGetLocation) {
            if (isGPS) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            } else if (isNetwork) { //Desde porveedor de internet
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }
            Toast.makeText(this, "Actualizacion de la ubicacion", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se pudo obtener la ubicacion", Toast.LENGTH_SHORT).show();
        }

    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS desactivado!");
        alertDialog.setMessage("Ativar GPS?");
        alertDialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();
        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private void getMarkers(){
        mDatabase.child("location").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        if (dataSnapshot.getValue() != null) {
                            getAllLocations((Map<String, Object>) dataSnapshot.getValue());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }

    private void getAllLocations(Map<String, Object> locations) {
        Date newDate = null;
        LatLng latLng = null;
        for (Map.Entry<String, Object> entry : locations.entrySet()){
            newDate = new Date(Long.valueOf(entry.getKey()));
            Map singleLocation = (Map) entry.getValue();
            latLng = new LatLng((Double) singleLocation.get("latitude"), (Double)singleLocation.get("longitude"));
            //addGreenMarker(newDate, latLng);
        }
        addGreenMarker(newDate, latLng);
    }

    private void addGreenMarker(Date newDate, LatLng latLng) {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(dt.format(newDate));
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ubicacion_perro3));
        mMap.addMarker(markerOptions);
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





}