package de.mpfl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.mpfl.app.R;
import de.mpfl.app.databinding.FragmentInfoDetailsBinding;


public class InfoDetailsFragment extends Fragment {

    public static final String TAG = "InfoDetailsFragment";
    public static final String KEY_INFO_VIEW = "KEY_INFO_VIEW";

    private String infoViewName;

    private FragmentInfoDetailsBinding components;

    public InfoDetailsFragment() {
        // Required empty public constructor
    }

    public static InfoDetailsFragment newInstance(String infoViewName) {
        Bundle args = new Bundle();
        args.putString(KEY_INFO_VIEW, infoViewName);

        InfoDetailsFragment fragment = new InfoDetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.infoViewName = this.getArguments().getString(KEY_INFO_VIEW);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_info_details, container, false);

        // read info html file from assets and display it in textview
        this.components.wvInfoDetailsContent.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        this.components.wvInfoDetailsContent.loadUrl("file:///android_asset/" + this.infoViewName + ".html");

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
