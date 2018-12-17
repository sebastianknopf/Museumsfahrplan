package de.mpfl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import de.mpfl.app.R;
import de.mpfl.app.databinding.FragmentInfoListBinding;
import de.mpfl.app.listeners.OnFragmentInteractionListener;

public class InfoListFragment extends Fragment {

    public final static String TAG = "InfoListFragment";
    public final static String KEY_INFO_CATEGORY = "KEY_INFO_CATEGORY";

    private OnFragmentInteractionListener listener;
    private FragmentInfoListBinding components;

    public InfoListFragment() {
        // Required empty public constructor
    }

    public static InfoListFragment newInstance() {
        InfoListFragment fragment = new InfoListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_info_list, container, false);

        // set info categories into list items
        String[] infoCategoryArray = this.getActivity().getResources().getStringArray(R.array.str_array_information_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, infoCategoryArray);
        this.components.lstInfoCategory.setAdapter(adapter);

        // notify parent activity about the user's selection
        this.components.lstInfoCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle arguments = new Bundle();
                arguments.putInt(KEY_INFO_CATEGORY, position);

                listener.onFragmentInteraction(InfoListFragment.this, arguments);
            }
        });

        return this.components.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            this.listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }
}
