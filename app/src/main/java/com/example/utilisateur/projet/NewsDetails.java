package com.example.utilisateur.projet;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NewsDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);
        Bundle b = getIntent().getExtras();
        if(b!=null) {
            String source = (String) b.get("source");
            if(!source.isEmpty()){
                TextView tv = (TextView) findViewById(R.id.source);
                tv.setText("pour "+source);
            }
            String author = (String) b.get("author");
            if(!(author==null||author.isEmpty()||author=="null")){
                TextView tv = (TextView) findViewById(R.id.author);
                tv.setText("par "+ author);
                tv.setVisibility(View.VISIBLE);
            }
            String title =(String) b.get("title");
            if(!title.isEmpty()){
                TextView tv = (TextView) findViewById(R.id.title);
                tv.setText(title);
            }
            String description =(String) b.get("description");
            if(!description.isEmpty()){
                TextView tv = (TextView) findViewById(R.id.description);
                tv.setText(description);
                tv.setVisibility(View.VISIBLE);
            }
            String date =(String) b.get("date");
            if(!date.isEmpty()){
                TextView tv = (TextView) findViewById(R.id.date);
                tv.setText(date);
            }
            final String urlLink =(String) b.get("url");
            if(!urlLink.isEmpty()){
                Button button = (Button) findViewById(R.id.button);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlLink));
                        startActivity(intent);
                    }
                });
            }
        }
    }
}
