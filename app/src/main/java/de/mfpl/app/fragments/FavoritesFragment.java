package de.mfpl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mfpl.app.R;
import de.mfpl.app.adapters.FavoritesListAdapter;
import de.mfpl.app.database.AppDatabase;
import de.mfpl.app.database.Favorite;
import de.mfpl.app.databinding.FragmentFavoritesBinding;
import de.mfpl.app.listeners.OnFavoriteItemClickListener;
import de.mfpl.app.listeners.OnFragmentInteractionListener;

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
    private int recentlyRemovedIndex = 0;
    private Favorite recentlyRemovedFavorite = null;

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
            this.showEmptyView();
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

    private void showEmptyView() {
        this.components.rcvFavoritesList.setVisibility(View.GONE);
        this.components.layoutFavoritesEmpty.setVisibility(View.VISIBLE);
    }

    private void showFavoritesList() {
        this.components.rcvFavoritesList.setVisibility(View.VISIBLE);
        this.components.layoutFavoritesEmpty.setVisibility(View.GONE);
    }

    private void showUndoSnackbar() {
        View view = this.components.getRoot();
        Snackbar snackbar = Snackbar.make(view, R.string.str_favorites_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.str_undo, v -> undoDeleteFavorite());
        snackbar.show();
    }

    private void undoDeleteFavorite() {
        if(this.recentlyRemovedFavorite != null) {
            // re-animate the favorite item :-)
            AppDatabase appDatabase = AppDatabase.getInstance(this.getContext());
            appDatabase.addFavorite(this.recentlyRemovedFavorite);

            // display in adapter again
            if(this.favoritesListAdapter != null) {
                this.favoritesListAdapter.insertItem(this.recentlyRemovedIndex, this.recentlyRemovedFavorite);
            }

            // we restored one element, so it cant be empty... show list in every case
            this.showFavoritesList();
        }
    }

    private void setFavoritesAdapter(List<Favorite> resultList) {
        this.favoritesListAdapter = new FavoritesListAdapter(getContext(), resultList);
        this.favoritesListAdapter.setOnFavoriteItemClickListener(FavoritesFragment.this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.components.rcvFavoritesList.setLayoutManager(layoutManager);

        // divider setup
        DividerItemDecoration itemDecor = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        this.components.rcvFavoritesList.addItemDecoration(itemDecor);

        // swipe to delete functionality
        /*FavoritesSwipeDeleteCallback favoritesSwipeDeleteCallback = new FavoritesSwipeDeleteCallback(this.getContext(), this.favoritesListAdapter);*/

        Drawable deleteDrawable = this.getContext().getDrawable(R.drawable.ic_delete);
        deleteDrawable.setTint(ContextCompat.getColor(this.getContext(), android.R.color.white));
        ColorDrawable deleteBackground = new ColorDrawable(Color.RED);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                recentlyRemovedIndex = viewHolder.getAdapterPosition();

                // remove favorite item from database
                recentlyRemovedFavorite = favoritesListAdapter.getFavoritesList().get(recentlyRemovedIndex);
                if(recentlyRemovedFavorite != null) {
                    AppDatabase appDatabase = AppDatabase.getInstance(getContext());
                    appDatabase.deleteFavorite(recentlyRemovedFavorite);
                }

                // remove item from adapter
                favoritesListAdapter.removeItem(recentlyRemovedIndex);

                // display undo snackbar
                showUndoSnackbar();

                // display empty screen if there's no favorite left
                if(favoritesListAdapter.getFavoritesList().size() == 0) {
                    showEmptyView();
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 0;

                int iconMargin = (itemView.getHeight() - deleteDrawable.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - deleteDrawable.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + deleteDrawable.getIntrinsicHeight();

                if (dX > 0) { // Swiping to the right
                    int iconLeft = itemView.getLeft() + iconMargin + deleteDrawable.getIntrinsicWidth();
                    int iconRight = itemView.getLeft() + iconMargin;
                    deleteDrawable.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    deleteBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                } else if (dX < 0) { // Swiping to the left
                    int iconLeft = itemView.getRight() - iconMargin - deleteDrawable.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    deleteDrawable.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    deleteBackground.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else { // view is unSwiped
                    deleteBackground.setBounds(0, 0, 0, 0);
                }

                try {
                    deleteBackground.draw(c);
                    deleteDrawable.draw(c);
                } catch(IllegalArgumentException e) {
                }
            }
        };

        // attach swipe helper to recycler view
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(this.components.rcvFavoritesList);

        this.components.rcvFavoritesList.setAdapter(favoritesListAdapter);
    }

}
