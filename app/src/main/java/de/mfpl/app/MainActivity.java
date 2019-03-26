package de.mfpl.app;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.mfpl.app.databinding.ActivityMainBinding;
import de.mfpl.app.fragments.FavoritesFragment;
import de.mfpl.app.fragments.InfoDetailsFragment;
import de.mfpl.app.fragments.InfoListFragment;
import de.mfpl.app.fragments.MapOverviewFragment;
import de.mfpl.app.fragments.SearchCalendarFragment;
import de.mfpl.app.fragments.SearchDetailsFragment;
import de.mfpl.app.fragments.SearchInputFragment;
import de.mfpl.app.fragments.SettingsFragment;
import de.mfpl.app.fragments.TripDetailsFragment;
import de.mfpl.app.listeners.OnFragmentInteractionListener;
import de.mfpl.app.utils.NavigationManager;
import de.mfpl.app.utils.SettingsManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    private ActivityMainBinding components;
    private NavigationManager navigationManager;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set theme before doing any other thing
        this.setTheme(R.style.AppTheme);

        // initialize activity content
        super.onCreate(savedInstanceState);
        this.components = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // setup navigation manager
        this.navigationManager = new NavigationManager(this, this.components.contentView.getId(), R.drawable.ic_menu_toggle);

        // setup settings manager
        this.settingsManager = new SettingsManager(this);

        // set toolbar as actionbar
        this.setSupportActionBar(this.components.mainToolbar);

        // set event for each menu item to be selected
        this.components.navigationView.setNavigationItemSelectedListener(this);

        // select the first item by default
        this.components.navigationView.setCheckedItem(R.id.navigationMenuMap);
        this.onNavigationItemSelected(this.components.navigationView.getMenu().findItem(R.id.navigationMenuMap));

        // display agency menu if there's a secondary authentification
        this.displayAgencyMenu();
    }

    // make navigation manager accessible for further purposes
    public NavigationManager getNavigationManager() {
        return this.navigationManager;
    }

    // make settings manager accessible for other components
    public SettingsManager getSettingsManager() {
        return this.settingsManager;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                // open navigation drawer when there is not fragment history currently
                // otherwise go back to the upper fragment
                if(this.navigationManager.getBackStackCount() > 0) {
                    this.onBackPressed();
                } else {
                    this.components.navigationDrawer.openDrawer(Gravity.START);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment targetFragment = null;
        switch(item.getItemId()) {
            case R.id.navigationMenuMap:
                targetFragment = MapOverviewFragment.newInstance();
                break;

            case R.id.navigationMenuSearch:
                targetFragment = SearchInputFragment.newInstance(this.settingsManager.getLastMapPosition().getLatitude(), this.settingsManager.getLastMapPosition().getLongitude());
                break;

            case R.id.navigationMenuFavorites:
                targetFragment = FavoritesFragment.newInstance();
                break;

            case R.id.navigationMenuSettings:
                targetFragment = SettingsFragment.newInstance();
                break;

            case R.id.navigationMenuInformation:
                targetFragment = InfoListFragment.newInstance();
                break;

            case R.id.navigationMenuReset:
                this.resetSecondaryAuthentification();
                break;
        }

        if(targetFragment != null) {
            // navigate to target fragment without keeping the back stack
            this.navigationManager.navigateTo(targetFragment, false);

            // set checked item
            this.components.navigationView.setCheckedItem(item.getItemId());
        }

        //  and hide drawer
        this.components.navigationDrawer.closeDrawers();

        return false;
    }

    @Override
    public void onBackPressed() {
        // close drawer if it's open - thats it
        if(this.components.navigationDrawer.isDrawerOpen(Gravity.START)) {
            this.components.navigationDrawer.closeDrawers();
            return;
        }

        // go back to the last fragment if there's one
        if(navigationManager.getBackStackCount() > 0) {
            this.navigationManager.navigateBack();
            return;
        }

        // or do the default behaviour
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null && result.getContents() != null) {
            String[] scanResults = result.getContents().split(";");
            if(scanResults.length > 1) {
                this.settingsManager.setApiCredentials(scanResults[0], scanResults[1]);
                this.displayAgencyMenu();

                Toast.makeText(this, R.string.str_secauth_authentification_successful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.str_secauth_authentification_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onFragmentInteraction(Fragment fragment, Bundle arguments) {
        if(fragment instanceof  InfoListFragment) {
            if(arguments.getInt(InfoListFragment.KEY_FRAGMENT_ACTION) == InfoListFragment.ACTION_SHOW_DETAILS) {
                InfoDetailsFragment infoDetailsFragment = InfoDetailsFragment.newInstance(arguments.getString(InfoListFragment.KEY_INFO_VIEW));
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                this.navigationManager.navigateTo(infoDetailsFragment);
            } else if(arguments.getInt(InfoListFragment.KEY_FRAGMENT_ACTION) == InfoListFragment.ACTION_START_AUTH) {
                IntentIntegrator intent = new IntentIntegrator(this);
                intent.setCaptureActivity(ScanActivity.class);
                intent.setPrompt("");
                intent.setBeepEnabled(false);
                intent.setOrientationLocked(false);
                intent.initiateScan();
            }

        } else if(fragment instanceof MapOverviewFragment) {
            if(arguments.getInt(MapOverviewFragment.KEY_FRAGMENT_ACTION) == MapOverviewFragment.ACTION_OPEN_SEARCH) {
                double searchLat = arguments.getDouble(MapOverviewFragment.KEY_SEARCH_LAT);
                double searchLon = arguments.getDouble(MapOverviewFragment.KEY_SEARCH_LON);

                SearchInputFragment searchInputFragment = SearchInputFragment.newInstance(searchLat, searchLon);
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_up, R.anim.fragment_exit_up);
                this.navigationManager.navigateTo(searchInputFragment);
            } else if(arguments.getInt(MapOverviewFragment.KEY_FRAGMENT_ACTION) == MapOverviewFragment.ACTION_SELECT_TRIP) {
                String tripId = arguments.getString(MapOverviewFragment.KEY_TRIP_ID);
                String tripDate = arguments.getString(MapOverviewFragment.KEY_TRIP_DATE);
                String tripTime = arguments.getString(MapOverviewFragment.KEY_TRIP_TIME);

                TripDetailsFragment tripDetailsFragment = TripDetailsFragment.newInstance(tripId, tripDate, tripTime);
                this.navigationManager.navigateTo(tripDetailsFragment);
            }
        } else if(fragment instanceof SearchInputFragment) {
            if(arguments.getInt(SearchInputFragment.KEY_FRAGMENT_ACTION) == SearchInputFragment.ACTION_SELECT_ROUTE) {
                String routeId = arguments.getString(SearchInputFragment.KEY_SEARCH_ROUTE_ID);
                String routeName = arguments.getString(SearchInputFragment.KEY_SEARCH_ROUTE_NAME);
                String searchDate = arguments.getString(SearchInputFragment.KEY_SEARCH_DATE);
                String searchTime = arguments.getString(SearchInputFragment.KEY_SEARCH_TIME);

                SearchDetailsFragment searchDetailsFragment = SearchDetailsFragment.newInstance(routeId, routeName, searchDate, searchTime);
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                this.navigationManager.navigateTo(searchDetailsFragment);
            } else if(arguments.getInt(SearchInputFragment.KEY_FRAGMENT_ACTION) == SearchInputFragment.ACTION_SHOW_SETTINGS) {
                SettingsFragment settingsFragment = SettingsFragment.newInstance();
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                this.navigationManager.navigateTo(settingsFragment);
            } else if(arguments.getInt(SearchInputFragment.KEY_FRAGMENT_ACTION) == SearchInputFragment.ACTION_SHOW_CALENDAR) {
                SearchCalendarFragment calendarFragment = SearchCalendarFragment.newInstance();
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                this.navigationManager.navigateTo(calendarFragment);
            }
        } else if(fragment instanceof SearchCalendarFragment) {
            if(arguments.getInt(SearchCalendarFragment.KEY_FRAGMENT_ACTION) == SearchCalendarFragment.ACTION_SELECT_ROUTE) {
                String routeId = arguments.getString(SearchCalendarFragment.KEY_SEARCH_ROUTE_ID);
                String routeName = arguments.getString(SearchCalendarFragment.KEY_SEARCH_ROUTE_NAME);
                String searchDate = arguments.getString(SearchCalendarFragment.KEY_SEARCH_DATE);
                String searchTime = arguments.getString(SearchCalendarFragment.KEY_SEARCH_TIME);

                SearchDetailsFragment searchDetailsFragment = SearchDetailsFragment.newInstance(routeId, routeName, searchDate, searchTime);
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                this.navigationManager.navigateTo(searchDetailsFragment);
            } else if(arguments.getInt(SearchCalendarFragment.KEY_FRAGMENT_ACTION) == SearchCalendarFragment.ACTION_SHOW_SETTINGS) {
                SettingsFragment settingsFragment = SettingsFragment.newInstance();
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                this.navigationManager.navigateTo(settingsFragment);
            }
        } else if(fragment instanceof SearchDetailsFragment) {
            if(arguments.getInt(SearchDetailsFragment.KEY_FRAGMENT_ACTION) == SearchDetailsFragment.ACTION_SELECT_TRIP) {
                String tripId = arguments.getString(SearchDetailsFragment.KEY_TRIP_ID);
                String tripDate = arguments.getString(SearchDetailsFragment.KEY_TRIP_DATE);
                String tripTime = arguments.getString(SearchDetailsFragment.KEY_TRIP_TIME);

                TripDetailsFragment tripDetailsFragment = TripDetailsFragment.newInstance(tripId, tripDate, tripTime);
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                this.navigationManager.navigateTo(tripDetailsFragment);
            }
        } else if(fragment instanceof TripDetailsFragment) {
            if(arguments.getInt(TripDetailsFragment.KEY_FRAGMENT_ACTION) == TripDetailsFragment.ACTION_VIEW_FAVORITES) {
                FavoritesFragment favoritesFragment = FavoritesFragment.newInstance();
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                this.navigationManager.navigateTo(favoritesFragment);
            }
        } else if(fragment instanceof FavoritesFragment) {
            if(arguments.getInt(FavoritesFragment.KEY_FRAGMENT_ACTION) == FavoritesFragment.ACTION_SELECT_TRIP) {
                String tripId = arguments.getString(FavoritesFragment.KEY_TRIP_ID);
                String tripDate = arguments.getString(FavoritesFragment.KEY_TRIP_DATE);
                String tripTime = arguments.getString(FavoritesFragment.KEY_TRIP_TIME);

                TripDetailsFragment tripDetailsFragment = TripDetailsFragment.newInstance(tripId, tripDate, tripTime);
                this.navigationManager.setNextAnimation(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                this.navigationManager.navigateTo(tripDetailsFragment);
            }
        }
    }

    private void resetSecondaryAuthentification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.str_reset);
        builder.setMessage(R.string.str_secauth_reset_confirmation);
        builder.setPositiveButton(R.string.str_reset, (dialog, which) -> {
            this.settingsManager.resetApiCredentials();
            this.displayAgencyMenu();
        });
        builder.setNegativeButton(R.string.str_no, null);
        builder.show();
    }

    private void displayAgencyMenu() {
        Menu mainMenu = this.components.navigationView.getMenu();
        MenuItem agencyMenuItem = mainMenu.findItem(R.id.navigationMenuAgency);

        if(this.settingsManager.isSecondaryAuthentification()) {
            agencyMenuItem.getSubMenu().setGroupVisible(R.id.navigationGroupAgency, true);
        } else {
            agencyMenuItem.getSubMenu().setGroupVisible(R.id.navigationGroupAgency, false);
        }
    }
}
