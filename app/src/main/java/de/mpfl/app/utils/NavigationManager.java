package de.mpfl.app.utils;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public final class NavigationManager {

    private AppCompatActivity parentActivity;

    private int targetContainerId;
    private int toggleResId;
    private int backResId;

    public NavigationManager(AppCompatActivity parentActivity, int targetContainerId, int toggleResId, int backResId) {
        this.parentActivity = parentActivity;

        this.targetContainerId = targetContainerId;
        this.toggleResId = toggleResId;
        this.backResId = backResId;
    }

    public NavigationManager(AppCompatActivity parentActivity, int targetContainerId, int toggleResId) {
        this.parentActivity = parentActivity;

        this.targetContainerId = targetContainerId;
        this.toggleResId = toggleResId;
        this.backResId = 0;
    }

    private void syncActionBar(int backStackCount) {
        ActionBar actionBar = this.parentActivity.getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            if(backStackCount > 0) {
                actionBar.setHomeAsUpIndicator(this.backResId);
            } else {
                actionBar.setHomeAsUpIndicator(this.toggleResId);
            }
        }
    }

    public void navigateTo(Fragment targetFragment, boolean keepStack) {
        FragmentManager fragmentManager = this.parentActivity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction().replace(this.targetContainerId, targetFragment);
        int backStackCount = fragmentManager.getBackStackEntryCount();

        if(keepStack) {
            transaction.addToBackStack(targetFragment.getTag());
            backStackCount++;
        } else {
            while(fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStackImmediate();
            }
            backStackCount = 0;
        }

        transaction.commit();
        this.syncActionBar(backStackCount);
    }

    public void navigateTo(Fragment targetFragment) {
        this.navigateTo(targetFragment, true);
    }

    public void navigateBack() {
        FragmentManager fragmentManager = this.parentActivity.getSupportFragmentManager();
        int backStackCount = fragmentManager.getBackStackEntryCount();

        if(backStackCount > 0) {
            fragmentManager.popBackStack();
            backStackCount--;
        }

        this.syncActionBar(backStackCount);
    }

    public int getBackStackCount() {
        return this.parentActivity.getSupportFragmentManager().getBackStackEntryCount();
    }

}
