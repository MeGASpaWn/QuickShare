package com.megaspawn.quickshare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.megaspawn.quickshare.auth.LoginActivity;
import com.megaspawn.quickshare.util.ContentAdapter;
import com.megaspawn.quickshare.util.Item;

import java.util.ArrayList;
import java.util.List;


public class ContentActivity extends AppCompatActivity {

    Context context;

    FirebaseDatabase database;
    DatabaseReference dbRef;
    private StorageReference mStorageRef;

    private FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authListener;

    EditText text;
    ImageButton sendText;

    private List<Item> itemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ContentAdapter mAdapter;

    static final String TAG = "ContentActivity";
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        text = (EditText) findViewById(R.id.editText);
        sendText = (ImageButton) findViewById(R.id.sendText);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        context = getApplicationContext();

        authenticateUser();
        initializeRealtimeDb();
        initializeUI();
    }

    private void initializeRealtimeDb() {

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("test");

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                String msg = dataSnapshot.getValue(String.class);
                itemList.add(new Item(key, msg));
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        };
        dbRef.addChildEventListener(childEventListener);
    }

    private void logContentShared(String type, int length) {

        Bundle params = new Bundle();
        params.putString(type + "_length", "" + length);
        mFirebaseAnalytics.logEvent("share_" + type, params);
    }

    private void authenticateUser() {

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "User is logged in.");
        }

        // this listener will be called when there is change in firebase user session
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(ContentActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
    }

    private void initializeUI() {

        mAdapter = new ContentAdapter(itemList, new ContentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
                Log.d(TAG, "Clicked: " + item.getText());
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int position = viewHolder.getAdapterPosition();
                Item item = itemList.get(itemList.size() - position -1);
                String txt = item.getText();

                Log.d(TAG, "Swiped: pos=" + position + ", msg=" + txt);

                dbRef.child(item.getKey()).removeValue();
                itemList.remove(itemList.size() - position -1);
                mAdapter.notifyItemRemoved(position);

                Snackbar.make(recyclerView, "Item deleted permanently", Snackbar.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);

        sendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = text.getText().toString();
                dbRef.push().setValue(str);
                text.setText("");
                logContentShared("text", str.length());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_signout:
                Log.d(TAG, "Signing out user");
                auth.signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
