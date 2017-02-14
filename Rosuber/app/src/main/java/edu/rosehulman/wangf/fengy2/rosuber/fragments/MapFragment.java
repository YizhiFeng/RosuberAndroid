package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import edu.rosehulman.wangf.fengy2.rosuber.Constants;
import edu.rosehulman.wangf.fengy2.rosuber.MainActivity;
import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.Trip;

public class MapFragment extends Fragment {

    private static final int RC_PERMISSIONS = 5;
    MapView mMapView;
    private GoogleMap googleMap;
    private boolean isSettingOrigin;
    private boolean isEdit;
    private String currentUserKey;
    private Trip trip;

    public MapFragment(){
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSettingOrigin = getArguments().getBoolean(Constants.ISSETTINGORIGIN);
        currentUserKey = getArguments().getString(Constants.USER);
        trip = getArguments().getParcelable(Constants.TRIP);
        isEdit = getArguments().getBoolean(Constants.EDIT);
        if(trip==null){
            trip = new Trip();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_map_search).setVisible(true);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_map_ok).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_map_search){
            searchAddress();
            return true;
        }else if(item.getItemId() ==R.id.action_map_ok){
            switchToInsertTripFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchAddress() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_get_place_name_title);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_map_search, null, false);
        final EditText editText = (EditText) view.findViewById(R.id.address_editText);

        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String placeName = editText.getText().toString();
                goToPlace(placeName, 17.0f);
            }
        });

        builder.create().show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
//
//                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                        || ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                    googleMap.setMyLocationEnabled(true);
//                } else {
//                    Toast.makeText(getActivity(), "No location permission", Toast.LENGTH_SHORT).show();
//                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSIONS);
//                }


                // For dropping a marker at a point on the Map
                LatLng terreHaute = new LatLng(39.4696, -87.3898);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(terreHaute, 5.0f));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(terreHaute).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return rootView;
    }

    private void goToPlace(String locationName, final float zoomLevel) {

        Geocoder geocoder = new Geocoder(getContext());
        if(geocoder.isPresent()){
            try {
                List<Address> addresses = geocoder.getFromLocationName(locationName,2);
                final List<Address> copy = addresses;
                if(addresses.size()==0){
                    Toast.makeText(getContext(), "Place not found",Toast.LENGTH_LONG).show();
                    return;
                }

                final String[] adds = new String[addresses.size()];
                int i = 0;
                for(Address add:addresses){

                    adds[i] = add.getAddressLine(0) +", "+add.getLocality();
                    i++;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select One");
                builder.setSingleChoiceItems(adds, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Address address = copy.get(which);
                        LatLng location = new LatLng(address.getLatitude(),address.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
                        if(isSettingOrigin){
                            trip.setOrigin(adds[which]);
                        }else{
                            trip.setDestination(adds[which]);
                        }

                    }
                });

                builder.setPositiveButton(android.R.string.ok, null);
                builder.create().show();

            } catch (IOException e) {
                Toast.makeText(getContext(), "Network connection to geocoder failed",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }catch (IllegalArgumentException e){
                Toast.makeText(getContext(), "No placed entered",Toast.LENGTH_LONG).show();

            }
        }

    }

    private void switchToInsertTripFragment() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        InsertTripFragment fragment = new InsertTripFragment();
        Bundle args = new Bundle();
        args.putString(Constants.USER,currentUserKey);
        args.putBoolean(Constants.EDIT,isEdit);
        args.putParcelable(Constants.TRIP,trip);
        fragment.setArguments(args);

        Slide slideTransition = new Slide(Gravity.RIGHT);
        slideTransition.setDuration(200);
        fragment.setEnterTransition(slideTransition);

        ft.replace(R.id.fragment_container, fragment);
        ft.addToBackStack("insert trip");
        ft.commit();
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
}
