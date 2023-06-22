package christine.moldovan.recycle.fragment;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import christine.moldovan.recycle.Article;
import christine.moldovan.recycle.ArticleAdapter;
import christine.moldovan.recycle.R;
import christine.moldovan.recycle.databinding.FragmentHomepageBinding;
import christine.moldovan.recycle.loginscreen.User;

public class HomepageFragment extends Fragment {

    FragmentHomepageBinding binding;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReferenceMarkers, markersReference, usersReference;
    private ArticleAdapter adapter;
    private List<Article> articles = new ArrayList<>();
    private String email;
    private Button addButton;
    private boolean isAdmin = false;

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceMarkers = FirebaseDatabase.getInstance().getReference();
        usersReference = firebaseDatabase.getReference().child("users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle bundle = this.getArguments();
        email = bundle.getString("email");
        System.out.println(email);
        checkIfAdmin();
        View view = inflater.inflate(R.layout.fragment_homepage, container, false);
        binding = FragmentHomepageBinding.inflate(inflater, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        addButton = view.findViewById(R.id.btn_add_article);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ArticleAdapter(articles, getActivity());
        recyclerView.setAdapter(adapter);
        if (isNetworkAvailable() != 0) {
            loadArticles();
            if (isAdmin) {
                addButton.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_LONG).show();
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open AddArticle when the button is clicked
                Bundle bundle = new Bundle();
                Fragment fragment = new AddArticleFragment();
                bundle.putString("email", email);
                fragment.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment).addToBackStack("addArticle").setReorderingAllowed(true).commit();

            }
        });
        return view;
    }

    private void loadArticles() {
        articles.clear();
        FirebaseDatabase.getInstance().getReference("articles")
                .orderByChild("date")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot articleSnapshot : snapshot.getChildren()) {
                            Article article = articleSnapshot.getValue(Article.class);
                            articles.add(0, article);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }


    private void checkIfAdmin() {
        Query query = usersReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    System.out.println("Exists");
                    for (DataSnapshot child : snapshot.getChildren()) {
                        System.out.println("enters");
                        User existingUser = child.getValue(User.class);
                        if (existingUser.getIsAdmin() == true) {
                            isAdmin = true;
                            System.out.println("is admin =" + isAdmin);
                            addButton.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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