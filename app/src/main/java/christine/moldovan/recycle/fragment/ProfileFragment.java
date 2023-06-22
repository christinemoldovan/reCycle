package christine.moldovan.recycle.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import christine.moldovan.recycle.R;
import christine.moldovan.recycle.loginscreen.LoginActivity;
import christine.moldovan.recycle.loginscreen.User;

public class ProfileFragment extends Fragment {
    private static final String USERS = "users";
    private final String TAG = "ProfileActivity";
    private TextView nameTextView;
    private TextView emailTextView, phoneTextView;
    private ImageView emailImageView, phoneImageView;
    private Button logoutButton, changePhone, changePassword;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private String email;
    private String userKey;
    private User user;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_profile, container, false);

        //Receive data from login screen
        Bundle bundle = this.getArguments();
        System.out.println(bundle);
        email = bundle.getString("email");

        nameTextView = (TextView) v.findViewById(R.id.name_textView);
        emailTextView = (TextView) v.findViewById(R.id.email_textView);
        phoneTextView = (TextView) v.findViewById(R.id.phone_textView);

        emailImageView = (ImageView) v.findViewById(R.id.email_imageView);
        phoneImageView = (ImageView) v.findViewById(R.id.phone_imageView);

        logoutButton = (Button) v.findViewById(R.id.logout_button);
        changePhone = (Button) v.findViewById(R.id.changePhone_button);
        changePassword = (Button) v.findViewById(R.id.changePassword_button);
        if (isNetworkAvailable() == 0) {
            emailImageView.setVisibility(View.GONE);
            emailTextView.setVisibility(View.GONE);
            phoneImageView.setVisibility(View.GONE);
            phoneTextView.setVisibility(View.GONE);
            changePassword.setVisibility(View.GONE);
            changePhone.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_LONG).show();
            return v;
        }
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = rootRef.child(USERS);

        Log.v("USERID", userRef.getKey());

        // Read from the database
        userRef.addValueEventListener(new ValueEventListener() {
            String name, phone;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot keyId : dataSnapshot.getChildren()) {
                    if (keyId.child("email").getValue().equals(email)) {
                        name = keyId.child("fullName").getValue(String.class);
                        phone = keyId.child("phone").getValue(String.class);
                        userKey = keyId.getKey();
                    }
                }
                nameTextView.setText(name);
                emailTextView.setText(email);
                phoneTextView.setText(phone);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                FirebaseAuth.getInstance().signOut();
                startActivity(loginIntent);
                //finish();
            }
        });
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailTextView.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Password reset has been sent to your email!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "Error sending email!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        changePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                final TextView title = (TextView) mView.findViewById(R.id.titleDialog);
                final TextView description = (TextView) mView.findViewById(R.id.descriptionDialog);
                final EditText newPhoneNumber = (EditText) mView.findViewById(R.id.dialog_textEdit);
                newPhoneNumber.setHint("Type new phone number");
                title.setText("Update phone number");
                description.setText("Enter the phone number and we'll send a verification code SMS to verify your number.");

                Button cancelButton = (Button) mView.findViewById(R.id.cancelDialog_button);
                Button okayButton = (Button) mView.findViewById(R.id.okayDialog_button);
                alert.setView(mView);
                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside(false);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                okayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println(newPhoneNumber.getText().toString());
                        userRef.child(userKey).child("phone").setValue(newPhoneNumber.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Phone number has been updated!", Toast.LENGTH_LONG).show();
                                    alertDialog.dismiss();
                                } else {
                                    Toast.makeText(getActivity(), "Error updating phone number!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
                alertDialog.show();
            }
        });
        //TODO: sms verification for phone number

        return v;
    }

    private int isNetworkAvailable() {
        int result = 0; // Returns connection type. 0: none; 1: mobile data; 2: wifi
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = 3;
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = 2;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = 1;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                        result = 3;
                    }
                }
            }
        }
        return result;
    }
}