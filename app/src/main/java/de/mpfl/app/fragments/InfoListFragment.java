package de.mpfl.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.mpfl.app.BuildConfig;
import de.mpfl.app.R;
import de.mpfl.app.adapters.IconListAdapter;
import de.mpfl.app.databinding.FragmentInfoListBinding;
import de.mpfl.app.listeners.OnFragmentInteractionListener;

public class InfoListFragment extends Fragment {

    public final static String TAG = "InfoListFragment";
    public final static String KEY_INFO_VIEW = "KEY_INFO_VIEW";

    public final static String KEY_FRAGMENT_ACTION = "KEY_FRAGMENT_ACTION";

    public final static int ACTION_SHOW_DETAILS = 0;
    public final static int ACTION_START_AUTH = 1;

    private OnFragmentInteractionListener listener;
    private FragmentInfoListBinding components;

    private int infoTapCount = 0;

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

        // set acitivty title
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(R.string.str_info_list_title);
        }

        // get icon list items base height for adapting
        int baseHeight = (int) this.getResources().getDimension(R.dimen.icon_item_height);

        // populate app info card
        List<IconListAdapter.IconListItem> infoAppList = new ArrayList<IconListAdapter.IconListItem>();
        infoAppList.add(new IconListAdapter.IconListItem(this.getString(R.string.str_info_list_app_version), BuildConfig.VERSION_NAME, R.drawable.ic_version));
        infoAppList.add(new IconListAdapter.IconListItem(this.getString(R.string.str_info_list_app_github_project), null, R.drawable.ic_code));

        IconListAdapter infoAppAdapter = new IconListAdapter(this.getContext(), infoAppList);
        this.components.lstInfoApp.setAdapter(infoAppAdapter);

        this.adaptListViewHeight(this.components.lstInfoApp, baseHeight, infoAppList.size());

        // notify parent activity about the user's selection
        this.components.lstInfoApp.setOnItemClickListener((parent, view, position, id) -> {
            if(position == 0) {
                if(this.infoTapCount == 0) {
                    this.infoTapCount++;
                    Handler handler = new Handler();
                    handler.postDelayed(() -> this.infoTapCount = 0, 20000);
                } else if(this.infoTapCount == 4) {
                    this.infoTapCount = 0;

                    if(this.listener != null) {
                        Bundle arguments = new Bundle();
                        arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_START_AUTH);

                        this.listener.onFragmentInteraction(this, arguments);
                    }
                } else {
                    this.infoTapCount++;
                }
            } else if(position == 1) {
                // okay stop! this is the link to github project of this app
                Intent githubWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sebastianknopf/Museumsfahrplan"));
                this.getActivity().startActivity(githubWebIntent);
            }
        });

        // populate contributors info card
        List<IconListAdapter.IconListItem> infoContributorsList = new ArrayList<IconListAdapter.IconListItem>();

        String[] contributorNames = this.getResources().getStringArray(R.array.str_info_list_contributor_names);
        String[] contributorInfos = this.getResources().getStringArray(R.array.str_info_list_contributor_infos);
        if(contributorNames.length == contributorInfos.length) {
            for(int i = 0; i < contributorNames.length; i++) {
                IconListAdapter.IconListItem contributorItem = new IconListAdapter.IconListItem(contributorNames[i], contributorInfos[i], R.drawable.ic_person);
                infoContributorsList.add(contributorItem);
            }
        }

        IconListAdapter infoContributorsAdapter = new IconListAdapter(this.getContext(), infoContributorsList);
        this.components.lstInfoContributors.setAdapter(infoContributorsAdapter);

        this.adaptListViewHeight(this.components.lstInfoContributors, baseHeight, infoContributorsList.size());

        // notify parent activity about the user's selection
        this.components.lstInfoContributors.setOnItemClickListener((parent, view, position, id) -> {

        });

        // populate legal info card
        List<IconListAdapter.IconListItem> infoLegalList = new ArrayList<>();
        infoLegalList.add(new IconListAdapter.IconListItem(this.getString(R.string.str_info_list_legal_privacy), null, R.drawable.ic_privacy));
        infoLegalList.add(new IconListAdapter.IconListItem(this.getString(R.string.str_info_list_legal_licenses), null, R.drawable.ic_license));

        IconListAdapter infoLegalAdapter = new IconListAdapter(this.getContext(), infoLegalList);
        this.components.lstInfoLegal.setAdapter(infoLegalAdapter);

        this.adaptListViewHeight(this.components.lstInfoLegal, baseHeight, infoLegalList.size());

        // notify parent activity about the user's selection
        this.components.lstInfoLegal.setOnItemClickListener((parent, view, position, id) -> {
            if(position == 0) {
                // privacy view
                Bundle arguments = new Bundle();
                arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_SHOW_DETAILS);
                arguments.putString(KEY_INFO_VIEW, "html_info_privacy");

                if(this.listener != null) {
                    listener.onFragmentInteraction(this, arguments);
                }
            } else if(position == 1) {
                // licenses view
                Bundle arguments = new Bundle();
                arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_SHOW_DETAILS);
                arguments.putString(KEY_INFO_VIEW, "html_info_opensource");

                if(this.listener != null) {
                    listener.onFragmentInteraction(this, arguments);
                }
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

    private void adaptListViewHeight(ListView listView, int baseHeight, int itemsCount) {
        if(listView != null) {
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = baseHeight * itemsCount;
            listView.setLayoutParams(params);
        }
    }
}
