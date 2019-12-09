package com.example.paquet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import android.support.annotation.NonNull;
import android.widget.Toast;

//Route Related
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import com.mapbox.mapboxsdk.style.layers.Property;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;

import com.google.firebase.auth.FirebaseAuth;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class Main2Activity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    private AutoCompleteTextView pickup_autocomplete, drop_autocomplete;

    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "pickup_marker";

    private Point origin, destination;
    private Style style;

    //For route
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    private DirectionsRoute currentRoute;
    private MapboxDirections client;


    //Order Related
    private static final String TO_BE_PICKED_UP= "To Be Picked Up!";
    private ScrollView order_init;
    private EditText order_quantity, order_title, order_description;
    private Spinner payment_spinner, cate_spinner;
    private ConstraintLayout progressCL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoicmVka2F5IiwiYSI6ImNqN2lpb2xjeTF0MTgzMm5wamY2NXJ2emcifQ.yHlWsKDNfEd-7vKTysjjqQ");
        setContentView(R.layout.activity_main2);
        drawerLayout = findViewById(R.id.drawer_layout);
        order_init = (ScrollView) findViewById(R.id.order_init);
        order_quantity = (EditText) findViewById(R.id.quantity);
        order_title = (EditText) findViewById(R.id.order_title);
        order_description = (EditText) findViewById(R.id.order_desc);
        payment_spinner = (Spinner) findViewById(R.id.paymentmethods_spinner);
        cate_spinner = (Spinner) findViewById(R.id.categories_spinner);
        progressCL =(ConstraintLayout)findViewById(R.id.progressbar_cl);
        progressCL.setVisibility(View.INVISIBLE);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Main2Activity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.payment_methods) );
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payment_spinner.setAdapter(arrayAdapter);

        arrayAdapter = new ArrayAdapter<String>(Main2Activity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.order_categories));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cate_spinner.setAdapter(arrayAdapter);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        InitNavBar();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        InitMapView();

        InitPickUpAC();
        InitDropAC();

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    public void AddOrder(View view){
//        if (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
//        }
//        Intent i = new Intent(Main2Activity.this, Paytm.class);
//        startActivity(i);
        Intent i2 = new Intent(Main2Activity.this, Main3Activity.class);
        startActivity(i2);

    }

    private void InitNavBar(){
        //        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar actionbar = getSupportActionBar();
//        actionbar.setDisplayHomeAsUpEnabled(true);
//        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        NavigationView navigationView = findViewById(R.id.nav_view);
        final View headerView = navigationView.getHeaderView(0);
        TextView header_text = headerView.findViewById(R.id.header_text);
        header_text.setText(firebaseAuth.getCurrentUser().getDisplayName());
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
//                        //menuItem.setChecked(true);

                        int id = menuItem.getItemId();
                        switch(id)
                        {
                            case R.id.nav_help:
                                Intent i = new Intent(Main2Activity.this, Help.class);
                                startActivity(i);
                                break;
                            case R.id.nav_your_order:
                                i = new Intent(Main2Activity.this, YourOrders.class);
                                startActivity(i);
                                break;

                            case R.id.nav_settings:
                                i = new Intent(Main2Activity.this, Setting.class);
                                startActivity(i);
                                break;

                            default:
                                return true;
                        }
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu m = navView.getMenu();
        for (int i=0;i<m.size();i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu!=null && subMenu.size() >0 ) {
                for (int j=0; j <subMenu.size();j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
    }

    private void InitMapView(){
        //Mapbox shit
//        mapView.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(@NonNull MapboxMap mapboxMap) {
//                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
//                    @Override
//                    public void onStyleLoaded(@NonNull Style style) {
//                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
//                        // Add the marker image to map
//                        style.addImage("marker-icon-id",
//                                BitmapFactory.decodeResource(
//                                        Main2Activity.this.getResources(), R.drawable.mapbox_marker_icon_default));
//
//                        GeoJsonSource geoJsonSource = new GeoJsonSource("source-id", Feature.fromGeometry(
//                                Point.fromLngLat(72.837506, 19.109941)));
//                        style.addSource(geoJsonSource);
//
//                        SymbolLayer symbolLayer = new SymbolLayer("layer-id", "source-id");
//                        symbolLayer.withProperties(
//                                PropertyFactory.iconImage("marker-icon-id")
//                        );
//                        style.addLayer(symbolLayer);
//                    }
//                });
//            }
//        });
        mapView.getMapAsync(this);
    }

    private void InitPickUpAC(){
        final GeocoderAdapter adapter = new GeocoderAdapter(this);
        pickup_autocomplete = (AutoCompleteTextView) findViewById(R.id.pickup_auto_comp);
        pickup_autocomplete.setLines(1);
        pickup_autocomplete.setAdapter(adapter);
        pickup_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CarmenFeature result = adapter.getItem(position);
                pickup_autocomplete.setText(result.text());
                //updateMap(result.prope, result.getLongitude());
                //Toast.makeText(Main2Activity.this, "result: " + result.center().toString(), Toast.LENGTH_LONG).show();
                IconFactory iconFactory = IconFactory.getInstance(Main2Activity.this);
                Icon icon = iconFactory.fromResource(R.drawable.pickup_marker);
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(result.center().latitude(), result.center().longitude()))
                        .icon(icon)).setTitle("Pick-up!");
                origin = result.center();
                drop_autocomplete.setVisibility(View.VISIBLE);
            }
        });
    }

    private void InitDropAC(){
        final GeocoderAdapter adapter = new GeocoderAdapter(this);
        drop_autocomplete = (AutoCompleteTextView) findViewById(R.id.drop_auto_comp);
        drop_autocomplete.setLines(1);
        drop_autocomplete.setAdapter(adapter);
        drop_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CarmenFeature result = adapter.getItem(position);
                drop_autocomplete.setText(result.text());
                //updateMap(result.prope, result.getLongitude());
                //Toast.makeText(Main2Activity.this, "result: " + result.center().toString(), Toast.LENGTH_LONG).show();
                IconFactory iconFactory = IconFactory.getInstance(Main2Activity.this);
                Icon icon = iconFactory.fromResource(R.drawable.pickup_marker);
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(result.center().latitude(), result.center().longitude()))
                        .icon(icon)).setTitle("Drop!");
                destination = result.center();
                //InitRoute(origin, destination);
                initSource(style);

                initLayers(style);
                Log.d("Main2Activity", "blehhhhhhhh");
// Get the directions route from the Mapbox Directions API
                //Toast.makeText(Main2Activity.this, "Drop do8888787ne!", Toast.LENGTH_LONG).show();
                getRoute(style, origin, destination);
                hideKeyboard(Main2Activity.this);
                order_init.setVisibility(View.VISIBLE);
                pickup_autocomplete.setVisibility(View.GONE);
                drop_autocomplete.setVisibility(View.GONE);

            }
        });
    }

    public void ConfirmOrder(View view){
        progressCL.setVisibility(View.VISIBLE);
        CollectionReference root = db.collection("AppRoot");

        //TODO:get user id and put in place of orders
        DocumentReference dr = root.document("User");

        CollectionReference userIDCollection = dr.collection(Objects.requireNonNull(firebaseAuth.getUid()));
        dr = userIDCollection.document("Orders");
        Map<String, Object> order = new HashMap<>();
        order.put("name", order_title.getText().toString());
        order.put("desc", order_description.getText().toString());
        order.put("quantity", order_quantity.getText().toString());
        order.put("pickup_lat", origin.latitude());
        order.put("pickup_lon", origin.longitude());
        order.put("drop_lat", destination.latitude());
        order.put("drop_lon", destination.longitude());
        order.put("status", TO_BE_PICKED_UP);
        order.put("driver", "None");
        order.put("price", "10");
        order.put("payment_method", payment_spinner.getSelectedItem().toString());
        order.put("category", cate_spinner.getSelectedItem().toString());

        dr.collection("AllOrders").add(order)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(Main2Activity.this,"Order Confirmed" + documentReference.getId(), Toast.LENGTH_LONG).show();
                        progressCL.setVisibility(View.INVISIBLE);
                        //tv.setText(documentReference.getId());
                        AddToTBPO(documentReference.getId());
                        order_init.setVisibility(View.GONE);
                        pickup_autocomplete.setVisibility(View.VISIBLE);
                        Intent i = new Intent(Main2Activity.this, Paytm.class);
                        startActivity(i);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressCL.setVisibility(View.INVISIBLE);
                Toast.makeText(Main2Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void AddToTBPO(String order_id){
        float order_quan = Float.parseFloat(order_quantity.getText().toString());
        //String order_name = "Order" + order_quan, order_desc = "Orderdesc" + order_quan;
        CollectionReference root = db.collection("AppRoot");

        //TODO:get user id and put in place of orders
        DocumentReference dr = root.document("ToBePickedupOrders");

        CollectionReference tbpuCollection = dr.collection("AllOrders");
        Map<String, Object> order = new HashMap<>();
//        order.put("name", order_name);
//        order.put("desc", order_desc);
        order.put("quantity", String.valueOf(order_quan));
        order.put("pickup_lat", origin.latitude());
        order.put("pickup_lon", origin.longitude());
        order.put("drop_lat", destination.latitude());
        order.put("drop_lon", destination.longitude());
        order.put("status", TO_BE_PICKED_UP);
        //order.put("driver", "None");
        order.put("price", "10");
        //order.put("payment_method", "Paytm");
        order.put("user", Objects.requireNonNull(firebaseAuth.getUid()));
        order.put("order_id", order_id);

        tbpuCollection.add(order)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //Toast.makeText(Main2Activity.this,"Order Confirmed" + documentReference.getId(), Toast.LENGTH_LONG).show();
                        //tv.setText(documentReference.getId());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Main2Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void InitRoute(Point origin, Point destination){
        NavigationRoute.builder(getApplicationContext())
                .accessToken("pk.eyJ1IjoicmVka2F5IiwiYSI6ImNqN2lpb2xjeTF0MTgzMm5wamY2NXJ2emcifQ.yHlWsKDNfEd-7vKTysjjqQ")
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });
    }

    /**
     * Add the route and marker sources to the map
     */
    private void initSource(@NonNull Style loadedMapStyle) {
        if(loadedMapStyle.getSource(ROUTE_SOURCE_ID) == null){
            loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID,
                    FeatureCollection.fromFeatures(new Feature[] {})));
        }


        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[] {
                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
        if(loadedMapStyle.getSource(ROUTE_SOURCE_ID) == null)
            loadedMapStyle.addSource(iconGeoJsonSource);
    }

    /**
     * Add the route and maker icon layers to the map
     */
    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(getResources().getColor(R.color.colorPrimary))
        );
        if(loadedMapStyle.getLayer(ROUTE_LAYER_ID) == null)
            loadedMapStyle.addLayer(routeLayer);

// Add the red marker icon image to the map
        if(loadedMapStyle.getImage(MARKER_IMAGE) == null){
            loadedMapStyle.addImage(MARKER_IMAGE, BitmapUtils.getBitmapFromDrawable(
                    getResources().getDrawable(R.drawable.pickup_marker)));
        }

// Add the red marker icon SymbolLayer to the map
        if(loadedMapStyle.getLayer(ICON_LAYER_ID) == null){
            loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                    iconImage(RED_PIN_ICON_ID),
                    iconIgnorePlacement(true),
                    iconIgnorePlacement(true),
                    iconOffset(new Float[] {0f, -4f})));
        }
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     *
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
     */
    private void getRoute(@NonNull final Style style, Point origin, Point destination) {

        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                System.out.println(call.request().url().toString());

// You can get the generic HTTP info about the response
                Timber.d("Response code: " + response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    return;
                }

// Get the directions route
                currentRoute = response.body().routes().get(0);

// Make a toast which displays the route's distance
                Toast.makeText(Main2Activity.this, currentRoute.distance().toString(), Toast.LENGTH_SHORT).show();

                if (style.isFullyLoaded()) {
// Retrieve and update the source designated for showing the directions route
                    GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

// Create a LineString with the directions route's geometry and
// reset the GeoJSON source for the route LineLayer source
                    if (source != null) {
                        Timber.d("onResponse: source != null");
                        source.setGeoJson(FeatureCollection.fromFeature(
                                Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6))));
                    }
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: " + throwable.getMessage());
                Toast.makeText(Main2Activity.this, "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void HelpWebView(){
        WebView myWebView = new WebView(this);
        setContentView(myWebView);
        myWebView.loadUrl("https://paquetsupport.000webhostapp.com/");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void openDrawer(View view){
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = ResourcesCompat.getFont(getApplicationContext(), R.font.opensans_light);
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("" , font), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        Main2Activity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.DARK,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style _style) {
                        enableLocationComponent(_style);
                        style = _style;
//                        style.addImage(MARKER_IMAGE, BitmapFactory.decodeResource(
//                                Main2Activity.this.getResources(), R.drawable.pickup_marker));
//                        addMarkers(style);
                    }
                });
    }

    private void addMarkers(@NonNull Style loadedMapStyle) {
        List<Feature> features = new ArrayList<>();
        features.add(Feature.fromGeometry(Point.fromLngLat(72.8365, 19.1157)));

        /* Source: A data source specifies the geographic coordinate where the image marker gets placed. */

        loadedMapStyle.addSource(new GeoJsonSource(MARKER_SOURCE, FeatureCollection.fromFeatures(features)));

        /* Style layer: A style layer ties together the source and image and specifies how they are displayed on the map. */
        loadedMapStyle.addLayer(new SymbolLayer(MARKER_STYLE_LAYER, MARKER_SOURCE)
                .withProperties(
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconImage(MARKER_IMAGE),
// Adjust the second number of the Float array based on the height of your marker image.
// This is because the bottom of the marker should be anchored to the coordinate point, rather
// than the middle of the marker being the anchor point on the map.
                        PropertyFactory.iconOffset(new Float[] {0f, -52f})
                ));
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(this, loadedMapStyle);

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);

                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }


}

