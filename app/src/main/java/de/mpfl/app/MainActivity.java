package de.mpfl.app;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import de.mpfl.app.databinding.ActivityMainBinding;
import de.mpfl.app.fragments.FavoritesFragment;
import de.mpfl.app.fragments.InfoDetailsFragment;
import de.mpfl.app.fragments.InfoListFragment;
import de.mpfl.app.fragments.MapOverviewFragment;
import de.mpfl.app.fragments.PreferencesFragment;
import de.mpfl.app.fragments.SearchInputFragment;
import de.mpfl.app.utils.NavigationManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, InfoListFragment.InfoListFragmentListener {

    private ActivityMainBinding components;
    private NavigationManager navigationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set theme before doing any other thing
        this.setTheme(R.style.AppTheme);

        // initialize activity content
        super.onCreate(savedInstanceState);
        this.components = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // setup navigation manager
        this.navigationManager = new NavigationManager(this, this.components.contentView.getId(), R.drawable.ic_menu_toggle);

        // set toolbar as actionbar
        this.setSupportActionBar(this.components.mainToolbar);

        // set event for each menu item to be selected
        this.components.navigationView.setNavigationItemSelectedListener(this);

        // select the first item by default
        this.components.navigationView.setCheckedItem(R.id.navigationMenuMap);
        this.onNavigationItemSelected(this.components.navigationView.getMenu().findItem(R.id.navigationMenuMap));
    }

    // make navigation manager accessible for further purposes
    public NavigationManager getNavigationManager() {
        return this.navigationManager;
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
                targetFragment = SearchInputFragment.newInstance();
                break;

            case R.id.navigationMenuFavorites:
                targetFragment = FavoritesFragment.newInstance();
                break;

            case R.id.navigationMenuSettings:
                targetFragment = PreferencesFragment.newInstance();
                break;

            case R.id.navigationMenuInformation:
                targetFragment = InfoListFragment.newInstance();
                break;
        }

        // navigate to target fragment without keeping the back stack
        if(targetFragment != null) {
            this.navigationManager.navigateTo(targetFragment, false);
        }

        // set checked item and hide drawer
        item.setChecked(true);
        setTitle(item.getTitle());

        // hide navigation drawer
        this.components.navigationDrawer.closeDrawers();

        return true;
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

    // called ever time when a info item is selected in info list fragment
    @Override
    public void onInformationCategorySelected(int categoryIndex) {
        Fragment infoDetailsFragment = null;
        switch(categoryIndex) {
            case 0:
                infoDetailsFragment = InfoDetailsFragment.newInstance("html_info_default.html");
                break;

            case 1:
                infoDetailsFragment = InfoDetailsFragment.newInstance("html_info_privacy.html");
                break;

            case 2:
                infoDetailsFragment = InfoDetailsFragment.newInstance("html_info_opensource.html");
                break;
        }

        // navigate to desired info fragment with keeping the back stack
        if(infoDetailsFragment != null) {
            this.navigationManager.navigateTo(infoDetailsFragment);
        }
    }
}
