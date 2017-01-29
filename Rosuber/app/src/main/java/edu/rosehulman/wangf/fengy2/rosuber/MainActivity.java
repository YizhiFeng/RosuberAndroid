package edu.rosehulman.wangf.fengy2.rosuber;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;
import java.util.Calendar;

import edu.rosehulman.rosefire.Rosefire;
import edu.rosehulman.rosefire.RosefireResult;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.AboutFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.HomePageFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.LoginFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.ProfileFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripDetailFragment;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripListFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    TripListFragment.TripListCallback, LoginFragment.OnLoginListener {

    private static final int RC_ROSEFIRE_LOGIN = 1;
    FloatingActionButton mFab;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    OnCompleteListener mOnCompleteListener;
    Toolbar mToolbar;

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
                addEditTripDialog(false);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mAuth = FirebaseAuth.getInstance();
        initializeListeners();
    }

    private void initializeListeners() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.d("TAG", "USER: " + user);
                if (user != null) {
                    switchToHomeFragment("users/" + user.getUid());
                } else {
                    mFab.setVisibility(View.GONE);
                    mToolbar.setVisibility(View.GONE);
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

    public void addEditTripDialog(boolean isEditing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add, null);
        builder.setView(view);
        builder.setTitle(R.string.add_a_trip);
        final Switch isDriverSwitch = (Switch) view.findViewById(R.id.isDriverSwitch);
        final TextView isDriverTextView = (TextView) view.findViewById(R.id.isDriverTextView);
        EditText originEditText = (EditText) view.findViewById(R.id.orginEditText);
        EditText destEditText = (EditText) view.findViewById(R.id.destEditText);
        Button dateButton = (Button) view.findViewById(R.id.dateButton);
        final TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        Button timeButton = (Button) view.findViewById(R.id.timeButton);
        final TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
//        SeekBar numPassengerSBar = (SeekBar) view.findViewById(R.id.seek_bar);
        EditText priceEditText = (EditText) view.findViewById(R.id.priceEditText);

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

        String posButtonText = "ADD";
        if (isEditing) {
            posButtonText = "UPDATE";
        }
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(posButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment switchTo = null;
        switch (item.getItemId()) {
            case R.id.nav_profile:
                switchTo = new ProfileFragment();
                mFab.setVisibility(View.GONE);
                break;
            case R.id.nav_home:
                switchTo = new HomePageFragment();
                mFab.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_find_trips:
                mFab.setVisibility(View.VISIBLE);
                switchTo = new TripListFragment();
                break;
            case R.id.nav_trip_history:
                mFab.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_about:
                switchTo = new AboutFragment();
                mFab.setVisibility(View.GONE);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ROSEFIRE_LOGIN) {
            RosefireResult result = Rosefire.getSignInResultFromIntent(data);
            if (result.isSuccessful()) {
                mAuth.signInWithCustomToken(result.getToken())
                        .addOnCompleteListener(this, mOnCompleteListener);
            } else {
                showLoginError("Rosefire sign-in error");
            }
        }
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
    }

    @Override
    public void onRosefireLogin() {
        Intent signInIntent = Rosefire.getSignInIntent(this, getString(R.string.rosefireKey));
        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN);
    }

    private void switchToLoginFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new LoginFragment(), "Login");
        ft.commit();
    }

    private void switchToHomeFragment(String path) {
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        Fragment passwordFragment = new PasswordFragment();
//        Bundle args = new Bundle();
//        args.putString(Constants.FIREBASE_PATH, path);
//        passwordFragment.setArguments(args);
//        ft.replace(R.id.fragment, passwordFragment, "Passwords");
//        ft.commit();
        mToolbar.setVisibility(View.VISIBLE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment homeFragment = new HomePageFragment();
        Bundle args = new Bundle();
        args.putString("ROSEFIRE_PATH", path);
        ft.replace(R.id.fragment_container, homeFragment, "home");
        ft.commit();
    }

    private void showLoginError(String message) {
        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("Login");
        loginFragment.onLoginError(message);
    }
}
