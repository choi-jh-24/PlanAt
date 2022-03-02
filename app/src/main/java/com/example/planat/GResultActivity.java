package com.example.planat;
        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.appcompat.app.AppCompatActivity;

        import com.bumptech.glide.Glide;

public class GResultActivity extends AppCompatActivity {

    private TextView tv_gName;
    private ImageView ic_launcher;
    private TextView textAppbarUser;
    private ImageView imageUser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        String nickName = intent.getStringExtra("nickName");
        String photoURL = intent.getStringExtra("photoURL");

        tv_gName = findViewById(R.id.tv_gName);
        tv_gName.setText(nickName);

        ic_launcher = findViewById(R.id.ic_launcher);
        Glide.with(this).load(photoURL).into(ic_launcher);

        textAppbarUser = findViewById(R.id.tv_name);
        imageUser = findViewById(R.id.iv_photo);
        textAppbarUser.setText(nickName);
        Glide.with(this)
                .load(photoURL)
                .error(R.drawable.ico_default_image)
                .circleCrop()
                .into(imageUser);

        imageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GResultActivity.this, MyPageActivity.class);
                startActivity(intent);
            }
        });
    }



}
