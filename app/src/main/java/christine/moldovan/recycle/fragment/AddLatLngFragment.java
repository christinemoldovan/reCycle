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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import christine.moldovan.recycle.databinding.FragmentAddLatLngBinding;


public class AddLatLngFragment extends Fragment {

    FragmentAddLatLngBinding binding;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference markersRef, userSuggestedMarkersRef;
    private TextView markerDescriptionEditText;
    private TextInputLayout titleLayout, addressOneLayout, addressTwoLayout, descriptionLayout;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
        markersRef = firebaseDatabase.getReference().child("mapMarkers");
        userSuggestedMarkersRef = firebaseDatabase.getReference().child("userSuggestedMarkers");
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
        View v = inflater.inflate(R.layout.fragment_add_lat_lng, container, false);

        binding = FragmentAddLatLngBinding.inflate(inflater, container, false);

        markerDescriptionEditText = (TextView) v.findViewById(R.id.MarkerDescription);
        titleLayout = (TextInputLayout) v.findViewById(R.id.AddressTitleLayout);
        addressOneLayout = (TextInputLayout) v.findViewById(R.id.AddressAddOneLayout);
        addressTwoLayout = (TextInputLayout) v.findViewById(R.id.AddressAddTwoLayout);
        descriptionLayout = (TextInputLayout) v.findViewById(R.id.descriptionLayout);

        checkBox1 = (CheckBox) v.findViewById(R.id.checkBox1);
        checkBox2 = (CheckBox) v.findViewById(R.id.checkBox2);
        checkBox3 = (CheckBox) v.findViewById(R.id.checkBox3);
        checkBox4 = (CheckBox) v.findViewById(R.id.checkBox4);
        checkBox5 = (CheckBox) v.findViewById(R.id.checkBox5);
        checkBox6 = (CheckBox) v.findViewById(R.id.checkBox6);

        Bundle bundle = this.getArguments();
        String latValue = bundle.getString("latitude");
        String lngValue = bundle.getString("longitude");
        String primaryAddress = bundle.getString("address");
        String email = bundle.getString("email");
        binding.LatitudeAdd.setText(latValue);
        binding.LongitudeAdd.setText(lngValue);
        binding.AddressAddOne.setText(primaryAddress);

        // for adding from user suggested
        if (bundle.containsKey("description")) {
            String description = bundle.getString("description");
            if (!description.isEmpty()) {
                binding.MarkerDescription.setText(description);
            }
        }

        if (bundle.containsKey("secondaryAddress")) {
            String secondaryAddress = bundle.getString("secondaryAddress");
            if (!secondaryAddress.isEmpty()) {
                binding.AddressAddTwo.setText(secondaryAddress);
            }
        }

        if (bundle.containsKey("titleAddress")) {
            binding.DeleteButton.setVisibility(View.VISIBLE);
            String titleAddress = bundle.getString("titleAddress");
            binding.AddressTitle.setText(titleAddress);
        }

        if (bundle.containsKey("recyclingPoints")) {
            String recyclingPoints = bundle.getString("recyclingPoints");
            String[] recyclingSplitPoints = recyclingPoints.split("(?=[A-Z])");
            String finalRecyclingPoints = String.join(" ", recyclingSplitPoints);
            String[] words = finalRecyclingPoints.split(" ");
            // Check the corresponding checkboxes based on the word
            for (String word : words) {
                System.out.println(word);
                if (word.equals("Plastic")) {
                    binding.checkBox1.setChecked(true);
                } else if (word.equals("Paper")) {
                    binding.checkBox2.setChecked(true);
                } else if (word.equals("Glass")) {
                    binding.checkBox3.setChecked(true);
                } else if (word.equals("Metal")) {
                    binding.checkBox4.setChecked(true);
                } else if (word.equals("Batteries")) {
                    binding.checkBox5.setChecked(true);
                } else if (word.equals("Electronics")) {
                    binding.checkBox6.setChecked(true);
                }
            }
        }

        binding.AddressTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.AddressTitle.getText().toString().isEmpty()) {
                    binding.AddressTitleLayout.setError("This field cannot be empty");
                } else {
                    binding.AddressTitleLayout.setError(null);
                }
            }
        });
        binding.AddressAddOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.AddressAddOne.getText().toString().isEmpty()) {
                    binding.AddressAddOneLayout.setError("This field cannot be empty");
                } else if (binding.AddressAddOne.getText().toString().length() > 80) {
                    binding.AddressAddOneLayout.setError("Primary address is too long");
                } else {
                    binding.AddressAddOneLayout.setError(null);
                }
            }
        });
        binding.AddressAddTwo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.AddressAddTwo.getText().length() > 45) {
                    binding.AddressAddTwoLayout.setError("Secondary address is too long");
                } else {
                    binding.AddressAddTwoLayout.setError(null);
                }
            }
        });
        binding.MarkerDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.MarkerDescription.getText().length() > 150) {
                    binding.descriptionLayout.setError("Marker description is too long");
                } else {
                    binding.descriptionLayout.setError(null);
                }
            }
        });

        binding.SubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedRecyclingPoints = new ArrayList<String>();
                if (binding.checkBox1.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox1.getText().toString());
                }
                if (binding.checkBox2.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox2.getText().toString());
                }
                if (binding.checkBox3.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox3.getText().toString());
                }
                if (binding.checkBox4.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox4.getText().toString());
                }
                if (binding.checkBox5.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox5.getText().toString());
                }
                if (binding.checkBox6.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox6.getText().toString());
                }

                String recyclePoints = TextUtils.join("", selectedRecyclingPoints);

                if (recyclePoints.isEmpty()) {
                    Toast.makeText(getActivity(), "Please select at least one recycling point", Toast.LENGTH_LONG).show();
                    return;
                }
                if (binding.AddressAddOne.getText().length() > 80) {
                    binding.AddressAddOneLayout.setError("Primary address is too long");
                    return;
                }
                if (binding.AddressAddTwo.getText().length() > 45) {
                    binding.AddressAddTwoLayout.setError("Secondary address is too long");
                    return;
                }
                if (binding.AddressTitle.getText().length() > 30) {
                    binding.AddressTitleLayout.setError("Title address is too long");
                    return;
                }
                if (binding.MarkerDescription.getText().length() > 150) {
                    binding.descriptionLayout.setError("Marker description is too long");
                    return;
                }
                if (binding.AddressAddOne.getText().toString().isEmpty()) {
                    binding.AddressAddOneLayout.setError("This field cannot be empty");
                    return;
                }
                if (binding.AddressTitle.getText().toString().isEmpty()) {
                    binding.AddressTitleLayout.setError("This field cannot be empty");
                    return;
                }
                MapMarker mapMarker = new MapMarker();
                mapMarker.setLatitude(Double.parseDouble(binding.LatitudeAdd.getText().toString()));
                mapMarker.setLongitude(Double.parseDouble(binding.LongitudeAdd.getText().toString()));
                mapMarker.setPrimaryAddress(binding.AddressAddOne.getText().toString());
                mapMarker.setSecondaryAddress(binding.AddressAddTwo.getText().toString());
                mapMarker.setTitleAddress(binding.AddressTitle.getText().toString());
                mapMarker.setDescriptionAddress(binding.MarkerDescription.getText().toString());
                //  mapMarker.setRecyclePoint(binding.recycleDropdown.getSelectedItem().toString());
                mapMarker.setRecyclePoint(recyclePoints);

                Bundle bundleEmail = new Bundle();
                bundleEmail.putString("email", email);
                //TODO: check if lat/lng already exists, and if it does, edit that value
                double latitude = Double.parseDouble(binding.LatitudeAdd.getText().toString());
                double longitude = Double.parseDouble(binding.LongitudeAdd.getText().toString());
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
                                    deleteUserSuggestedMarker(latitude, longitude);
                                    Toast.makeText(getActivity(), "Updated marker successfully!", Toast.LENGTH_LONG).show();
                                    getActivity().getSupportFragmentManager().popBackStack();
                                    return;
                                }
                            }
                        }

                        // If no matching child node exists, create a new one
                        firebaseDatabase.getReference().child("mapMarkers").push().setValue(mapMarker).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getActivity(), "Uploaded marker successfully!",
                                        Toast.LENGTH_LONG).show();
                                deleteUserSuggestedMarker(latitude, longitude);
                                getActivity().getSupportFragmentManager().popBackStack();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), "An error occurred when uploading!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle cancellation
                    }
                });
            }
        });
        binding.DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitude = Double.parseDouble(binding.LatitudeAdd.getText().toString());
                double longitude = Double.parseDouble(binding.LongitudeAdd.getText().toString());
                Toast.makeText(getActivity(), "Suggested marker deleted successfully!", Toast.LENGTH_SHORT).show();
                deleteUserSuggestedMarker(latitude, longitude);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return binding.getRoot();
    }

    private void deleteUserSuggestedMarker(double latitude, double longitude) {
        Query query = userSuggestedMarkersRef.orderByChild("latitude").equalTo(latitude);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        MapMarker existingMarker = child.getValue(MapMarker.class);
                        if (existingMarker.getLongitude() == longitude) {
                            String key = child.getKey();
                            child.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}