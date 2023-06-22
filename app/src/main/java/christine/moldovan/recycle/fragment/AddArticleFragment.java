package christine.moldovan.recycle.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import christine.moldovan.recycle.Article;
import christine.moldovan.recycle.R;
import christine.moldovan.recycle.databinding.FragmentAddArticleBinding;


public class AddArticleFragment extends Fragment {
    FragmentAddArticleBinding binding;
    TextView url, imageUrl, articleTitle;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference articlesRef;

    public static boolean isValidUrl(String url) {
        String regex = "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
        articlesRef = firebaseDatabase.getReference().child("articles");
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
        View v = inflater.inflate(R.layout.fragment_add_article, container, false);
        binding = FragmentAddArticleBinding.inflate(inflater, container, false);

        url = (TextView) v.findViewById(R.id.Url);
        imageUrl = (TextView) v.findViewById(R.id.ImageUrl);
        articleTitle = (TextView) v.findViewById(R.id.ArticleTitle);
        Bundle bundle = this.getArguments();
        String email = bundle.getString("email");
        System.out.println("1%****%");

        binding.Url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.Url.getText().toString().isEmpty()) {
                    binding.UrlLayout.setError("This field cannot be empty");
                } else if (!isValidUrl(binding.Url.getText().toString())) {
                    binding.UrlLayout.setError("Invalid url");
                } else {
                    binding.UrlLayout.setError(null);
                }
            }
        });

        binding.ImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isValidUrl(binding.ImageUrl.getText().toString()) && !binding.ImageUrl.getText().toString().isEmpty()) {
                    binding.ImageUrlLayout.setError("Invalid url");
                } else if (binding.ImageUrl.getText().toString().isEmpty()) {
                    binding.ImageUrlLayout.setError(null);
                } else {
                    binding.ImageUrlLayout.setError(null);
                }
            }
        });
        binding.ArticleTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.ArticleTitle.getText().toString().isEmpty()) {
                    binding.ArticleTitleLayout.setError("This field cannot be empty");
                }
                if (binding.ArticleTitle.getText().length() > 80) {
                    binding.ArticleTitleLayout.setError("Article title is too long");
                } else {
                    binding.ArticleTitleLayout.setError(null);
                }
            }
        });

        binding.SubmitArticleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("%****%");

                if (binding.Url.getText().toString().isEmpty()) {
                    binding.UrlLayout.setError("This field cannot be empty");
                    return;
                }
                if (!isValidUrl(binding.Url.getText().toString())) {
                    binding.UrlLayout.setError("Invalid url");
                    return;
                }
                if (!isValidUrl(binding.ImageUrl.getText().toString()) && !binding.ImageUrl.getText().toString().isEmpty()) {
                    binding.ImageUrlLayout.setError("Invalid url");
                    return;
                }
                if (binding.ArticleTitle.getText().toString().isEmpty()) {
                    binding.ArticleTitleLayout.setError("This field cannot be empty");
                    return;
                }
                if (binding.ArticleTitle.getText().length() > 80) {
                    binding.ArticleTitleLayout.setError("Article title is too long");
                    return;
                }
                if (binding.ImageUrl.getText().toString().isEmpty()) {
                    binding.ImageUrl.setText("https://cdn-icons-png.flaticon.com/512/1208/1208469.png?w=740&t=st=1682849718~exp=1682850318~hmac=5cf7201e1751e70dde18c6d30d6586a9672e2ef0fd50983ab8c70a4e8da3e8ba");
                }
                Bundle bundleEmail = new Bundle();
                bundleEmail.putString("email", email);

                Article article = new Article();
                article.setUrl(binding.Url.getText().toString());
                article.setImageUrl(binding.ImageUrl.getText().toString());
                article.setHeadline(binding.ArticleTitle.getText().toString());
                article.setDate(System.currentTimeMillis());

                firebaseDatabase.getReference().child("articles").push().setValue(article).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getActivity(), "Uploaded article successfully!", Toast.LENGTH_LONG).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error uploading article!", Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
        return binding.getRoot();
    }

}