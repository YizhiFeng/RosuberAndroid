package edu.rosehulman.wangf.fengy2.rosuber;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Calendar;

import edu.rosehulman.rosefire.Rosefire;
import edu.rosehulman.rosefire.RosefireResult;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.AboutFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.HomePageFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.LoginFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.ProfileFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripDetailFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripHistoryFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripListFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TripListFragment.TripListCallback, LoginFragment.OnLoginListener, TripDetailFragment.OnJoinListener, ProfileFragment.ProfileUpdateListener {

    private final static String PREFS = "PREFS";
    private static final int RC_ROSEFIRE_LOGIN = 1;
    private static final int RC_SELECT_IMAGE = 2;
    private static final String KEY_USER_NAME = "USER_NAME";
    private static final String KEY_USER_KEY= "USER_KEY";
    private static final String KEY_USER_EMAIL= "USER_EMAIL";
    private static final String KEY_USER_PHONE= "USER_PHONE";
    FloatingActionButton mFab;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    OnCompleteListener mOnCompleteListener;
    Toolbar mToolbar;
    User currentUser;
    TripListFragment mTripListFragment;
    TextView navNameTextView;
    TextView navContactInfoTextView;
    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://rosuber-android.appspot.com").child("images");
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
    ImageView navProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                addEditTripDialog(false, null);
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
        currentUser.setKey(prefs.getString(KEY_USER_KEY,null));
        currentUser.setEmail(prefs.getString(KEY_USER_EMAIL,null));
        currentUser.setName(prefs.getString(KEY_USER_NAME,null));
        currentUser.setPhoneNumber(prefs.getLong(KEY_USER_PHONE,0));

        mAuth = FirebaseAuth.getInstance();
        initializeListeners();

        final ImageView profileImageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image);
        navProfileImageView = profileImageView;

//        profileImagePath = finalLocalFile.getPath();
        navNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_name_text);
        navNameTextView.setText(currentUser.getName());
        navContactInfoTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_contact_info_text);
        navContactInfoTextView.setText(currentUser.getEmail());

        loadProfileImage();
    }

    private void initializeListeners() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.d(Constants.TAG, "USER: " + user);
                if (user != null) {
                    switchToHomeFragment("users/" + user.getUid());
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

    public void joinTripConfirmDialog(final Trip trip){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join This Trip As The Following Role");
        long capacity = trip.getCapacity();

        if(trip.getDriverKey()==null){
            String[] selections = new String[]{"Passenger","Driver"};
            builder.setSingleChoiceItems(selections, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                trip.setPassengerKey(mAuth.getCurrentUser().getUid());
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

    public void addEditTripDialog(final boolean isEditing, final Trip trip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add, null);
        builder.setView(view);
        builder.setTitle(R.string.add_a_trip);
        final Switch isDriverSwitch = (Switch) view.findViewById(R.id.isDriverSwitch);
        final TextView isDriverTextView = (TextView) view.findViewById(R.id.isDriverTextView);
        final EditText originEditText = (EditText) view.findViewById(R.id.orginEditText);
        final EditText destEditText = (EditText) view.findViewById(R.id.destEditText);
        Button dateButton = (Button) view.findViewById(R.id.dateButton);
        final TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        Button timeButton = (Button) view.findViewById(R.id.timeButton);
        final TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
        final SeekBar numPassengerSBar = (SeekBar) view.findViewById(R.id.seek_bar);
        final TextView capacityTextView = (TextView)view.findViewById(R.id.capacityTextView);
        final EditText priceEditText = (EditText) view.findViewById(R.id.priceEditText);

        isDriverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    isDriverTextView.setText(R.string.i_am_a_driver);
                } else {
                    isDriverTextView.setText(R.string.i_am_a_passenger);
                }
            }
        });

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mDatePicker;
                mDatePicker = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        selectedmonth = selectedmonth + 1;
                        dateTextView.setText(new DecimalFormat("00").format(selectedday) + "/" + new DecimalFormat("00").format(selectedmonth) + "/" + selectedyear);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Date");
                mDatePicker.show();
            }
        });

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timeTextView.setText(new DecimalFormat("00").format(selectedHour) + ":" + new DecimalFormat("00").format(selectedMinute));
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        numPassengerSBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                capacityTextView.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        String posButtonText = "ADD";
        if (isEditing) {
            posButtonText = "UPDATE";
        }

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(posButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isEditing) {
                    trip.setOrigin(originEditText.getText().toString());
                    trip.setDestination(destEditText.getText().toString());
                    trip.setTime(dateTextView.getText().toString()+" "+timeTextView.getText().toString());
                    trip.setPrice(Long.parseLong(priceEditText.getText().toString()));
                    trip.setCapacity(numPassengerSBar.getProgress());
                    mTripListFragment.getAdapter().editTrip(trip);

                } else {
                    Trip newTrip = new Trip();
                    if(isDriverSwitch.isChecked()){
                        newTrip.setDriverKey(currentUser.getKey());
                    }else{
                        newTrip.setPassengerKey(currentUser.getKey());
                    }
                    newTrip.setOrigin(originEditText.getText().toString());
                    newTrip.setDestination(destEditText.getText().toString());
                    newTrip.setTime(dateTextView.getText().toString()+" "+timeTextView.getText().toString());
                    newTrip.setPrice(Long.parseLong(priceEditText.getText().toString()));
                    newTrip.setCapacity(numPassengerSBar.getProgress());
                    mTripListFragment.getAdapter().addTrip(newTrip);
                }
            }
        });
        builder.create().show();
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
        getMenuInflater().inflate(R.menu.main, menu);
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
        switch (item.getItemId()) {
            case R.id.nav_profile:
                ProfileFragment profileFragment = new ProfileFragment();
                Bundle args = new Bundle();
                args.putParcelable(Constants.USER, currentUser);
                profileFragment.setArguments(args);
                switchTo = profileFragment;
                break;
            case R.id.nav_home:
                HomePageFragment homePageFragment = new HomePageFragment();
                Bundle homeArgs = new Bundle();
                homeArgs.putString(Constants.ROSEFIRE_PATH, "users/" + mAuth.getCurrentUser().getUid());
                homePageFragment.setArguments(homeArgs);
                switchTo = homePageFragment;
                break;
            case R.id.nav_find_trips:
                mTripListFragment = new TripListFragment();
                switchTo = mTripListFragment;
                break;
            case R.id.nav_trip_history:
                switchTo = new TripHistoryFragment();
                break;
            case R.id.nav_about:
                switchTo = new AboutFragment();
                break;
            case R.id.nav_settings:
                break;
        }

        if (switchTo != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, switchTo);
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStackImmediate();
            }
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
        ft.replace(R.id.fragment_container, TripDetailFragment.newInstance(trip));
        ft.addToBackStack("detail");
        ft.commit();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ROSEFIRE_LOGIN) {
            RosefireResult result = Rosefire.getSignInResultFromIntent(data);
            if (result.isSuccessful()) {
                mAuth.signInWithCustomToken(result.getToken())
                        .addOnCompleteListener(this, mOnCompleteListener);
//                Log.d(Constants.TAG, result.getEmail());
//                Log.d(Constants.TAG, result.getGroup());
//                Log.d(Constants.TAG, result.getName());
//                Log.d(Constants.TAG, result.getUsername());
                userRef.keepSynced(true);
                currentUser = new User();
                currentUser.setKey(result.getUsername());
                currentUser.setName(result.getName());
                currentUser.setEmail(result.getEmail());
                setNavInfo();
            } else {
                showLoginError("Rosefire sign-in error");
            }
        }else if(requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK){
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
                        Log.d("upload image", "onSuccess: "+downloadUrl);
                        switchToProfileFragment();
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadProfileImage(){
        StorageReference profileImageRef = imageRef.child(currentUser.getKey());

        File localFile = null;
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final File finalLocalFile = localFile;
        profileImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Bitmap bmp = BitmapFactory.decodeFile(finalLocalFile.getPath());
                navProfileImageView.setImageBitmap(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
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
                Log.d("phone number", "onClick: "+currentUser.getKey());
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
        ft.replace(R.id.fragment_container, new LoginFragment(), "login");
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
        ft.replace(R.id.fragment_container, profileFragment, "login");
        ft.commit();
    }

    private void switchToHomeFragment(String path) {

        userRef.child(currentUser.getKey()).child("phoneNumber").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long phone = dataSnapshot.getValue(Long.class);
                if(phone==null || phone==0){
                    showAddPhoneNumberDialog();
                }
                currentUser.setPhoneNumber(phone);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setNavInfo();
        loadProfileImage();
        mToolbar.setVisibility(View.VISIBLE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment homeFragment = new HomePageFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ROSEFIRE_PATH, path);
        homeFragment.setArguments(args);
        ft.replace(R.id.fragment_container, homeFragment, "home");
        ft.commit();
    }

    private void setNavInfo(){
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
        if(currentUser!=null){
        editor.putString(KEY_USER_KEY, currentUser.getKey());
        editor.putString(KEY_USER_EMAIL, currentUser.getEmail());
        editor.putString(KEY_USER_NAME, currentUser.getName());
//        if(currentUser.getPhoneNumber()!=null){
        editor.putLong(KEY_USER_PHONE, currentUser.getPhoneNumber());}

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
                        new Intent[] { takePhotoIntent }
                );

        Log.d("image", "onImageButtonPressed: ");
        startActivityForResult(chooserIntent, RC_SELECT_IMAGE);
    }

    @Override
    public void onEditButtonPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Your Information");
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);
        final EditText phoneEditText = (EditText) view.findViewById(R.id.edit_profile_phone_number);
        builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentUser.setPhoneNumber(Long.valueOf(phoneEditText.getText().toString()));
//                Log.d("phone number", "onClick: "+currentUser.getKey());
                userRef.child(currentUser.getKey()).setValue(currentUser);
                switchToProfileFragment();
            }
        });
        builder.setNegativeButton(android.R.string.cancel,null);
        builder.create().show();
    }
}
