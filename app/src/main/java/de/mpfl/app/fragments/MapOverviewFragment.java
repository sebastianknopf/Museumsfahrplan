package de.mpfl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.mpfl.app.R;
import de.mpfl.app.databinding.FragmentMapOverviewBinding;


public class MapOverviewFragment extends Fragment {

    public final static String TAG ="MapOverviewFragment";

    private FragmentMapOverviewBinding components;

    public MapOverviewFragment() {
        // Required empty public constructor
    }

    public static MapOverviewFragment newInstance() {
        MapOverviewFragment fragment = new MapOverviewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_map_overview, container, false);
        return this.components.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
