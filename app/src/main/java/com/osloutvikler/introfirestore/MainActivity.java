package com.osloutvikler.introfirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextTitle;
    private static final String TAG = "Main";
    private EditText editTextThought;
    private Button buttonSave;
    private TextView textViewRecTitle;
    private TextView textViewRecThought;
    private Button buttonShowData, buttonUpdate, buttonDeleteThought;

    //Keys, Remember firebase is a key value pairs, the words keys below put the data into keys
    public static final String KEY_TITLE= "title";
    public static final String KEY_THOUGHT= "thought";

    // Access a Cloud Firestore instance from your Activity to enable the creation of a database as shown in the code after the one below
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //This declares the path to the db/document in firebase firestore
    private DocumentReference journalRef = db.collection("Journal")
            .document("First Thoughts");

    //This code is used for creating a collection reference which enables one to reference any document
    // in the collection and not just a specific document as given in the ablove code
    private CollectionReference collectionReference = db.collection("Journal")


    //This cold can also be used to connect and create a document
    //private DocumentReference journalRef = db.document("Journal/First Thought");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSave=findViewById(R.id.button_save);
        editTextTitle=findViewById(R.id.edit_text_title);
        editTextThought=findViewById(R.id.edit_text_thoughts);
        textViewRecTitle=findViewById(R.id.text_view_rec_title);
        textViewRecThought=findViewById(R.id.textView_rec_thought);
        buttonShowData=findViewById(R.id.button_show_data);
         buttonUpdate=findViewById(R.id.button_update);
         buttonDeleteThought=findViewById(R.id.button_delete);

         buttonDeleteThought.setOnClickListener(this);
         buttonUpdate.setOnClickListener(this);

        buttonShowData.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                journalRef.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>(){
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot){
                                if(documentSnapshot.exists()){
                                    //get or retrieve the data from the db is done through the documentSnapshot object
                                    // above and through the keys define on top of the class
                                    //String title = documentSnapshot.getString(KEY_TITLE);
                                    //String thought =documentSnapshot.getString(KEY_THOUGHT);

                                    //This code is mapping out documentSnapshot to the journal class
                                    Journal journal = documentSnapshot.toObject(Journal.class);

                                    //We the call our textView to show the results
                                    textViewRecTitle.setText(journal.getTitle());
                                    textViewRecThought.setText(journal.getThought());
                                }

                                else{
                                    Toast.makeText(MainActivity.this, "No data exists", Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener(){
                            @Override
                            public void onFailure(@Nullable Exception e){
                                Log.d(TAG, "onFailure: " + e.toString());
                            }
                        });


            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Getting the input from the input screen
                String title= editTextTitle.getText().toString().trim();
                String thought = editTextThought.getText().toString().trim();

                //Create an object of the data class and set the data using the setter methods declared in your class
                Journal journal = new Journal();
                journal.setTitle(title);
                journal.setThought(thought);


                //Create columns or rather structure
                //Map<String, Object> data = new HashMap<>();
                //data.put(KEY_TITLE, title);
                //data.put(KEY_THOUGHT, thought);

                //Below we use the set() because we are adding data into the db/document
                journalRef.set(journal)
                        .addOnSuccessListener(new OnSuccessListener<Void>(){
                            @Override
                            public void onSuccess(Void aVoid){
                                Toast.makeText(MainActivity.this,"Success",Toast.LENGTH_LONG);

                            }

                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.toString());

                            }
                        });

            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        //The word this in this code below attached the snapshot to this page and allows it to automatically close when you leave this main or the snapshot
        journalRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>(){

            //This code listens to changes and displays new update, displays what ever you have entered, deletes the previous one
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    //you can also put a return; here if you have challenges as in this halts here
                }

                if(value != null && value.exists()){

                    //String title = value.getString(KEY_TITLE);
                    //String thought = value.getString(KEY_THOUGHT);

                    //We the call our textView to show the results
                    //textViewRecTitle.setText(title);
                    //textViewRecThought.setText(thought);

                    Journal journal = value.toObject(Journal.class);

                    //We the call our textView to show the results
                    textViewRecTitle.setText(journal.getTitle());
                    textViewRecThought.setText(journal.getThought());
                }
                else{
                    textViewRecThought.setText("");
                    textViewRecTitle.setText("");
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_update:
                updateTitle();
                break;

            case R.id.button_delete:
                //deleteThought();deletes only a thought and not its title
                deleteAll();
            break;
        }

    }

    //Deletes thought and title, the entire document
    private void deleteAll() {
        journalRef.delete();
    }

    //This method deletes a thought only
    private void deleteThought() {
        //Map<String, Object> data = new HaspMap<>();
        //data.put(KEY_THOUGHT, FieldValue.delete());
        //journalRef.update(data);
        journalRef.update(KEY_THOUGHT, FieldValue.delete());
    }

    private void updateTitle() {
        String title = editTextTitle.getText().toString().trim();
        String thought=editTextThought.getText().toString().trim();
        Map<String, Object> data = new HashMap<>();
        data.put(KEY_TITLE, title);
        data.put(KEY_THOUGHT, thought);

        journalRef.update(data).addOnSuccessListener(new OnSuccessListener<Void>(){

            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "Updated", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@Nullable Exception e){
                Log.d(TAG, "onFailure: " + e.toString());
            }
        });
    }
}