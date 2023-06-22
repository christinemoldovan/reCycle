package christine.moldovan.recycle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import christine.moldovan.recycle.R;
import christine.moldovan.recycle.databinding.FragmentListLocationsBinding;

public class ListLocationsFragment extends Fragment {
    FragmentListLocationsBinding binding;
    private List<MarkerOptions> allMarkers = new ArrayList<MarkerOptions>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                getActivity().getSupportFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list_locations, container, false);
        binding = FragmentListLocationsBinding.inflate(inflater, container, false);
        Bundle bundle = this.getArguments();
        MarkerOptions[] markersArray = (MarkerOptions[]) bundle.getParcelableArray("markers");
        allMarkers = Arrays.asList(markersArray);

        for (MarkerOptions marker : allMarkers) {
            System.out.println("Position: " + marker.getPosition() + ", Title: " + marker.getTitle() + ", Snippet: " + marker.getSnippet());
        }
        return binding.getRoot();
    }
}