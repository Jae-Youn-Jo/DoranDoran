package com.doraesol.dorandoran.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.doraesol.dorandoran.ActivityResultEvent;
import com.doraesol.dorandoran.MainActivity;
import com.doraesol.dorandoran.R;
import com.doraesol.dorandoran.config.DataConfig;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */

public class MapMainFragment extends Fragment implements OnMapReadyCallback {
    final String LOG_TAG = MapMainFragment.class.getSimpleName();
    private GoogleMap m_googleMap;
    private MapView m_mapView;
    private Marker marker;
    private GoogleMap mMap;

    // Required empty public constructor
    public MapMainFragment() {}

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_map_main, container, false);
        m_mapView = (MapView) rootView.findViewById(R.id.map);
        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();
        m_mapView.getMapAsync(this);
        try
        {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.fg_search);
        ButterKnife.bind(this, rootView);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place1) {
                // TODO: Get info about the selected place.
                LatLng latLng = place1.getLatLng();
                CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);

                mMap.clear();
                marker = mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(LOG_TAG, "An error occurred: " + status);

            }

        });
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        mMap = map;

        LatLng cbnu = new LatLng(36.625123, 127.457177);
        mMap.addMarker(new MarkerOptions().position(cbnu).title("학연산"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cbnu, 18));

    }


    @Subscribe
    public void onActivityResult(ActivityResultEvent activityResultEvent){
        onActivityResult(activityResultEvent.getRequestCode(), activityResultEvent.getResultCode(), activityResultEvent.getData());
        String resultData = activityResultEvent.getData().getStringExtra("name");
        Log.d(LOG_TAG, "resultData : " + resultData);


        // 경로목록
        if(activityResultEvent.getRequestCode() == DataConfig.RESULT_MAP_LIST) {
        }
        // 즐겨찾기
        else if(activityResultEvent.getRequestCode() == DataConfig.RESULT_MAP_BOOKMARK) {
        }
        // 경로 추가
        else if(activityResultEvent.getRequestCode() == DataConfig.RESULT_MAP_INSERT){
            Toast.makeText(getContext(), resultData , Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "resultData : " + resultData);
        }
    }

}






