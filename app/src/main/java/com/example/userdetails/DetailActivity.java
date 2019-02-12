package com.example.userdetails;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.userdetails.model.Results;

public class DetailActivity extends AppCompatActivity {

    Results clicked;
    TextView textView;
    ImageView imageView;
    TextView ageView;
    TextView greeting;
    TextView location;
    TextView phone;
    TextView cell;
    TextView email;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = this.getIntent();
        clicked = intent.getParcelableExtra("clicked");
        clicked.getName();

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle("Random Friends");

        //Setup layout
        fab = findViewById(R.id.addFriend);
        textView = findViewById(R.id.detailNameView);
        imageView = findViewById(R.id.detailImageView);
        ageView = findViewById(R.id.detailAgeView);
        greeting = findViewById(R.id.greeting);
        phone = findViewById(R.id.phoneNumber);
        cell = findViewById(R.id.cellNumber);
        location = findViewById(R.id.location);
        email = findViewById(R.id.email);

        //Setup content
        textView.setText(new StringBuilder()
                .append(Utils.caps(clicked.getName().getFirst())).append(" ")
                .append(Utils.capsMulti(clicked.getName().getLast())).toString());
        ageView.setText(new StringBuilder().append("Age: ")
                .append(clicked.getDob().getAge().toString()).toString());
        greeting.setText(new StringBuilder()
                .append("Hi! I'm ")
                .append(Utils.caps(clicked.getName().getFirst() + "!")).toString());
        phone.setText(new StringBuilder()
                .append("Home: ").append(clicked.getPhone()).toString());
        cell.setText(new StringBuilder()
                .append("Cell: ").append(clicked.getCell()).toString());
        email.setText(new StringBuilder()
                .append("E-mail: ").append(clicked.getEmail()).toString());
        location.setText(new StringBuilder()
                .append("City: ").append(Utils.capsMulti(clicked.getLocation().getCity())).toString());
        new DownloadImageTask(imageView).execute(clicked.getPicture().getMedium());

        fab.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

                //Adding data to contact editor

                //Email
                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, clicked.getEmail())
                //Home phone
                        .putExtra(ContactsContract.Intents.Insert.PHONE, clicked.getPhone())
                        .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                //Mobile Phone
                        .putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, clicked.getCell())
                        .putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                //Name
                        .putExtra(ContactsContract.Intents.Insert.NAME, textView.getText())
                //Navigation
                        .putExtra("finishActivityOnSaveCompleted", true);
                startActivity(intent);
            }
        });
    }

}


