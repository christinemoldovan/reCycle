package christine.moldovan.recycle.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import christine.moldovan.recycle.MapMarker;
import christine.moldovan.recycle.R;
import christine.moldovan.recycle.databinding.FragmentLocationEditBinding;


public class LocationEditFragment extends Fragment {
    FragmentLocationEditBinding binding;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference markersRef;
    private TextView markerDescriptionEditText;
    private TextInputLayout titleLayout, descriptionLayout;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
        markersRef = firebaseDatabase.getReference().child("mapMarkers");

        // This callback will only be called when MyFragment is at least Started.
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
        View v = inflater.inflate(R.layout.fragment_location_edit, container, false);
        binding = FragmentLocationEditBinding.inflate(inflater, container, false);
        markerDescriptionEditText = (TextView) v.findViewById(R.id.MarkerDescriptionEdit);
        titleLayout = (TextInputLayout) v.findViewById(R.id.AddressTitleLayoutEdit);

        descriptionLayout = (TextInputLayout) v.findViewById(R.id.descriptionLayoutEdit);

        checkBox1 = (CheckBox) v.findViewById(R.id.checkBox1Edit);
        checkBox2 = (CheckBox) v.findViewById(R.id.checkBox2Edit);
        checkBox3 = (CheckBox) v.findViewById(R.id.checkBox3Edit);
        checkBox4 = (CheckBox) v.findViewById(R.id.checkBox4Edit);
        checkBox5 = (CheckBox) v.findViewById(R.id.checkBox5Edit);
        checkBox6 = (CheckBox) v.findViewById(R.id.checkBox6Edit);

        Bundle bundle = this.getArguments();
        String email = bundle.getString("email");
        Double latValue = bundle.getDouble("latitude");
        Double lngValue = bundle.getDouble("longitude");
        String addressLine1 = bundle.getString("primaryAddress");
        String addressLine2 = bundle.getString("secondaryAddress");
        String addressTitle = bundle.getString("titleAddress");
        String description = bundle.getString("description");
        String recyclingPoints = bundle.getString("recyclingPoints");

        String[] recyclingSplitPoints = recyclingPoints.split("(?=[A-Z])");
        String finalRecyclingPoints = String.join(" ", recyclingSplitPoints);
        String[] words = finalRecyclingPoints.split(" ");
        // Check the corresponding checkboxes based on the word
        for (String word : words) {
            System.out.println(word);
            if (word.equals("Plastic")) {
                binding.checkBox1Edit.setChecked(true);
            } else if (word.equals("Paper")) {
                binding.checkBox2Edit.setChecked(true);
            } else if (word.equals("Glass")) {
                binding.checkBox3Edit.setChecked(true);
            } else if (word.equals("Metal")) {
                binding.checkBox4Edit.setChecked(true);
            } else if (word.equals("Batteries")) {
                binding.checkBox5Edit.setChecked(true);
            } else if (word.equals("Electronics")) {
                binding.checkBox6Edit.setChecked(true);
            }
        }
        binding.LatitudeAddEdit.setText(latValue.toString());
        binding.LongitudeAddEdit.setText(lngValue.toString());
        binding.AddressTitleEdit.setText(addressTitle);
        binding.MarkerDescriptionEdit.setText(description);


        //TODO: update map and database based on edits
        binding.AddressTitleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.AddressTitleEdit.getText().toString().isEmpty()) {
                    binding.AddressTitleLayoutEdit.setError("This field cannot be empty");
                } else {
                    binding.AddressTitleLayoutEdit.setError(null);
                }
            }
        });

        binding.MarkerDescriptionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.MarkerDescriptionEdit.getText().length() > 150) {
                    binding.descriptionLayoutEdit.setError("Marker description is too long");
                } else {
                    binding.descriptionLayoutEdit.setError(null);
                }
            }
        });

        binding.UpdateButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedRecyclingPoints = new ArrayList<String>();
                if (binding.checkBox1Edit.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox1Edit.getText().toString());
                }
                if (binding.checkBox2Edit.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox2Edit.getText().toString());
                }
                if (binding.checkBox3Edit.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox3Edit.getText().toString());
                }
                if (binding.checkBox4Edit.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox4Edit.getText().toString());
                }
                if (binding.checkBox5Edit.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox5Edit.getText().toString());
                }
                if (binding.checkBox6Edit.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox6Edit.getText().toString());
                }

                String recyclePoints = TextUtils.join("", selectedRecyclingPoints);

                if (recyclePoints.isEmpty()) {
                    Toast.makeText(getActivity(), "Please select at least one recycling point", Toast.LENGTH_LONG).show();
                    return;
                }
                if (binding.MarkerDescriptionEdit.getText().length() > 150) {
                    binding.descriptionLayoutEdit.setError("Marker description is too long");
                    return;
                }
                if (binding.AddressTitleEdit.getText().toString().isEmpty()) {
                    binding.AddressTitleLayoutEdit.setError("This field cannot be empty");
                    return;
                }

                //Updating marker
                MapMarker mapMarker = new MapMarker();
                mapMarker.setLatitude(latValue);
                mapMarker.setLongitude(lngValue);
                mapMarker.setPrimaryAddress(addressLine1);
                mapMarker.setSecondaryAddress(addressLine2);
                mapMarker.setTitleAddress(binding.AddressTitleEdit.getText().toString());
                mapMarker.setDescriptionAddress(binding.MarkerDescriptionEdit.getText().toString());
                mapMarker.setRecyclePoint(recyclePoints);

                Bundle bundleEmail = new Bundle();
                bundleEmail.putString("email", email);

                double latitude = Double.parseDouble(binding.LatitudeAddEdit.getText().toString());
                double longitude = Double.parseDouble(binding.LongitudeAddEdit.getText().toString());

                Query query = markersRef.orderByChild("latitude").equalTo(latitude);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                MapMarker existingMarker = child.getValue(MapMarker.class);
                                if (existingMarker.getLongitude() == longitude) {
                                    // Update the existing marker with the new values
                                    String key = child.getKey();
                                    Map<String, Object> newValues = new HashMap<>();
                                    newValues.put("primaryAddress", mapMarker.getPrimaryAddress());
                                    newValues.put("secondaryAddress", mapMarker.getSecondaryAddress());
                                    newValues.put("recyclePoint", mapMarker.getRecyclePoint());
                                    newValues.put("titleAddress", mapMarker.getTitleAddress());
                                    newValues.put("descriptionAddress", mapMarker.getDescriptionAddress());
                                    markersRef.child(key).updateChildren(newValues);
                                    Toast.makeText(getActivity(), "Updated marker successfully!", Toast.LENGTH_LONG).show();
//                                    Fragment fragment = new MapsFragment();
//                                    fragment.setArguments(bundleEmail);
//                                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//                                    fragmentTransaction.replace(R.id.container, fragment).commit();
                                    getActivity().getSupportFragmentManager().popBackStack();

                                    return;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle cancellation
                    }
                });
            }
        });
        return binding.getRoot();
    }
}