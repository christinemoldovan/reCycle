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
import christine.moldovan.recycle.databinding.FragmentSuggestAddLocationBinding;


public class SuggestAddLocationFragment extends Fragment {

    FragmentSuggestAddLocationBinding binding;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userSuggestedMarkersRef;
    private TextView markerDescriptionEditText;
    private TextInputLayout titleLayout, addressOneLayout, addressTwoLayout, descriptionLayout;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_suggest_add_location, container, false);
        binding = FragmentSuggestAddLocationBinding.inflate(inflater, container, false);
        markerDescriptionEditText = (TextView) v.findViewById(R.id.MarkerDescriptionSuggest);
        titleLayout = (TextInputLayout) v.findViewById(R.id.AddressTitleLayoutSuggest);
        addressOneLayout = (TextInputLayout) v.findViewById(R.id.AddressAddOneLayoutSuggest);
        addressTwoLayout = (TextInputLayout) v.findViewById(R.id.AddressAddTwoLayoutSuggest);
        descriptionLayout = (TextInputLayout) v.findViewById(R.id.descriptionLayoutSuggest);

        checkBox1 = (CheckBox) v.findViewById(R.id.checkBox1Suggest);
        checkBox2 = (CheckBox) v.findViewById(R.id.checkBox2Suggest);
        checkBox3 = (CheckBox) v.findViewById(R.id.checkBox3Suggest);
        checkBox4 = (CheckBox) v.findViewById(R.id.checkBox4Suggest);
        checkBox5 = (CheckBox) v.findViewById(R.id.checkBox5Suggest);
        checkBox6 = (CheckBox) v.findViewById(R.id.checkBox6Suggest);

        Bundle bundle = this.getArguments();
        String latValue = bundle.getString("latitude");
        String lngValue = bundle.getString("longitude");
        String addressLines = bundle.getString("address");
        String postalCode = bundle.getString("postalCode");
        String email = bundle.getString("email");
        binding.LatitudeAddSuggest.setText(latValue);
        binding.LongitudeAddSuggest.setText(lngValue);
        binding.AddressAddOneSuggest.setText(addressLines);

        binding.AddressTitleSuggest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.AddressTitleSuggest.getText().toString().isEmpty()) {
                    binding.AddressTitleLayoutSuggest.setError("This field cannot be empty");
                } else {
                    binding.AddressTitleLayoutSuggest.setError(null);
                }
            }
        });
        binding.AddressAddOneSuggest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.AddressAddOneSuggest.getText().toString().isEmpty()) {
                    binding.AddressAddOneLayoutSuggest.setError("This field cannot be empty");
                } else if (binding.AddressAddOneSuggest.getText().toString().length() > 80) {
                    binding.AddressAddOneLayoutSuggest.setError("Primary address is too long");
                } else {
                    binding.AddressAddOneLayoutSuggest.setError(null);
                }
            }
        });
        binding.AddressAddTwoSuggest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.AddressAddTwoSuggest.getText().length() > 45) {
                    binding.AddressAddTwoLayoutSuggest.setError("Secondary address is too long");
                } else {
                    binding.AddressAddTwoLayoutSuggest.setError(null);
                }
            }
        });

        binding.MarkerDescriptionSuggest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.MarkerDescriptionSuggest.getText().length() > 150) {
                    binding.descriptionLayoutSuggest.setError("Marker description is too long");
                } else {
                    binding.descriptionLayoutSuggest.setError(null);
                }
            }
        });

        binding.SubmitButtonSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedRecyclingPoints = new ArrayList<String>();
                if (binding.checkBox1Suggest.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox1Suggest.getText().toString());
                }
                if (binding.checkBox2Suggest.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox2Suggest.getText().toString());
                }
                if (binding.checkBox3Suggest.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox3Suggest.getText().toString());
                }
                if (binding.checkBox4Suggest.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox4Suggest.getText().toString());
                }
                if (binding.checkBox5Suggest.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox5Suggest.getText().toString());
                }
                if (binding.checkBox6Suggest.isChecked()) {
                    selectedRecyclingPoints.add(binding.checkBox6Suggest.getText().toString());
                }

                String recyclePoints = TextUtils.join("", selectedRecyclingPoints);

                if (recyclePoints.isEmpty()) {
                    Toast.makeText(getActivity(), "Please select at least one recycling point", Toast.LENGTH_LONG).show();
                    return;
                }
                if (binding.AddressAddOneSuggest.getText().length() > 80) {
                    binding.AddressAddOneLayoutSuggest.setError("Primary address is too long");
                    return;
                }
                if (binding.AddressAddTwoSuggest.getText().length() > 45) {
                    binding.AddressAddTwoLayoutSuggest.setError("Secondary address is too long");
                    return;
                }
                if (binding.AddressTitleSuggest.getText().length() > 30) {
                    binding.AddressTitleLayoutSuggest.setError("Title address is too long");
                    return;
                }
                if (binding.MarkerDescriptionSuggest.getText().length() > 150) {
                    binding.descriptionLayoutSuggest.setError("Marker description is too long");
                    return;
                }
                if (binding.AddressAddOneSuggest.getText().toString().isEmpty()) {
                    binding.AddressAddOneLayoutSuggest.setError("This field cannot be empty");
                    return;
                }
                if (binding.AddressTitleSuggest.getText().toString().isEmpty()) {
                    binding.AddressTitleLayoutSuggest.setError("This field cannot be empty");
                    return;
                }
                MapMarker mapMarker = new MapMarker();
                mapMarker.setLatitude(Double.parseDouble(binding.LatitudeAddSuggest.getText().toString()));
                mapMarker.setLongitude(Double.parseDouble(binding.LongitudeAddSuggest.getText().toString()));
                mapMarker.setPrimaryAddress(binding.AddressAddOneSuggest.getText().toString());
                mapMarker.setSecondaryAddress(binding.AddressAddTwoSuggest.getText().toString());
                mapMarker.setTitleAddress(binding.AddressTitleSuggest.getText().toString());
                mapMarker.setDescriptionAddress(binding.MarkerDescriptionSuggest.getText().toString());
                mapMarker.setRecyclePoint(recyclePoints);

                Bundle bundleEmail = new Bundle();
                bundleEmail.putString("email", email);
                //TODO: check if lat/lng already exists, and if it does, edit that value
                double latitude = Double.parseDouble(binding.LatitudeAddSuggest.getText().toString());
                double longitude = Double.parseDouble(binding.LongitudeAddSuggest.getText().toString());
                Query query = userSuggestedMarkersRef.orderByChild("latitude").equalTo(latitude);
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
                                    userSuggestedMarkersRef.child(key).updateChildren(newValues);
                                    Toast.makeText(getActivity(), "Updated suggested location successfully!", Toast.LENGTH_LONG).show();
                                    getActivity().getSupportFragmentManager().popBackStack();
                                    return;
                                }
                            }
                        }

                        // If no matching child node exists, create a new one
                        firebaseDatabase.getReference().child("userSuggestedMarkers").push().setValue(mapMarker).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getActivity(), "Suggested location uploaded successfully!",
                                        Toast.LENGTH_LONG).show();
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
        return binding.getRoot();
    }
}