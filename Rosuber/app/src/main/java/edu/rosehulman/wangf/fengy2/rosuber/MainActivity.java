package edu.rosehulman.wangf.fengy2.rosuber;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import edu.rosehulman.rosefire.Rosefire;
import edu.rosehulman.rosefire.RosefireResult;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.AboutFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.InsertTripFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.LoginFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.MapFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.MyTripContactFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.ProfileFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripDetailFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripHistoryFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripListFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TripHistoryFragment.TripHistoryCallback,
        TripListFragment.TripListCallback, LoginFragment.OnLoginListener, TripDetailFragment.OnJoinListener,
        ProfileFragment.ProfileUpdateListener, MyTripContactFragment.OnContactListener,
        InsertTripFragment.InsertTripCallBack, AboutFragment.AboutCallback {

    private final static String PREFS = "PREFS";
    private static final int RC_ROSEFIRE_LOGIN = 1;
    private static final int RC_SELECT_IMAGE = 2;
    private static final String KEY_USER_NAME = "USER_NAME";
    private static final String KEY_USER_KEY = "USER_KEY";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";
    private static final String KEY_USER_PHONE = "USER_PHONE";
    FloatingActionButton mFab;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    OnCompleteListener mOnCompleteListener;
    Toolbar mToolbar;
    User currentUser;
    TripListFragment mTripListFragment;
    TripHistoryFragment mTripHistoryFragment;
    TextView navNameTextView;
    TextView navContactInfoTextView;
    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://rosuber-android.appspot.com").child("images");
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
    DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference().child("trips");
    ImageView navProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //custom font supports
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Philosopher-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mFab = fab;
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToInsertTripFragment(false, null);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        currentUser = new User();
        currentUser.setKey(prefs.getString(KEY_USER_KEY, null));
        currentUser.setEmail(prefs.getString(KEY_USER_EMAIL, null));
        currentUser.setName(prefs.getString(KEY_USER_NAME, null));
        currentUser.setPhoneNumber(prefs.getLong(KEY_USER_PHONE, 0));

        mAuth = FirebaseAuth.getInstance();
        initializeListeners();

        final ImageView profileImageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image);
        navProfileImageView = profileImageView;

//        profileImagePath = finalLocalFile.getPath();
        navNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_name_text);
        navNameTextView.setText(currentUser.getName());
        navContactInfoTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_contact_info_text);
        navContactInfoTextView.setText(currentUser.getEmail());
        if (currentUser.getKey() != null) {
            loadProfileImage();
        }

    }

    private void switchToInsertTripFragment(boolean isEdit, Trip trip) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        InsertTripFragment fragment = new InsertTripFragment();
        Bundle args = new Bundle();
        args.putString(Constants.USER, currentUser.getKey());
        args.putBoolean(Constants.EDIT, isEdit);
        args.putParcelable(Constants.TRIP, trip);
        fragment.setArguments(args);

        Slide slideTransition = new Slide(Gravity.RIGHT);
        slideTransition.setDuration(200);
        fragment.setEnterTransition(slideTransition);

        ft.replace(R.id.fragment_container, fragment);
        ft.addToBackStack("insert trip");
        ft.commit();
    }

    private void initializeListeners() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.d(Constants.TAG, "USER: " + user);
                if (user != null) {
                    switchToHistoryFragment(true);
                } else {
                    switchToLoginFragment();
                }
            }
        };
        mOnCompleteListener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    showLoginError("Login failed");
                }
            }
        };
    }

    public void joinTripConfirmDialog(final Trip trip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.join_trip_dialog_title);
        if ((trip.getDriverKey() != null && trip.getDriverKey().equals(currentUser.getKey())) || (trip.getPassengerKey() != null && trip.getPassengerKey().containsKey(currentUser.getKey()))) {
            builder.setTitle("You are already in this trip!");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.create().show();
            return;
        }

        long capacity = trip.getCapacity();
        final int seatsLeft = (int) capacity - trip.getPassengerKey().size();
        if (trip.getDriverKey() == null) {
            if (seatsLeft <= 0) {
                String[] sel = new String[]{"Driver"};
                builder.setSingleChoiceItems(sel, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        trip.setDriverKey(currentUser.getKey());

                    }
                });
            } else {
                String[] selections = new String[]{"Passenger", "Driver"};
                builder.setSingleChoiceItems(selections, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 1) {
                            trip.setDriverKey(currentUser.getKey());
                            trip.getPassengerKey().remove(currentUser.getKey());
                        } else {
                            trip.setDriverKey(null);
                            trip.addPassenger(currentUser.getKey());
                        }
                    }
                });
            }
        } else {
            if (seatsLeft <= 0) {
                builder.setMessage(R.string.join_dialog_msg_full_trip);
                builder.setTitle(R.string.join_dialog_msg_full_trip_title);
            } else {
                String[] selection = new String[]{"Passenger"};
                builder.setSingleChoiceItems(selection, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        trip.addPassenger(currentUser.getKey());
                    }
                });
            }
        }


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mTripListFragment.getAdapter().editTrip(trip);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, mTripListFragment);
                ft.addToBackStack("list");
                ft.commit();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    private void switchToHistoryFragment(boolean isInitializing) {
        mFab.setVisibility(View.GONE);
        mTripHistoryFragment = new TripHistoryFragment();
        Bundle arg = new Bundle();
        arg.putParcelable(Constants.USER, currentUser);
        mTripHistoryFragment.setArguments(arg);

        if (isInitializing) {
            userRef.child(currentUser.getKey()).child("phoneNumber").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long phone = dataSnapshot.getValue(Long.class);
                    if (phone == null || phone == 0) {
                        showAddPhoneNumberDialog();
                    }
                    currentUser.setPhoneNumber(phone);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        setNavInfo();
        loadProfileImage();
        mToolbar.setVisibility(View.VISIBLE);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mTripHistoryFragment, "myTrips");
        ft.commit();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_map_search).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            mAuth.signOut();
            currentUser = null;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mToolbar.setVisibility(View.VISIBLE);
        Fragment switchTo = null;
        String backStack = "";
        switch (item.getItemId()) {
            case R.id.nav_profile:
                ProfileFragment profileFragment = new ProfileFragment();
                Bundle args = new Bundle();
                args.putParcelable(Constants.USER, currentUser);
                profileFragment.setArguments(args);
                switchTo = profileFragment;
                backStack = "profile";
                break;
            case R.id.nav_find_trips:
                mTripListFragment = new TripListFragment();
                switchTo = mTripListFragment;
                backStack = "trips";
                break;
            case R.id.nav_trip_history:
                mTripHistoryFragment = new TripHistoryFragment();
                Bundle arg = new Bundle();
                arg.putParcelable(Constants.USER, currentUser);
                mTripHistoryFragment.setArguments(arg);
                switchTo = mTripHistoryFragment;
                backStack = "history";
                break;
            case R.id.nav_about:
                switchTo = new AboutFragment();
                backStack = "about";
                break;
        }

        if (switchTo != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, switchTo);
//            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
//                getSupportFragmentManager().popBackStackImmediate();
//            }
            ft.addToBackStack(backStack);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onTripSelected(Trip trip) {
        mToolbar.setVisibility(View.VISIBLE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        TripDetailFragment fragment = TripDetailFragment.newInstance(trip);

        Slide slideTransition = new Slide(Gravity.RIGHT);
        slideTransition.setDuration(100);
        fragment.setEnterTransition(slideTransition);

        ft.replace(R.id.fragment_container, fragment);
        ft.addToBackStack("detail");
        ft.commit();
    }

    @Override
    public void onTripSearch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_search, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.search_dialog_title));

        final EditText originEditText = (EditText) view.findViewById(R.id.search_origin);
        final EditText destEditText = (EditText) view.findViewById(R.id.search_dest);


        builder.setNeutralButton(getString(R.string.search_dialog_netural), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mTripListFragment.getAdapter().toggle(null, null);
            }
        });

        final Activity activity = this;

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String origin = originEditText.getText().toString();
                String dest = destEditText.getText().toString();
                if (origin.isEmpty() || dest.isEmpty()) {
                    Toast.makeText(activity, getString(R.string.invalid_search_toast),
                            Toast.LENGTH_LONG).show();
                } else {
                    mTripListFragment.getAdapter().toggle(origin, dest);
                }
            }
        });
        builder.create().show();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ROSEFIRE_LOGIN) {
            RosefireResult result = Rosefire.getSignInResultFromIntent(data);
            if (result.isSuccessful()) {
                mAuth.signInWithCustomToken(result.getToken())
                        .addOnCompleteListener(this, mOnCompleteListener);
                userRef.keepSynced(true);
                currentUser = new User();
                currentUser.setKey(result.getUsername());
                currentUser.setName(result.getName());
                currentUser.setEmail(result.getEmail());
                setNavInfo();
            } else {
                showLoginError("Rosefire sign-in error");
            }
        } else if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                Log.d(Constants.TAG, "onActivityResult: image selection");
                return;
            }
            try {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                StorageReference imageUserRef = imageRef.child(currentUser.getKey());
                UploadTask uploadTask = imageUserRef.putStream(inputStream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("upload image", "onSuccess: " + downloadUrl);
                        switchToProfileFragment();
                    }
                });

                loadProfileImage();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadProfileImage() {
        StorageReference profileImageRef = imageRef.child(currentUser.getKey());

        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(MainActivity.this).load(uri.toString()).into(navProfileImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("image load", "onFailure: ");
            }
        });

    }

    private void showAddPhoneNumberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.phone_dialog_title);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_phone, null);
        builder.setView(view);
        final EditText phoneEditText = (EditText) view.findViewById(R.id.editText_phone_number);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentUser.setPhoneNumber(Long.valueOf(phoneEditText.getText().toString()));
                Log.d("phone number", "onClick: " + currentUser.getKey());
                userRef.child(currentUser.getKey()).setValue(currentUser);
            }
        });
        builder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
//        userRef.removeEventListener();
    }

    @Override
    public void onRosefireLogin() {
        mToolbar.setVisibility(View.GONE);
        mFab.setVisibility(View.GONE);
        Intent signInIntent = Rosefire.getSignInIntent(this, getString(R.string.rosefireKey));
        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN);
    }

    private void switchToLoginFragment() {
        mToolbar.setVisibility(View.GONE);
        mFab.setVisibility(View.GONE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new LoginFragment());
        ft.addToBackStack("login");
        ft.commit();
    }

    private void switchToProfileFragment() {
        loadProfileImage();
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.USER, currentUser);
        profileFragment.setArguments(args);
        mFab.setVisibility(View.GONE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, profileFragment);
        ft.addToBackStack("profile");
        ft.commit();
    }

    private void setNavInfo() {
        navContactInfoTextView.setText(currentUser.getEmail());
        navNameTextView.setText(currentUser.getName());
    }

    private void showLoginError(String message) {
        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("Login");
        loginFragment.onLoginError(message);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (currentUser != null) {
            editor.putString(KEY_USER_KEY, currentUser.getKey());
            editor.putString(KEY_USER_EMAIL, currentUser.getEmail());
            editor.putString(KEY_USER_NAME, currentUser.getName());
//        if(currentUser.getPhoneNumber()!=null){
            editor.putLong(KEY_USER_PHONE, currentUser.getPhoneNumber());
        }

        // Put the other fields into the editor
        editor.commit();
    }

    @Override
    public void onJoinButtonPressed(Trip trip) {
        joinTripConfirmDialog(trip);
    }

    @Override
    public void onImageButtonPressed() {
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String pickTitle = getString(R.string.image_intent_title); // Or get from strings.xml
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        chooserIntent.putExtra
                (
                        Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[]{takePhotoIntent}
                );

        Log.d("image", "onImageButtonPressed: ");
        startActivityForResult(chooserIntent, RC_SELECT_IMAGE);
    }

    @Override
    public void onEditButtonPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_profile_dialog_title);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);
        final EditText phoneEditText = (EditText) view.findViewById(R.id.edit_profile_phone_number);
        phoneEditText.setText(currentUser.getPhoneNumber() + "");
        builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentUser.setPhoneNumber(Long.valueOf(phoneEditText.getText().toString()));
//                Log.d("phone number", "onClick: "+currentUser.getKey());
                userRef.child(currentUser.getKey()).setValue(currentUser);
                switchToProfileFragment();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void onEditTripClicked(Trip trip) {
        switchToInsertTripFragment(true, trip);
    }

    @Override
    public void onContactInfoButtonClicked(String driverKey, Map<String, Boolean> passengerKeys) {
        MyTripContactFragment myTripContactFragment = new MyTripContactFragment();
        Bundle args = new Bundle();
        args.putString(Constants.DRIVER_KEY, driverKey);
        args.putStringArray(Constants.PASSENGER_KEYS, (passengerKeys.keySet()).toArray(new String[passengerKeys.size()]));
        myTripContactFragment.setArguments(args);
        mFab.setVisibility(View.GONE);

        Slide slideTransition = new Slide(Gravity.RIGHT);
        slideTransition.setDuration(200);
        myTripContactFragment.setEnterTransition(slideTransition);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, myTripContactFragment);
        ft.addToBackStack("contactInfo");
        ft.commit();
    }

    @Override
    public void onMessage(String receiver) {
        Activity activity = this;
        if (receiver == null || receiver.isEmpty() || receiver.equals("N/A")) {
            Toast.makeText(activity, getString(R.string.invalid_phone_number),
                    Toast.LENGTH_LONG).show();
        } else {
//            Intent smsIntent = new Intent(Intent.ACTION_SEND);
////            smsIntent.setType("vnd.android-dir/mms-sms");
////            smsIntent.putExtra("address", receiver);
//            smsIntent.setData(Uri.parse("smsto:"+receiver));
//            startActivity(smsIntent);
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.setData(Uri.parse("sms:" + receiver));
            startActivity(smsIntent);
        }
    }

    @Override
    public void onEmail(String receiver, boolean toDevelopers) {
        email(receiver, toDevelopers);
    }

    @Override
    public void onEmailToDevelopers() {
        email(Constants.EMAIL, true);
    }

    private void email(String receiver, boolean toDevelopers) {
        Activity activity = this;
        if (receiver == null || receiver.isEmpty() || receiver.equals("N/A")) {
            Toast.makeText(activity, getString(R.string.invalid_email),
                    Toast.LENGTH_LONG).show();
        } else {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            String uriText = "mailto:";
            if (toDevelopers) {
                uriText += Uri.encode("wangf@rose-hulman.edu") + "?cc=" +
                        Uri.encode("fengy2@rose-hulman.edu") +
                        "&subject=" + Uri.encode(Constants.TAG);
            } else {
                uriText += Uri.encode(receiver) +
                        "?subject=" + Uri.encode(Constants.TAG);
            }
            Uri uri = Uri.parse(uriText);
            emailIntent.setData(uri);
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
            }
        }
    }


    @Override
    public void insertTripPressed(Trip trip, boolean isEdit, String origin, String destination, String time, Long price, long capacity, boolean isDriverChecked) {
        if (isEdit) {
            trip.setOrigin(origin);
            trip.setDestination(destination);
            trip.setTime(time);
            trip.setPrice(price);
            trip.setCapacity(capacity);
            mTripHistoryFragment.getAdapter().editTrip(trip);
            switchToHistoryFragment(false);
        } else {
            Trip newTrip = new Trip();
            if (isDriverChecked) {
                newTrip.setDriverKey(currentUser.getKey());
            } else {
                newTrip.addPassenger(currentUser.getKey());
            }
            newTrip.setOrigin(origin);
            newTrip.setDestination(destination);
            newTrip.setTime(time);
            newTrip.setPrice(price);
            newTrip.setCapacity(capacity);
            mTripListFragment.getAdapter().addTrip(newTrip);
            switchToHistoryFragment(false);
        }
    }

    @Override
    public void leaveTripPressed(Trip trip) {
        leaveTripConfirmDialog(trip);
    }

    private void leaveTripConfirmDialog(final Trip trip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.leave_trip_dialog_title);
        builder.setMessage(R.string.leave_trip_dialog_msg);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTripHistoryFragment.getAdapter().editTrip(trip);
                switchToHistoryFragment(false);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }


    @Override
    public void cancelInsertTripButtonPressed(boolean idEdit) {
        if (idEdit) {
            switchToHistoryFragment(false);
        } else {
            switchToTripListFragment();
        }
    }

    private void switchToTripListFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        Slide slideTransition = new Slide(Gravity.RIGHT);
//        slideTransition.setDuration(200);
//        mTripHistoryFragment.setEnterTransition(slideTransition);
        ft.replace(R.id.fragment_container, mTripListFragment);
//        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
//            getSupportFragmentManager().popBackStackImmediate();
//        }
        ft.addToBackStack("trips");
        ft.commit();
    }
}
