package de.mpfl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import de.mpfl.app.R;
import de.mpfl.app.database.AppDatabase;
import de.mpfl.app.database.Favorite;
import de.mpfl.app.databinding.FragmentFavoritesBinding;

public class FavoritesFragment extends Fragment {

    public final static String TAG ="FavoritesFragment";

    private FragmentFavoritesBinding components;

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

        // todo: correct implementation following after test
        Toast.makeText(this.getContext(), String.valueOf(favoriteList.size()), Toast.LENGTH_SHORT).show();

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
