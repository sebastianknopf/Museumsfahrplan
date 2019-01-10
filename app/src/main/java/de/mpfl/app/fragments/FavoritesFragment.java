package de.mpfl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mpfl.app.R;
import de.mpfl.app.adapters.FavoritesListAdapter;
import de.mpfl.app.database.AppDatabase;
import de.mpfl.app.database.Favorite;
import de.mpfl.app.databinding.FragmentFavoritesBinding;
import de.mpfl.app.listeners.OnFavoriteItemClickListener;
import de.mpfl.app.listeners.OnFragmentInteractionListener;

public class FavoritesFragment extends Fragment implements OnFavoriteItemClickListener {

    public final static String TAG ="FavoritesFragment";

    public final static String KEY_FRAGMENT_ACTION = "KEY_FRAGMENT_ACTION";
    public final static String KEY_TRIP_ID = "KEY_TRIP_ID";
    public final static String KEY_TRIP_DATE = "KEY_TRIP_DATE";
    public final static String KEY_TRIP_TIME = "KEY_TRIP_TIME";

    public final static int ACTION_SELECT_TRIP = 0;

    private FragmentFavoritesBinding components;
    private OnFragmentInteractionListener fragmentInteractionListener;

    private FavoritesListAdapter favoritesListAdapter;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance() {
        FavoritesFragment fragment = new FavoritesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_favorites, container, false);

        // set activity title
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(R.string.str_favorites_title);
        }

        // get app database instance and display all favorites
        AppDatabase appDatabase = AppDatabase.getInstance(this.getContext());
        List<Favorite> favoriteList = appDatabase.getAllFavorites();

        if(favoriteList.size() == 0) {
            this.showErrorView();
        } else {
            this.setFavoritesAdapter(favoriteList);
        }

        return this.components.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.fragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onFavoriteItemClick(Favorite favoriteItem) {
        if(this.fragmentInteractionListener != null) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_SELECT_TRIP);
            arguments.putString(KEY_TRIP_ID, favoriteItem.getTripId());
            arguments.putString(KEY_TRIP_DATE, favoriteItem.getTripDate());
            arguments.putString(KEY_TRIP_TIME, favoriteItem.getTripTime());

            this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
        }
    }

    private void showErrorView() {
        this.components.rcvFavoritesList.setVisibility(View.GONE);
        this.components.layoutFavoritesEmpty.setVisibility(View.VISIBLE);
    }

    private void setFavoritesAdapter(List<Favorite> resultList) {
        this.favoritesListAdapter = new FavoritesListAdapter(getContext(), resultList);
        favoritesListAdapter.setOnFavoriteItemClickListener(FavoritesFragment.this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.components.rcvFavoritesList.setLayoutManager(layoutManager);

        // divider setup
        DividerItemDecoration itemDecor = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        this.components.rcvFavoritesList.addItemDecoration(itemDecor);

        this.components.rcvFavoritesList.setAdapter(favoritesListAdapter);
    }

}
