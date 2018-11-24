package de.mpfl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.mpfl.app.R;
import de.mpfl.app.databinding.FragmentInfoDetailsBinding;
import de.mpfl.app.utils.AssetReader;


public class InfoDetailsFragment extends Fragment {

    public static final String TAG = "InfoDetailsFragment";
    public static final String ARG_INFO_FILENAME = "InfoFileName";

    private String infoFileName;

    private FragmentInfoDetailsBinding components;

    public InfoDetailsFragment() {
        // Required empty public constructor
    }

    public static InfoDetailsFragment newInstance(String infoFileName) {
        InfoDetailsFragment fragment = new InfoDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INFO_FILENAME, infoFileName);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.infoFileName = this.getArguments().getString(ARG_INFO_FILENAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_info_details, container, false);

        // read info html file from assets and display it in textview
        AssetReader assetReader = new AssetReader(this.getContext(), this.infoFileName);
        this.components.tvInfoDetailsText.setText(Html.fromHtml(assetReader.getContent()));

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
