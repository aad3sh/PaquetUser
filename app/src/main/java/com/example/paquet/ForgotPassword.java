package com.example.paquet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText passwordEmail;
    private Button resetpassword;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        passwordEmail=(EditText)findViewById(R.id.email_reset);
        resetpassword=(Button)findViewById(R.id.reset);
        firebaseAuth = FirebaseAuth.getInstance();

        resetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail=passwordEmail.getText().toString().trim();

                if(useremail.equalsIgnoreCase(""))
                {
                    Toast.makeText(ForgotPassword.this,"Please enter correct E-mail ID.",Toast.LENGTH_LONG).show();
                }
                else
                {
                    firebaseAuth.sendPasswordResetEmail(useremail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(ForgotPassword.this,"An E-mail has been sent to Reset Your Password!",Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(new Intent(ForgotPassword.this,MainActivity.class));
                            }else{
                                Toast.makeText(ForgotPassword.this,"Something went wrong, E-mail not sent.",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
