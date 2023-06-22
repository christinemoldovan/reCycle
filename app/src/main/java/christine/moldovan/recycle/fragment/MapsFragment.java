package christine.moldovan.recycle.fragment;

import static android.view.View.GONE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import christine.moldovan.recycle.MapMarker;
import christine.moldovan.recycle.MarkerOptionsComparator;
import christine.moldovan.recycle.MyPairAdapter;
import christine.moldovan.recycle.R;
import christine.moldovan.recycle.RecycleTypes;
import christine.moldovan.recycle.UserFavMarkers;
import christine.moldovan.recycle.databinding.FragmentMapsBinding;
import christine.moldovan.recycle.loginscreen.User;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    FragmentMapsBinding binding;
    SupportMapFragment mapFragment;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReferenceMarkers, markersReference, usersReference, usersFavoritesReference, userSuggestedMarkersReference;
    LatLng currentLatLng;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker marker;
    private MarkerOptions markerOptions;
    private String m_addressLines, m_postalCode;
    private PopupWindow currentPopupWindow = null;
    private PopupWindow infoPopupWindow = null;
    private PopupWindow locationListPopupWindow = null;
    private PopupWindow userSuggestedLocationListPopupWindow = null;
    private Spinner recycleSpinner;
    private LatLng lastClickedPosition;
    private String email;
    private List<MarkerOptions> allMarkers = new ArrayList<MarkerOptions>(); //handle when adding new markers
    private List<MarkerOptions> allUserSuggestedMarkers = new ArrayList<MarkerOptions>(); //handle when adding new markers
    private Marker userSuggestedMarker = null;
    private boolean isAdmin = false;

    @Override
    public void onResume() {
        super.onResume();
        updateRecycleSpinner();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceMarkers = FirebaseDatabase.getInstance().getReference();
        markersReference = firebaseDatabase.getReference().child("mapMarkers");
        usersReference = firebaseDatabase.getReference().child("users");
        userSuggestedMarkersReference = firebaseDatabase.getReference().child("userSuggestedMarkers");
        usersFavoritesReference = firebaseDatabase.getReference().child("userFavoritesMarkers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideLayoutSearch();
        hideMarkerPopup();
        hideInfoPopup();
        hideLocationListPopup();
        hideUserSuggestedLocationListPopup();
        removeUserSuggestedMarker();
        lastClickedPosition = null;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        hideLayoutSearch();
        hideMarkerPopup();
        hideInfoPopup();
        hideLocationListPopup();
        hideUserSuggestedLocationListPopup();
        removeUserSuggestedMarker();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        lastClickedPosition = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        Bundle bundle = this.getArguments();
        email = bundle.getString("email");
        System.out.println(email);
        checkIfAdmin();

        //spinner
        recycleSpinner = binding.recycleTypesSpinner;
        RecycleTypes[] materials = RecycleTypes.values();
        String[] materialStrings = new String[materials.length];
        for (int i = 0; i < materials.length; i++) {
            materialStrings[i] = materials[i].toString();
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, materialStrings);
        recycleSpinner.setAdapter(spinnerAdapter);
        recycleSpinner.setSelection(0); //select first from dropdown

        mapFragment.getMapAsync(this);

        mapInitialize();

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLayoutSearch();
                hideMarkerPopup();
                hideInfoPopup();
                hideLocationListPopup();
                hideUserSuggestedLocationListPopup();
                removeUserSuggestedMarker();
                String latValue = binding.Latitude.getText().toString();
                String lngValue = binding.Longitude.getText().toString();
                if (isAdmin) {
                    if (!latValue.isEmpty() && !lngValue.isEmpty()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("latitude", latValue);
                        bundle.putString("longitude", lngValue);
                        bundle.putString("address", m_addressLines);
                        bundle.putString("postalCode", m_postalCode);
                        bundle.putString("email", email);
                        Fragment fragment = new AddLatLngFragment();
                        fragment.setArguments(bundle);
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, fragment).addToBackStack("add").setReorderingAllowed(true).commit();
                    } else {
                        Toast.makeText(getActivity(), "Invalid location points!",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (!latValue.isEmpty() && !lngValue.isEmpty()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("latitude", latValue);
                        bundle.putString("longitude", lngValue);
                        bundle.putString("address", m_addressLines);
                        bundle.putString("postalCode", m_postalCode);
                        bundle.putString("email", email);
                        Fragment fragment = new SuggestAddLocationFragment();
                        fragment.setArguments(bundle);
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, fragment).addToBackStack("suggest").setReorderingAllowed(true).commit();
                    } else {
                        Toast.makeText(getActivity(), "Invalid location points!",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        binding.suggestAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable() != 0) {
                    hideMarkerPopup();
                    hideInfoPopup();
                    hideLocationListPopup();
                    hideUserSuggestedLocationListPopup();
                    removeUserSuggestedMarker();
                    if (binding.LayoutSearch.getVisibility() == View.VISIBLE) {
                        binding.LayoutSearch.setVisibility(View.GONE);
                        return;
                    }
                    binding.LayoutSearch.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        databaseReferenceMarkers = FirebaseDatabase.getInstance().getReference("mapMarkers");

        return binding.getRoot();
    }

    private void mapInitialize() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(3000)
                .build();

        binding.searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    goToSearchLocation(binding.searchEdit.getText().toString());
                }
                return false;
            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    private void goToSearchLocation(String locationAddress) {
        String searchLocation = locationAddress;

        Geocoder geocoder = new Geocoder(getContext());
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchLocation, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            String location = address.getAdminArea();
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();
            m_addressLines = address.getAddressLine(0);
            m_postalCode = address.getPostalCode();
            goToLatLng(latitude, longitude, 17f);
            binding.Description.setText("Drag the marker to your desired location on the map.");
            if (marker != null) {
                marker.remove();
            }
            markerOptions = new MarkerOptions();
            markerOptions.title(location);
            markerOptions.draggable(true);
            markerOptions.icon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpointred));
            markerOptions.position(new LatLng(latitude, longitude));
            marker = mMap.addMarker(markerOptions);
            marker.setTag("user" + email);
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDrag(@NonNull Marker marker) {
                    binding.Latitude.setText(Double.toString(marker.getPosition().latitude));
                    binding.Longitude.setText(Double.toString(marker.getPosition().longitude));
                    List<Address> newAddressList = new ArrayList<>();
                    try {
                        newAddressList = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (newAddressList.size() > 0) {
                        Address newAddress = newAddressList.get(0);
                        m_addressLines = newAddress.getAddressLine(0);
                        m_postalCode = newAddress.getPostalCode();
                    }
                    binding.searchEdit.setText(m_addressLines);
                }

                @Override
                public void onMarkerDragEnd(@NonNull Marker marker) {
                    binding.nextButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onMarkerDragStart(@NonNull Marker marker) {
                    Toast.makeText(getActivity(), "Drag marker to desired location", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "Invalid location address", Toast.LENGTH_SHORT).show();
        }

    }

    private void goToLatLng(double latitude, double longitude, float zoomLevel) {
        LatLng latLng = new LatLng(latitude, longitude);
        binding.Latitude.setText(String.valueOf(latLng.latitude));
        binding.Longitude.setText(String.valueOf(latLng.longitude));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);
        mMap.animateCamera(update);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Dexter.withContext(getContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                binding.suggestAddLocation.setVisibility(View.VISIBLE);
                binding.listRecycleLocations.setVisibility(View.VISIBLE);
                binding.goToCurrentLocationButton.setVisibility(View.VISIBLE);
                if (isAdmin) {
                    binding.userSuggestedMarkerList.setVisibility(View.VISIBLE);
                }
                mMap.setMyLocationEnabled(true); //puts icon corner right up
                mMap.getUiSettings().setMyLocationButtonEnabled(false); //hides current location icon corner right up

                // get user current location
                fusedLocationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error getting last location", Toast.LENGTH_SHORT).show();
                        Log.v("USER_CURRENT_LOCATION", e.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (lastClickedPosition != null) {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastClickedPosition, 17f);
                            mMap.animateCamera(cameraUpdate, 2000, null);
                        } else {
                            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            lastClickedPosition = currentLatLng;
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f);
                            // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                            mMap.animateCamera(cameraUpdate, 2000, null);
                        }

                    }
                });

                binding.goToCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f);
                        // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                        mMap.animateCamera(cameraUpdate, 2000, null);

                    }
                });

                userSuggestedMarkersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allUserSuggestedMarkers.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MapMarker mapMarker = dataSnapshot.getValue(MapMarker.class);
                            LatLng latLng = new LatLng(mapMarker.getLatitude(), mapMarker.getLongitude());
                            String recyclingPoint = mapMarker.getRecyclePoint();
                            String[] recyclingSplitPoints = recyclingPoint.split("(?=[A-Z])");
                            String finalRecyclingPoints = String.join(" ", recyclingSplitPoints);
                            markerOptions = new MarkerOptions();
                            markerOptions.position(latLng).icon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpoint));
                            markerOptions.title(mapMarker.getPrimaryAddress());
                            markerOptions.snippet(finalRecyclingPoints);
                            allUserSuggestedMarkers.add(markerOptions);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Toast.makeText(getContext(), "Permission for location was denied. Please review application permissions.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

        // search bar disappears/appears on long click if admin
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                if (!isAdmin) {
                    checkIfAdmin();
                    if (isAdmin) {
                        refreshLayoutSearch();
                    }
                } else {
                    refreshLayoutSearch();
                }
            }
        });

        //change markers based on spinner
        updateRecycleSpinner();

        // handle pressing the markers
        if (mMap != null) {
            //list of user suggested marker locations
            binding.userSuggestedMarkerList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideLayoutSearch();
                    hideMarkerPopup();
                    hideInfoPopup();
                    hideLocationListPopup();
                    hideUserSuggestedLocationListPopup();
                    removeUserSuggestedMarker();

                    View userSuggestedLocationsView = getLayoutInflater().inflate(R.layout.fragment_list_locations, null);
                    userSuggestedLocationListPopupWindow = new PopupWindow(userSuggestedLocationsView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    ListView userSuggestedLocationsListView = (ListView) userSuggestedLocationsView.findViewById(R.id.locationListView);
                    userSuggestedLocationListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            userSuggestedLocationListPopupWindow = null;
                        }
                    });

                    Collections.sort(allUserSuggestedMarkers, new MarkerOptionsComparator(currentLatLng));
                    Location userLocation = new Location("");
                    userLocation.setLatitude(currentLatLng.latitude);
                    userLocation.setLongitude(currentLatLng.longitude);
                    List<Pair<String, String>> markerPairs = new ArrayList<>();
                    for (MarkerOptions markerOptions : allUserSuggestedMarkers) {
                        Location markerLocation = new Location("");
                        markerLocation.setLatitude(markerOptions.getPosition().latitude);
                        markerLocation.setLongitude(markerOptions.getPosition().longitude);
                        float distance = userLocation.distanceTo(markerLocation) / 1000f;
                        String distanceString = String.format("%.2f km\n", distance);
                        Pair<String, String> pair = new Pair<>(markerOptions.getTitle(), distanceString + markerOptions.getSnippet());
                        markerPairs.add(pair);
                    }

                    if (markerPairs.isEmpty()) {
                        Toast.makeText(getActivity(), "Empty list of user suggested markers", Toast.LENGTH_LONG).show();
                    } else {
                        MyPairAdapter adapter = new MyPairAdapter(getContext(), R.layout.list_item_layout, markerPairs);
                        userSuggestedLocationsListView.setAdapter(adapter);

                        userSuggestedLocationsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                // get the clicked Pair object
                                Pair<String, String> clickedPair = markerPairs.get(position);
                                System.out.println("enters");
                                System.out.println(clickedPair.first + clickedPair.second);
                                // get the corresponding MarkerOptions object from allMarkers
                                for (MarkerOptions markerOptions : allUserSuggestedMarkers) {
                                    if (markerOptions.getTitle().equals(clickedPair.first)) {
                                        // move the camera to the marker's position
                                        LatLng userSuggestedMarkerPosition = markerOptions.getPosition();
                                        goToLatLng(userSuggestedMarkerPosition.latitude, userSuggestedMarkerPosition.longitude, 17f);
                                        hideUserSuggestedLocationListPopup();
                                        double latitude = userSuggestedMarkerPosition.latitude;
                                        double longitude = userSuggestedMarkerPosition.longitude;
                                        //ask if to add or not
                                        Query query = userSuggestedMarkersReference.orderByChild("latitude").equalTo(latitude);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot child : snapshot.getChildren()) {
                                                        MapMarker existingMarker = child.getValue(MapMarker.class);
                                                        if (existingMarker.getLongitude() == longitude) {
                                                            String key = child.getKey();
                                                            Bundle bundle = new Bundle();
                                                            bundle.putString("latitude", Double.toString(existingMarker.getLatitude()));
                                                            bundle.putString("longitude", Double.toString(existingMarker.getLongitude()));
                                                            bundle.putString("description", existingMarker.getDescriptionAddress());
                                                            bundle.putString("address", existingMarker.getPrimaryAddress());
                                                            bundle.putString("secondaryAddress", existingMarker.getSecondaryAddress());
                                                            bundle.putString("titleAddress", existingMarker.getTitleAddress());
                                                            bundle.putString("recyclingPoints", existingMarker.getRecyclePoint());
                                                            bundle.putString("email", email);
                                                            lastClickedPosition = userSuggestedMarkerPosition;
                                                            userSuggestedMarker = null;
                                                            Fragment fragment = new AddLatLngFragment();
                                                            fragment.setArguments(bundle);
                                                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                            transaction.replace(R.id.container, fragment).addToBackStack("add").setReorderingAllowed(true).commit();
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                        break;
                                    }
                                }
                                return false;
                            }
                        });

                        userSuggestedLocationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // get the clicked Pair object
                                Pair<String, String> clickedPair = markerPairs.get(position);
                                System.out.println(clickedPair.first + clickedPair.second);
                                // get the corresponding MarkerOptions object from allMarkers
                                for (MarkerOptions markerOptions : allUserSuggestedMarkers) {
                                    if (markerOptions.getTitle().equals(clickedPair.first)) {
                                        // move the camera to the marker's position
                                        LatLng userSuggestedMarkerPosition = markerOptions.getPosition();
                                        //todo: see how to display marker icon and make it draggable
                                        markerOptions.position(userSuggestedMarkerPosition).icon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpointred));
                                        marker = mMap.addMarker(markerOptions);
                                        marker.setTag("user" + email);
                                        userSuggestedMarker = marker;

                                        goToLatLng(userSuggestedMarkerPosition.latitude, userSuggestedMarkerPosition.longitude, 17f);
                                        hideUserSuggestedLocationListPopup();

                                    }
                                }
                            }

                        });
                        // show the popup window
                        userSuggestedLocationListPopupWindow.showAtLocation(userSuggestedLocationsView, Gravity.CENTER, 0, 0);
                    }
                }
            });
            //list of all locations based on most nearby
            binding.listRecycleLocations.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideLayoutSearch();
                    hideMarkerPopup();
                    hideInfoPopup();
                    hideLocationListPopup();
                    hideUserSuggestedLocationListPopup();
                    removeUserSuggestedMarker();
                    View locationsView = getLayoutInflater().inflate(R.layout.fragment_list_locations, null);
                    locationListPopupWindow = new PopupWindow(locationsView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    ListView locationsListView = (ListView) locationsView.findViewById(R.id.locationListView);
                    locationListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            locationListPopupWindow = null;
                        }
                    });

                    Collections.sort(allMarkers, new MarkerOptionsComparator(currentLatLng));
                    Location userLocation = new Location("");
                    userLocation.setLatitude(currentLatLng.latitude);
                    userLocation.setLongitude(currentLatLng.longitude);
                    List<Pair<String, String>> markerPairs = new ArrayList<>();
                    for (MarkerOptions markerOptions : allMarkers) {
                        Location markerLocation = new Location("");
                        markerLocation.setLatitude(markerOptions.getPosition().latitude);
                        markerLocation.setLongitude(markerOptions.getPosition().longitude);
                        float distance = userLocation.distanceTo(markerLocation) / 1000f;
                        String distanceString = String.format("%.2f km\n", distance);
                        Pair<String, String> pair = new Pair<>(markerOptions.getTitle(), distanceString + markerOptions.getSnippet());
                        markerPairs.add(pair);
                    }

                    if (markerPairs.isEmpty()) {
                        Toast.makeText(getActivity(), "There are no markers available!", Toast.LENGTH_LONG).show();
                    } else {
                        MyPairAdapter adapter = new MyPairAdapter(getContext(), R.layout.list_item_layout, markerPairs);
                        locationsListView.setAdapter(adapter);

                        locationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // get the clicked Pair object
                                Pair<String, String> clickedPair = markerPairs.get(position);
                                System.out.println(clickedPair.first + clickedPair.second);
                                // get the corresponding MarkerOptions object from allMarkers
                                for (MarkerOptions markerOptions : allMarkers) {
                                    if (markerOptions.getTitle().equals(clickedPair.first)) {
                                        // move the camera to the marker's position
                                        LatLng markerPosition = markerOptions.getPosition();
                                        goToLatLng(markerPosition.latitude, markerPosition.longitude, 17f);
                                        hideLocationListPopup();
                                        break;
                                    }
                                }
                            }
                        });

                        // show the popup window
                        locationListPopupWindow.showAtLocation(locationsView, Gravity.CENTER, 0, 0);
                    }
                }
            });

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    hideMarkerPopup();
                    hideInfoPopup();
                    hideLocationListPopup();
                    hideUserSuggestedLocationListPopup();
                }
            });

            mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    hideMarkerPopup();
                    hideInfoPopup();
                    hideLocationListPopup();
                    hideUserSuggestedLocationListPopup();
                }
            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    if (marker.getTag() != null && marker.getTag().equals("user" + email)) {
                        return true; // Ignore click on new marker from user suggested markers
                    }
                    hideLayoutSearch();
                    removeUserSuggestedMarker();
                    hideMarkerPopup();
                    hideInfoPopup();
                    hideLocationListPopup();
                    hideUserSuggestedLocationListPopup();
                    // Get the marker's snippet
                    String recyclePointsContent = marker.getSnippet();

                    // Inflate the marker_popup.xml layout
                    View popupView = getLayoutInflater().inflate(R.layout.activity_popup, null);
                    // Set the title and snippet in the popup layout
                    ImageView paperIcon = (ImageView) popupView.findViewById(R.id.paperIcon);
                    ImageView plasticIcon = (ImageView) popupView.findViewById(R.id.plasticIcon);
                    ImageView glassIcon = (ImageView) popupView.findViewById(R.id.glassIcon);
                    ImageView metalIcon = (ImageView) popupView.findViewById(R.id.metalIcon);
                    ImageView batteryIcon = (ImageView) popupView.findViewById(R.id.batteryIcon);
                    ImageView electronicsIcon = (ImageView) popupView.findViewById(R.id.eWasteIcon);
                    LinearLayout iconsLayout = (LinearLayout) popupView.findViewById(R.id.iconsLayout);
                    LinearLayout adminsPanelLayout = (LinearLayout) popupView.findViewById(R.id.adminsCommandsLayout);

                    String[] substrings = recyclePointsContent.split(" ");
                    int countVisibleIcons = 0;
                    //set icon visibility:
                    for (String substring : substrings) {
                        if (substring.equals("Plastic")) {
                            plasticIcon.setVisibility(View.VISIBLE);
                            countVisibleIcons++;
                        } else if (substring.equals("Paper")) {
                            paperIcon.setVisibility(View.VISIBLE);
                            countVisibleIcons++;
                        } else if (substring.equals("Glass")) {
                            glassIcon.setVisibility(View.VISIBLE);
                            countVisibleIcons++;
                        } else if (substring.equals("Metal")) {
                            metalIcon.setVisibility(View.VISIBLE);
                            countVisibleIcons++;
                        } else if (substring.equals("Batteries")) {
                            batteryIcon.setVisibility(View.VISIBLE);
                            countVisibleIcons++;
                        } else if (substring.equals("Electronics")) {
                            electronicsIcon.setVisibility(View.VISIBLE);
                            countVisibleIcons++;
                        }
                    }
                    if (countVisibleIcons > 3) {
                        iconsLayout.setOrientation(LinearLayout.VERTICAL);
                    } else {
                        iconsLayout.setOrientation(LinearLayout.HORIZONTAL);
                    }

                    double latitude = marker.getPosition().latitude;
                    double longitude = marker.getPosition().longitude;
                    LatLng lastClickedMarkerPosition = new LatLng(latitude, longitude);

                    ///ADMIN PANELS ACTIONS
                    if (!isAdmin) {
                        checkIfAdmin();
                        if (isAdmin) {
                            adminsPanelLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        adminsPanelLayout.setVisibility(View.VISIBLE);
                    }
                    ImageView deleteLocation = (ImageView) popupView.findViewById(R.id.deleteLocation);
                    deleteLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Query query = markersReference.orderByChild("latitude").equalTo(latitude); //??
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot child : snapshot.getChildren()) {
                                            MapMarker existingMarker = child.getValue(MapMarker.class);
                                            if (existingMarker.getLongitude() == longitude) {
                                                final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                                View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                                                final TextView title = (TextView) mView.findViewById(R.id.titleDialog);
                                                final TextView description = (TextView) mView.findViewById(R.id.descriptionDialog);
                                                final EditText textViewEdit = (EditText) mView.findViewById(R.id.dialog_textEdit); //it is not needed
                                                textViewEdit.setVisibility(GONE);
                                                title.setText("Delete location");
                                                description.setText("Are you sure you want to delete the selected location?");

                                                Button cancelButton = (Button) mView.findViewById(R.id.cancelDialog_button);
                                                Button okayButton = (Button) mView.findViewById(R.id.okayDialog_button);
                                                alert.setView(mView);
                                                final AlertDialog alertDialog = alert.create();
                                                alertDialog.setCanceledOnTouchOutside(false);
                                                cancelButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        alertDialog.dismiss();
                                                    }
                                                });
                                                okayButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        hideMarkerPopup();
                                                        child.getRef().removeValue();
                                                        marker.remove();
                                                        for (MarkerOptions options : allMarkers) {//new test if its ok!!!!
                                                            if (options.getPosition().equals(marker.getPosition())) {
                                                                allMarkers.remove(options);
                                                                break;
                                                            }
                                                        }
                                                        alertDialog.dismiss();
                                                        Toast.makeText(getActivity(), "Location deleted successfully!",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                                alertDialog.show();
                                            }
                                            //

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    ImageView editLocation = (ImageView) popupView.findViewById(R.id.editLocation);
                    editLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideMarkerPopup();
                            Query query = markersReference.orderByChild("latitude").equalTo(latitude);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot child : snapshot.getChildren()) {
                                            MapMarker existingMarker = child.getValue(MapMarker.class);
                                            if (existingMarker.getLongitude() == longitude) {
                                                String key = child.getKey();
                                                lastClickedPosition = lastClickedMarkerPosition;

                                                Bundle bundle = new Bundle();
                                                bundle.putDouble("latitude", existingMarker.getLatitude());
                                                bundle.putDouble("longitude", existingMarker.getLongitude());
                                                bundle.putString("description", existingMarker.getDescriptionAddress());
                                                bundle.putString("primaryAddress", existingMarker.getPrimaryAddress());
                                                bundle.putString("secondaryAddress", existingMarker.getSecondaryAddress());
                                                bundle.putString("titleAddress", existingMarker.getTitleAddress());
                                                bundle.putString("recyclingPoints", existingMarker.getRecyclePoint());
                                                bundle.putDouble("currentLat", currentLatLng.latitude);
                                                bundle.putDouble("currentLng", currentLatLng.longitude);
                                                Fragment fragment = new LocationEditFragment();
                                                fragment.setArguments(bundle);
                                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                transaction.replace(R.id.container, fragment).addToBackStack("edit").setReorderingAllowed(true).commit();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    //ALL USERS ACTIONS
                    ImageView infoLocation = (ImageView) popupView.findViewById(R.id.infoLocation);
                    infoLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideMarkerPopup();

                            Query query = markersReference.orderByChild("latitude").equalTo(latitude);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot child : snapshot.getChildren()) {
                                            MapMarker existingMarker = child.getValue(MapMarker.class);
                                            if (existingMarker.getLongitude() == longitude) {
                                                lastClickedPosition = lastClickedMarkerPosition;
                                                View infoView = getLayoutInflater().inflate(R.layout.fragment_location_info, null);
                                                infoPopupWindow = new PopupWindow(infoView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                                                infoPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss() {
                                                        // Do something when the popup window is dismissed
                                                        infoPopupWindow = null;
                                                        Query query = usersFavoritesReference.orderByChild("latitude").equalTo(latitude);
                                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()) {
                                                                    for (DataSnapshot child : snapshot.getChildren()) {
                                                                        MapMarker existingMarker = child.getValue(MapMarker.class);
                                                                        if (existingMarker.getLongitude() == longitude) {
                                                                            // The marker exists
                                                                            // Retrieve the marker data and do something with it
                                                                            String key = child.getKey();
                                                                            marker.setIcon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpointfavorite));                                                                            // etc.
                                                                        }
                                                                    }
                                                                } else {
                                                                    // The marker does not exist
                                                                    marker.setIcon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpointseen));
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                // Handle the error
                                                            }
                                                        });

                                                    }
                                                });

                                                TextView primaryAddress = (TextView) infoView.findViewById(R.id.primaryAddressDetails);
                                                primaryAddress.setText(existingMarker.getPrimaryAddress());

                                                TextView title = (TextView) infoView.findViewById(R.id.addressTitleDetails);
                                                title.setText(existingMarker.getTitleAddress());

                                                TextView recyclePoints = (TextView) infoView.findViewById(R.id.recyclingPointsDetails);
                                                String recyclingPoints = existingMarker.getRecyclePoint();
                                                String[] recyclingSplitPoints = recyclingPoints.split("(?=[A-Z])");
                                                String finalRecyclingPoints = String.join(" ", recyclingSplitPoints);
                                                recyclePoints.setText(finalRecyclingPoints);

                                                TextView secondaryAddress = (TextView) infoView.findViewById(R.id.secondaryAddressDetails);
                                                TextView secondaryAddressHeadline = (TextView) infoView.findViewById(R.id.secondaryAddressHeadline);
                                                if (!existingMarker.getSecondaryAddress().isEmpty()) {
                                                    secondaryAddress.setText(existingMarker.getSecondaryAddress());
                                                } else {
                                                    secondaryAddress.setVisibility(GONE);
                                                    secondaryAddressHeadline.setVisibility(GONE);
                                                }

                                                TextView description = (TextView) infoView.findViewById(R.id.descriptionDetails);
                                                TextView descriptionHeadline = (TextView) infoView.findViewById(R.id.descriptionHeadline);
                                                if (!existingMarker.getDescriptionAddress().isEmpty()) {
                                                    description.setText(existingMarker.getDescriptionAddress());
                                                } else {
                                                    description.setVisibility(GONE);
                                                    descriptionHeadline.setVisibility(GONE);
                                                }

                                                Button goToLocationButton = (Button) infoView.findViewById(R.id.goToLocationButton);
                                                goToLocationButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        // Launch the new activity to show the route
                                                        String uri = "http://maps.google.com/maps?saddr=" + currentLatLng.latitude + "," + currentLatLng.longitude + "&daddr=" + existingMarker.getLatitude() + "," + existingMarker.getLongitude();
                                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                                        startActivity(intent);
                                                    }
                                                });

                                                ImageView favoriteButton = (ImageView) infoView.findViewById(R.id.favoriteButton);
                                                ImageView unfavoriteButton = (ImageView) infoView.findViewById(R.id.unfavoriteButton);

                                                usersFavoritesReference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot userFavoriteMarker : snapshot.getChildren()) {
                                                            if (userFavoriteMarker.getKey().equals(marker.getTag())) {
                                                                favoriteButton.setVisibility(GONE);
                                                                unfavoriteButton.setVisibility(View.VISIBLE);
                                                                unfavoriteButton.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        Toast.makeText(getActivity(), "Unfavorite!", Toast.LENGTH_SHORT).show();
                                                                        userFavoriteMarker.getRef().removeValue();
                                                                        unfavoriteButton.setVisibility(GONE);
                                                                        favoriteButton.setVisibility(View.VISIBLE);
                                                                    }
                                                                });
                                                                return;
                                                            }
                                                        }
                                                        unfavoriteButton.setVisibility(GONE);
                                                        favoriteButton.setVisibility(View.VISIBLE);
                                                        favoriteButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                UserFavMarkers userFavMarkers = new UserFavMarkers();
                                                                userFavMarkers.setLatitude(existingMarker.getLatitude());
                                                                userFavMarkers.setLongitude(existingMarker.getLongitude());
                                                                userFavMarkers.setUserUID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                                System.out.println(marker.getTag());
                                                                usersFavoritesReference.child(marker.getTag().toString()).setValue(userFavMarkers).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText(getActivity(), "Favorite!", Toast.LENGTH_SHORT).show();
                                                                        marker.setIcon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpointfavorite));
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(getActivity(), "Error!",
                                                                                Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                            }
                                                        });

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                                //distance:
                                                TextView distanceValue = (TextView) infoView.findViewById(R.id.distanceValue);
                                                TextView distanceHeadline = (TextView) infoView.findViewById(R.id.distanceHeadline);
                                                Location userLocation = new Location("");
                                                userLocation.setLatitude(currentLatLng.latitude);
                                                userLocation.setLongitude(currentLatLng.longitude);
                                                Location markerLocation = new Location("");
                                                markerLocation.setLatitude(marker.getPosition().latitude);
                                                markerLocation.setLongitude(marker.getPosition().longitude);
                                                float distance = userLocation.distanceTo(markerLocation) / 1000f;
                                                distanceValue.setText(String.format("%.2f km", distance));
                                                infoPopupWindow.showAtLocation(infoView, Gravity.CENTER, 0, 0);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    Button goToLocationButton = popupView.findViewById(R.id.goToLocation_button);
                    goToLocationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Launch the new activity to show the route
                            lastClickedPosition = lastClickedMarkerPosition;
                            String uri = "http://maps.google.com/maps?saddr=" + currentLatLng.latitude + "," + currentLatLng.longitude + "&daddr=" + marker.getPosition().latitude + "," + marker.getPosition().longitude;
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            startActivity(intent);
                        }
                    });

                    // Create a new popup window
                    currentPopupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                    // Set a dismiss listener for the popup window
                    currentPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            // Do something when the popup window is dismissed
                            marker.setIcon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpointseen));
                        }
                    });

                    // Move the camera to focus on the marker
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17f);

                    mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onFinish() {
                            // Get the screen position of the marker
                            Projection projection = mMap.getProjection();
                            Point markerScreenPosition = projection.toScreenLocation(marker.getPosition());

                            // Offset the popup window to show it above the marker
                            int popupWidth = currentPopupWindow.getWidth();
                            int popupHeight = currentPopupWindow.getHeight();
                            System.out.println(popupWidth);
                            System.out.println(popupHeight);
                            int popupOffsetX = popupWidth - 40; // 200
                            int popupOffsetY = popupHeight + 300; // add some padding->650
                            marker.setIcon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpointred));
                            currentPopupWindow.showAtLocation(binding.getRoot(), Gravity.NO_GRAVITY, markerScreenPosition.x - popupOffsetX, markerScreenPosition.y - popupOffsetY);
                        }
                    });
                    return true;
                }
            });
        }
    }


    /**
     * Creates the new marker icon that shows up on the map
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void hideMarkerPopup() {
        if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
            currentPopupWindow.dismiss();
            currentPopupWindow = null;
        }
    }

    private void hideInfoPopup() {
        if (infoPopupWindow != null && infoPopupWindow.isShowing()) {
            infoPopupWindow.dismiss();
            infoPopupWindow = null;
        }
    }

    private void hideLocationListPopup() {
        if (locationListPopupWindow != null && locationListPopupWindow.isShowing()) {
            locationListPopupWindow.dismiss();
            locationListPopupWindow = null;
        }
    }

    private void hideUserSuggestedLocationListPopup() {
        if (userSuggestedLocationListPopupWindow != null && userSuggestedLocationListPopupWindow.isShowing()) {
            userSuggestedLocationListPopupWindow.dismiss();
            userSuggestedLocationListPopupWindow = null;
        }
    }

    private void removeUserSuggestedMarker() {
        if (userSuggestedMarker != null) {
            userSuggestedMarker.remove();
            userSuggestedMarker = null;
        }
    }

    private void hideLayoutSearch() {
        int LayoutSearch = binding.LayoutSearch.getVisibility();
        if (LayoutSearch == View.VISIBLE) {
            binding.LayoutSearch.setVisibility(GONE);
        }
    }

    private void refreshLayoutSearch() {
        int LayoutSearch = binding.LayoutSearch.getVisibility();
        if (LayoutSearch == View.VISIBLE) {
            binding.LayoutSearch.setVisibility(GONE);
        } else if (LayoutSearch == GONE) {
            hideMarkerPopup();
            hideInfoPopup();
            hideLocationListPopup();
            hideUserSuggestedLocationListPopup();
            removeUserSuggestedMarker();
            binding.LayoutSearch.setVisibility(View.VISIBLE);
        }
    }

    private void updateRecycleSpinner() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            recycleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedValue = parent.getItemAtPosition(position).toString();
                    System.out.println("INTRA");
                    hideLayoutSearch();
                    hideMarkerPopup();
                    hideInfoPopup();
                    hideLocationListPopup();
                    hideUserSuggestedLocationListPopup();
                    removeUserSuggestedMarker();
                    List<Marker> markerList = new ArrayList<>();
                    allMarkers.clear();
                    databaseReferenceMarkers.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            mMap.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                MapMarker mapMarker = dataSnapshot.getValue(MapMarker.class);
                                LatLng latLng = new LatLng(mapMarker.getLatitude(), mapMarker.getLongitude());
                                String recyclingPoint = mapMarker.getRecyclePoint();
                                //here check if string recyclingPoint has value 'selectedValue'
                                if (recyclingPoint.contains(selectedValue) || selectedValue.equals("All")) {
                                    String[] recyclingSplitPoints = recyclingPoint.split("(?=[A-Z])");
                                    String finalRecyclingPoints = String.join(" ", recyclingSplitPoints);
                                    markerOptions = new MarkerOptions();
                                    markerOptions.position(latLng).icon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpoint));
                                    markerOptions.title(mapMarker.getPrimaryAddress());
                                    markerOptions.snippet(finalRecyclingPoints);
                                    marker = mMap.addMarker(markerOptions);
                                    allMarkers.add(markerOptions);
                                    marker.setTag(dataSnapshot.getKey());
                                    markerList.add(marker);
                                    System.out.println("adauga");
                                    System.out.println(marker);
                                }
                            }

                            usersFavoritesReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot userFavoriteMarker : snapshot.getChildren()) {
                                        if (markerList != null) {
                                            for (Marker markerOnMap : markerList) {
                                                if (userFavoriteMarker.getKey().equals(markerOnMap.getTag())) { // Check if the favorite matches the marker
                                                    markerOnMap.setIcon(bitmapDescriptorFromVector(getActivity(), R.drawable.recyclingpointfavorite)); // Change the icon of the marker to indicate that it is a favorite
                                                }
                                            }
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

    }

    private int isNetworkAvailable() {
        int result = 0; // Returns connection type. 0: none; 1: mobile data; 2: wifi
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = 3;
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = 2;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = 1;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                        result = 3;
                    }
                }
            }
        }
        return result;
    }

    private void checkIfAdmin() {
        Query query = usersReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    System.out.println("Exists");
                    for (DataSnapshot child : snapshot.getChildren()) {
                        System.out.println("enters");
                        User existingUser = child.getValue(User.class);
                        if (existingUser.getIsAdmin() == true) {
                            isAdmin = true;
                            System.out.println("is admin =" + isAdmin);
                            //binding.suggestAddLocation.setVisibility(View.GONE); //TODO uncomment
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}