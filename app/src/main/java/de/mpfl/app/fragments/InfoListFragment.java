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

public class InfoListFragment extends Fragment {

    public final static String TAG = "InfoListFragment";

    private InfoListFragmentListener listener;
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
                listener.onInformationCategorySelected(position);
            }
        });

        return this.components.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof InfoListFragmentListener) {
            this.listener = (InfoListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement InfoListFragmentListener!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    public interface InfoListFragmentListener {
        void onInformationCategorySelected(int categoryIndex);
    }
}