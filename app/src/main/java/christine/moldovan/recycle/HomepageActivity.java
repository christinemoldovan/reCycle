package christine.moldovan.recycle;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationBarView;

import christine.moldovan.recycle.databinding.ActivityHomepageBinding;
import christine.moldovan.recycle.fragment.HomepageFragment;
import christine.moldovan.recycle.fragment.MapsFragment;
import christine.moldovan.recycle.fragment.ProfileFragment;
import christine.moldovan.recycle.fragment.WasteClassifierFragment;

public class HomepageActivity extends AppCompatActivity {
    final Fragment fragmentDashboard = new HomepageFragment();
    final Fragment fragmentMaps = new MapsFragment();
    final Fragment fragmentClassifier = new WasteClassifierFragment();
    final Fragment fragmentProfile = new ProfileFragment();
    final FragmentManager fm = getSupportFragmentManager();
    ActivityHomepageBinding binding;
    Fragment currentActive = fragmentDashboard;
    private String email;

    public HomepageActivity() {
        super();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        fragmentMaps.setArguments(bundle);
        fragmentProfile.setArguments(bundle);
        fragmentDashboard.setArguments(bundle);

        fm.beginTransaction().add(R.id.container, fragmentMaps, "maps").hide(fragmentMaps).commit();
        fm.beginTransaction().add(R.id.container, fragmentClassifier, "classifier").hide(fragmentClassifier).commit();
        fm.beginTransaction().add(R.id.container, fragmentProfile, "profile").hide(fragmentProfile).commit();
        fm.beginTransaction().add(R.id.container, fragmentDashboard, "dashboard").commit();

        binding.navigationBarBottom.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.Homepage:
                        fm.beginTransaction().hide(currentActive).show(fragmentDashboard).commit();
                        currentActive = fragmentDashboard;
                        break;
                    case R.id.Maps:
                        fm.beginTransaction().hide(currentActive).show(fragmentMaps).commit();
                        currentActive = fragmentMaps;
                        break;
                    case R.id.Classifier:
                        fm.beginTransaction().hide(currentActive).show(fragmentClassifier).commit();
                        currentActive = fragmentClassifier;
                        break;
                    case R.id.Profile:
                        fm.beginTransaction().hide(currentActive).show(fragmentProfile).commit();
                        currentActive = fragmentProfile;
                        break;
                }
                return true;
            }
        });
    }
}