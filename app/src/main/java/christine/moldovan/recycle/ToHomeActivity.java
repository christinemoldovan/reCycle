package christine.moldovan.recycle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class ToHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_home);
        Intent intent = getIntent();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(ToHomeActivity.this, HomepageActivity.class);
                homeIntent.putExtra("email", intent.getStringExtra("email"));
                startActivity(homeIntent);
                finish();
            }
        }, 1000);
    }
}