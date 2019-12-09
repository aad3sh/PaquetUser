package com.example.paquet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Tag;

import java.util.Objects;

public class Setting extends AppCompatActivity {

    private Button mlogout,submit;
    EditText ed2,ed3,ed4,ed5;
    TextView ed1;
    DatabaseReference rootref,demoref;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ed1=(TextView) findViewById(R.id.editText5);

        ed2=(EditText)findViewById(R.id.editText);
        ed3=(EditText)findViewById(R.id.editText2);
        ed4=(EditText)findViewById(R.id.editText3);

        ed5=(EditText) findViewById(R.id.editText4);
        mAuth=FirebaseAuth.getInstance();
        ed1.setText(mAuth.getCurrentUser().getDisplayName());
        ed4.setText( mAuth.getCurrentUser().getEmail());
        String user_ID= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        rootref= FirebaseDatabase.getInstance().getReference();
        demoref=rootref.child(user_ID);

        submit=(Button)findViewById(R.id.button2);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value1=ed1.getText().toString();
                String value2=ed2.getText().toString();
                String value3=ed3.getText().toString();
                String value4=ed4.getText().toString();
                String value5=ed5.getText().toString();
                demoref.child("User name").setValue(value1);
                demoref.child("Work Address").setValue(value2);
                demoref.child("Home Address").setValue(value3);
                demoref.child("Email").setValue(value4);
                demoref.child("Contact Number").setValue(value5);
                Toast.makeText(Setting.this,"Your details have been stored successfully",Toast.LENGTH_LONG).show();
            }
        });

        mlogout=(Button)findViewById(R.id.button3);
        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(Setting.this,MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}
