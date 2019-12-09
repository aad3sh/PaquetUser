package com.example.paquet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

public class YourOrders extends AppCompatActivity {

    DatabaseReference rootref,demoref;
    private FirebaseAuth mAuth;
    private List<String> namesList = new ArrayList<>();
    ListView listView;
    Integer i;

    @android.support.annotation.Nullable
    List<Map<String, Object>> orders;

    private String TAG = "YourOrders";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_orders);

        listView = (ListView) findViewById(R.id.dynamic) ;
        orders = new ArrayList<Map<String, Object>>();
        mAuth=FirebaseAuth.getInstance();

        Toast.makeText(YourOrders.this, "HERE!", Toast.LENGTH_LONG).show();

        String user_ID= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        GetAllOrders();
        //PopulateOrders();

    }

    private void GetAllOrders(){
        final FirebaseFirestore db= FirebaseFirestore.getInstance();

        CollectionReference root = db.collection("AppRoot");

        //TODO:get user id and put in place of orders
        DocumentReference dr = root.document("User");

        CollectionReference userIDCollection = dr.collection(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        dr = userIDCollection.document("Orders");

        dr.collection("AllOrders").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                Log.d(TAG, document.getId() + " => " + data);
                                orders.add(data);
                            }
                            PopulateOrders();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void PopulateOrders(){
        //ArrayAdapter<Map<String, Object>> adapter=new ArrayAdapter<Map<String, Object>>(getApplicationContext(),android.R.layout.simple_list_item_1, orders);
        Map<String, Object> mapOrders = new HashMap<String, Object>();
        for (int i=0; i<orders.size(); i++){
            mapOrders.put(orders.get(i).get("name").toString(), orders.get(i));
        }

        MyAdaptor adapter = new MyAdaptor(mapOrders);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
}
