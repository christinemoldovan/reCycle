package christine.moldovan.recycle.loginscreen;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import christine.moldovan.recycle.R;

public class RegisterActivity extends AppCompatActivity {
    private static final String USERS = "users";
    private TextInputEditText nameEditText, passwordEditText;
    private TextInputEditText phoneEditText, emailEditText;
    private TextInputLayout emailLayout, passwordLayout, nameLayout, phoneLayout;
    private Button registerButton, backToLoginButton;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private String TAG = "RegisterActivity";
    private String name, email, phone, username;
    private String password;
    private User user;
    private FirebaseAuth mAuth;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.email_editText);
        passwordEditText = findViewById(R.id.password_editText);
        nameEditText = findViewById(R.id.name_editText);
        phoneEditText = findViewById(R.id.phone_editText);

        emailLayout = findViewById(R.id.email_Layout);
        passwordLayout = findViewById(R.id.passwordRegister_Layout);
        nameLayout = findViewById(R.id.name_Layout);
        phoneLayout = findViewById(R.id.phone_Layout);

        registerButton = findViewById(R.id.register_button);
        backToLoginButton = findViewById(R.id.backToLogin_button);

        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USERS);
        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Insert data into firebase database
                System.out.println(emailEditText.getText().toString());
                System.out.println(passwordEditText.getText().toString());
                if (!(emailEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty())) {
                    name = nameEditText.getText().toString();
                    email = emailEditText.getText().toString();
                    phone = phoneEditText.getText().toString();
                    password = passwordEditText.getText().toString();
                    user = new User(name, email, phone, false);
                    registerUser();
                } else if (emailEditText.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Email cannot be empty!",
                            Toast.LENGTH_SHORT).show();
                } else if (passwordEditText.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Password cannot be empty!",
                            Toast.LENGTH_SHORT).show();
                } else if (emailEditText.getText().toString().isEmpty() && passwordEditText.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Email and password cannot be empty!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordLayout.setError(null);
            }
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                emailLayout.setError(null);
            }
        });
    }

    public void registerUser() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            if (isNetworkAvailable()) {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed. ",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Please check your internet connection!",
                                        Toast.LENGTH_SHORT).show();
                            }
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                passwordLayout.setError(getString(R.string.invalid_password));
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                emailLayout.setError(getString(R.string.invalid_email));
                                emailEditText.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                emailLayout.setError(getString(R.string.existing_email));
                                emailEditText.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                });
    }

    /**
     * Adding user information to database and redirect to login screen
     *
     * @param currentUser
     */
    public void updateUI(FirebaseUser currentUser) {
        String keyId = mDatabase.push().getKey();
        mDatabase.child(keyId).setValue(user); //adding user info to database
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
