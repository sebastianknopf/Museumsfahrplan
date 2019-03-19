package de.mfpl.app.fragments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;

import de.mfpl.app.R;
import de.mfpl.app.databinding.FragmentPreferencesBinding;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String TAG ="SettingsFragment";

    public final static String KEY_WHEELCHAIR_ACCESSIBLE = "KEY_WHEELCHAIR_ACCESSIBLE";
    public final static String KEY_BIKES_ALLOWED = "KEY_BIKES_ALLOWED";
    public final static String KEY_NUM_RESULTS = "KEY_NUM_RESULTS";

    private FragmentPreferencesBinding components;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set activity title
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(R.string.str_preferences_title);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        this.addPreferencesFromResource(R.xml.screen_fragment_preferences);
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_preferences, container, false);

        // set activity title
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(R.string.str_preferences_title);
        }

        return this.components.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }*/
}
