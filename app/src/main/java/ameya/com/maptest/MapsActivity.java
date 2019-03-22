package ameya.com.maptest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMarkerDragListener {
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference mDatabase;
    private ArrayList<Recommendation> mrecommendations=new ArrayList<>();
    ListView listView;
    GarageListAdapter garageListAdapter;
    Button addparkbtn;
    ArrayList<Garage> mgarage=new ArrayList<>();
    Dialog dialog;
    ArrayList<GarageParking> parkinglist=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog=new Dialog(this);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MapsActivity.this, Login.class));
                    finish();
                }
            }
        };

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final ValueEventListener postListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                parkinglist.clear();
                Log.d(TAG, "onDataChange: Parking called");
                Log.d(TAG, "onDataChange: Parking got checked?"+dataSnapshot.hasChildren());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    GarageParking gparking = postSnapshot.getValue(GarageParking.class);
                    parkinglist.add(gparking);
                    Log.d(TAG, "onDataChange: "+parkinglist.isEmpty());
                }
                //populatemarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("MapsActivity:Garage:", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("parkings").addValueEventListener(postListener2);
        final ValueEventListener postListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mgarage.clear();
                Log.d(TAG, "onDataChange: Garage called");
                Log.d(TAG, "onDataChange: Garages got checked?"+dataSnapshot.hasChildren());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Garage garage = postSnapshot.getValue(Garage.class);
                    mgarage.add(garage);
                }
                populatemarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("MapsActivity:Garage:", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("garage").addValueEventListener(postListener1);
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mrecommendations.clear();
                Log.d(TAG, "onDataChange: Recommendation called");
                Log.d(TAG, "onDataChange: Recommendations got checked?"+dataSnapshot.hasChildren());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Recommendation recommendation = postSnapshot.getValue(Recommendation.class);
                    mrecommendations.add(recommendation);
                }
                populatemarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("MapsActivity:Recommendation:", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("recommendation").addValueEventListener(postListener);
      /*  try {
            GeoJsonLayer layer = new GeoJsonLayer(mMap, R.raw.parkinglot, getApplicationContext());
            layer.addLayerToMap();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }*/



    }
    private void bookrecommendation(String key) {
        //String key = mDatabase.child("recommendation").push().getKey();
        //Recommendation post = new Recommendation(key,desc, btime.toString(), etime.toString(), lat, lang, 1);
        Log.d(TAG, "bookrecommendation: "+key);
        mDatabase.child("recommendation").child(key).child("status").setValue(1);
    }

    private void writeNewrecommendation(String desc, Date btime, Date etime, Double lat, Double lang) {
        String key = mDatabase.child("recommendation").push().getKey();
        Recommendation post = new Recommendation(key,desc, btime.toString(), etime.toString(), lat, lang, 0);
        Log.d(TAG, "writeNewrecommendation: "+key);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/recommendation/"+ key, postValues);
        mDatabase.updateChildren(childUpdates);

    }
    private void writeNewgarage(String toString, double latitude, double longitude) {
        String key = mDatabase.child("garage").push().getKey();
        Garage post = new Garage(0,toString,latitude, longitude,key);
        Log.d(TAG, "writeNewGarage: "+key);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/garage/"+ key, postValues);
        mDatabase.updateChildren(childUpdates);
    }
    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        else if(item.getItemId()==R.id.add_garage){
            addgarage();
        }
        else if (item.getItemId() == R.id.add_marker){
            addrecommendation();
        }
        else if (item.getItemId() == R.id.profile){
            startActivity(new Intent(MapsActivity.this,ProfileActivity.class));
        }
        else if (item.getItemId() == R.id.help){
            startActivity(new Intent(MapsActivity.this,Help.class));
        }
        else if(item.getItemId() == R.id.sign_out){
            auth.signOut();
            finish();
        }

        return true;
    }

    public void addrecommendation() {
        Log.e(TAG, "addrecommendation: Inside method");
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()))
                .draggable(true)).setTag("Recommendation");
    }
    public void addgarage() {
        Log.e(TAG, "addgarage: Inside method");
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()))
                .draggable(true)).setTag("Garage");
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
    mMap.setOnMarkerDragListener(this);
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                if(marker.getTag()=="Recommendation"){
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Book Parking")
                        .setMessage("Are you sure you want to book this parking?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                for(Recommendation r : mrecommendations)
                                    if(r.lang==marker.getPosition().longitude && r.lat==marker.getPosition().latitude) {
                                        bookrecommendation(r.key);
                                    }
                            }
                        })

                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();}
                        if(marker.getTag()=="Garage"){
                            dialog.setContentView(R.layout.garage_layout);
                            final TextView txtview;
                            listView =dialog.findViewById(R.id.garagelist);


                            txtview=dialog.findViewById(R.id.parking_title1);
                            TextView closebtn=dialog.findViewById(R.id.close_button);
                            closebtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            for(Garage r : mgarage)
                                if(r.lang==marker.getPosition().longitude && r.lat==marker.getPosition().latitude) {
                                    Log.d(TAG, "onInfoWindowClick:"+r.key+r.title);
                                    txtview.setText(r.title);
                                    Log.d(TAG, "onInfoWindowClick: Step before loop"+parkinglist.isEmpty());
                                    for(GarageParking garageParking:parkinglist){
                                    {Log.d(TAG, "onInfoWindowClick: "+garageParking.gkey+" == "+r.key);
                                        if(garageParking.gkey.equals(r.key)){
                                            garageListAdapter=new GarageListAdapter(getApplicationContext(),parkinglist);
                                            listView.setAdapter(garageListAdapter);
                                        }
                                    }
                                    }

                                }

                            addparkbtn=dialog.findViewById(R.id.add_parking_btn);
                            addparkbtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    for(Garage r : mgarage)
                                        if(r.lang==marker.getPosition().longitude && r.lat==marker.getPosition().latitude) {

                                            addgaragepark(r.key);
                                        }
                                }
                            });
                            dialog.show();
                        }

            }
        });
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());
               // GeoJsonLayer layer = new GeoJsonLayer(getMap(), R.raw.geoJsonFile, getApplicationContext());
                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        //String path = "android.resource://" + getPackageName() + "/" + R.raw.pkl;
        //loadKml();

    }

    private void addgaragepark(final String gkey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Garage Parking");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        // Set up the input
        final EditText input = new EditText(getApplicationContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Enter Parking Title");
        layout.addView(input);
        final EditText input2 = new EditText(getApplicationContext());
        input2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input2.setHint("Enter Dimen Title");
        layout.addView(input2);
        final EditText input3 = new EditText(getApplicationContext());
        input3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input3.setHint("Enter Type Title");
        layout.addView(input3);
        builder.setView(layout);

        builder.setPositiveButton("Add Parking", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String key = mDatabase.child("garage").child(gkey).child("parking").push().getKey();
                GarageParking post = new GarageParking(0,input.getText().toString(),input2.getText().toString(),gkey ,input3.getText().toString(),key,"");
                Log.d(TAG, "writeNewGaragePark: "+gkey);
                Map<String, Object> postValues = post.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/parkings/"+key, postValues);
                mDatabase.updateChildren(childUpdates);
                garageListAdapter=new GarageListAdapter(getApplicationContext(),parkinglist);
                garageListAdapter.notifyDataSetChanged();
                populatemarkers();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        //AlertDialog alertDialog = builder.create();
        builder.show();
    }

   /* public void loadKml() {

        try( InputStream inputstream = getApplicationContext().getResources().openRawResource(R.raw.pkl)) {

            // Set kmllayer to map
            // map is a GoogleMap, context is the Activity Context

            KmlLayer layer = new KmlLayer(mMap, inputstream, getApplicationContext());
            Log.d(TAG, "loadKml: done?");
            layer.addLayerToMap();



            // Handle these errors

        } catch (XmlPullParserException | IOException e) {

            e.printStackTrace();

        }

    }*/





    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }
  public void populatemarkers(){
      Log.d(TAG, "populatemarkers: inside pop markers"+mrecommendations.size());
      mMap.clear();
      int height = 115;
      int width = 115;
      BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.parking_icon);
      BitmapDrawable bitmapdraw1=(BitmapDrawable)getResources().getDrawable(R.drawable.bookedparkingicon);
      BitmapDrawable bitmapdraw2=(BitmapDrawable)getResources().getDrawable(R.drawable.bookedparkingicon);
      Bitmap b=bitmapdraw.getBitmap();
      Bitmap b1=bitmapdraw1.getBitmap();
      Bitmap b2=bitmapdraw2.getBitmap();
      Bitmap smallMarker2 = Bitmap.createScaledBitmap(b2, width, height, false);
      Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
      Bitmap smallMarker1 = Bitmap.createScaledBitmap(b1, width, height, false);

for(Garage g:mgarage){
    mMap.addMarker(new MarkerOptions().title(g.title).icon(BitmapDescriptorFactory.fromBitmap(smallMarker2)).position(new LatLng(g.lat,g.lang)).snippet("Garage:"+g.title+" Parking spots available:"+g.totalav)).setTag("Garage");
}
      for (Recommendation r:mrecommendations) {
          if (r.status==1)
              mMap.addMarker(new MarkerOptions().title(r.desc).icon(BitmapDescriptorFactory.fromBitmap(smallMarker1)).position(new LatLng(r.lat,r.lang)).snippet("Begin time is:"+r.btime+" and end time is:"+r.etime)).setTag("Recommendation");
          else
          mMap.addMarker(new MarkerOptions().title(r.desc).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).position(new LatLng(r.lat,r.lang)).snippet("Begin time is:"+r.btime+" and end time is:"+r.etime)).setTag("Recommendation");
      }
  }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
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
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(final Marker marker) {
        if(marker.getTag()=="Recommendation")
        {
        Log.d(TAG, "onMarkerDragEnd: This is it");
                //LayoutInflater li = LayoutInflater.from(getApplicationContext());
                //View promptsView = li.inflate(R.layout.add_rec_menu, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Parking Recommendation");

                LinearLayout layout = new LinearLayout(getApplicationContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                // Set up the input
                final EditText input = new EditText(getApplicationContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input.setHint("Enter Description");
                layout.addView(input);
                final EditText input2 = new EditText(getApplicationContext());
                input2.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME );
                input2.setHint("Enter Begin Time");
                input2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Calendar c = Calendar.getInstance();
                        final int[] mHour = {c.get(Calendar.HOUR_OF_DAY)};
                        final int[] mMinute = {c.get(Calendar.MINUTE)};

                        // Launch Time Picker Dialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(MapsActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {

                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                        mHour[0] = hourOfDay;
                                        mMinute[0] = minute;

                                        input2.setText(hourOfDay + ":" + minute);
                                    }
                                }, mHour[0], mMinute[0], false);
                        timePickerDialog.show();

                    }
                });
                layout.addView(input2);
                final EditText input3 = new EditText(getApplicationContext());
                input3.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                input3.setHint("Enter End Time");
                input3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                final int[] mHour = {c.get(Calendar.HOUR_OF_DAY)};
                final int[] mMinute = {c.get(Calendar.MINUTE)};

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(MapsActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                mHour[0] = hourOfDay;
                                mMinute[0] = minute;

                                input3.setText(hourOfDay + ":" + minute);
                            }
                        }, mHour[0], mMinute[0], false);
                timePickerDialog.show();

            }
        });
                layout.addView(input3);
                builder.setView(layout);

                builder.setPositiveButton("Add marker", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            writeNewrecommendation(input.getText().toString(),new SimpleDateFormat("HH:mm").parse(input2.getText().toString()),new SimpleDateFormat("HH:mm").parse(input3.getText().toString()),marker.getPosition().latitude,marker.getPosition().longitude);
                        populatemarkers();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                //AlertDialog alertDialog = builder.create();

                builder.show();
            }
            else if(marker.getTag()=="Garage"){
            Log.d(TAG, "onMarkerDragEnd: This is Garage Parking");
            //LayoutInflater li = LayoutInflater.from(getApplicationContext());
            //View promptsView = li.inflate(R.layout.add_rec_menu, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Garage Parking");

            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            // Set up the input
            final EditText input = new EditText(getApplicationContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setHint("Enter Garage Title");
            layout.addView(input);
            builder.setView(layout);

            builder.setPositiveButton("Add marker", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    writeNewgarage(input.getText().toString(),marker.getPosition().latitude,marker.getPosition().longitude);
                    populatemarkers();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            //AlertDialog alertDialog = builder.create();
            builder.show();
        }
    }


}