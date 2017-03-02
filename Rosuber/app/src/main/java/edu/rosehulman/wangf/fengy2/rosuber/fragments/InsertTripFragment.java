package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import edu.rosehulman.wangf.fengy2.rosuber.Constants;
import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.Trip;

/**
 * Created by wangf on 2/12/2017.
 */

public class InsertTripFragment extends Fragment {


    private InsertTripCallBack mCallback;
    private boolean isEdit;
    private Trip trip;
    private String currentUserKey;

    public InsertTripFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isEdit = getArguments().getBoolean(Constants.EDIT);
        trip = getArguments().getParcelable(Constants.TRIP);
        currentUserKey = getArguments().getString(Constants.USER);
    }

    private void switchToMap(boolean isSettingOrigin) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(Constants.USER, currentUserKey);
        args.putBoolean(Constants.EDIT, isEdit);
        args.putParcelable(Constants.TRIP, trip);
        args.putBoolean(Constants.ISSETTINGORIGIN, isSettingOrigin);
        fragment.setArguments(args);
        ft.replace(R.id.fragment_container, fragment);
        ft.addToBackStack("map");
        ft.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (isEdit) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Update Trip");
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("New Trip");
        }
        getActivity().findViewById(R.id.fab).setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.dialog_add, null);
        final Switch isDriverSwitch = (Switch) view.findViewById(R.id.isDriverSwitch);
        final TextView isDriverTextView = (TextView) view.findViewById(R.id.isDriverTextView);
        final EditText originEditText = (EditText) view.findViewById(R.id.orginEditText);
        final EditText destEditText = (EditText) view.findViewById(R.id.destEditText);
        Button dateButton = (Button) view.findViewById(R.id.dateButton);
        final TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        Button timeButton = (Button) view.findViewById(R.id.timeButton);
        final TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
        final SeekBar numPassengerSBar = (SeekBar) view.findViewById(R.id.seek_bar);
        final TextView capacityTextView = (TextView) view.findViewById(R.id.capacityTextView);
        final EditText priceEditText = (EditText) view.findViewById(R.id.priceEditText);
        Button insertButton = (Button) view.findViewById(R.id.insert_trip_button);
        Button cancelButton = (Button) view.findViewById(R.id.cancel_insert_trip_button);
        Button leaveTripButton = (Button) view.findViewById(R.id.leave_trip_button);
        ImageButton destinationButton = (ImageButton) view.findViewById(R.id.destination_image_button);
        ImageButton originButton = (ImageButton) view.findViewById(R.id.origin_image_button);


        if (isEdit) {
            leaveTripButton.setVisibility(View.VISIBLE);
        } else {
            leaveTripButton.setVisibility(View.GONE);
        }

        destinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMap(false);
            }
        });

        originButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMap(true);
            }
        });

        if (!isEdit && trip != null) {
            if (trip.getOrigin() != null) {
                originEditText.setText(trip.getOrigin());
            }
            if (trip.getDestination() != null) {
                destEditText.setText(trip.getDestination());
            }
        }

        if (isEdit) {
            final boolean isDriver;
            if (trip.getDriverKey() != null) {
                isDriver = trip.getDriverKey().equals(currentUserKey) ? true : false;
            } else {
                isDriver = false;
            }

            if (isDriver) {
                isDriverTextView.setText(R.string.i_am_a_driver);
                numPassengerSBar.setEnabled(true);
            } else {
                isDriverTextView.setText(R.string.i_am_a_passenger);
                numPassengerSBar.setEnabled(false);
            }
            String[] timeArray = trip.getTime().split(" ");
            isDriverSwitch.setChecked(isDriver);
            originEditText.setText(trip.getOrigin());
            destEditText.setText(trip.getDestination());
            dateTextView.setText(timeArray[0]);
            timeTextView.setText(timeArray[1]);
            numPassengerSBar.setProgress((int) trip.getCapacity());
            capacityTextView.setText(trip.getCapacity() + "");
            priceEditText.setText((int) trip.getPrice() + "");
            insertButton.setText(getString(R.string.button_text_update));
            leaveTripButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isDriver) {
                        trip.setDriverKey(null);
                    } else {
                        Map<String, Boolean> newPass = new HashMap<>();
                        for (String pass : trip.getPassengerKey().keySet()) {
                            if (!pass.equals(currentUserKey)) {
                                newPass.put(pass, true);
                            }
                        }
                        trip.setPassengerKey(newPass);
                    }
                    mCallback.leaveTripPressed(trip);
                }
            });

        }


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

        if (isEdit) {
            isDriverSwitch.setEnabled(false);
        } else {
            isDriverSwitch.setEnabled(true);
        }

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
                capacityTextView.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final Activity thisAcitivy = getActivity();

        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDriverChecked = isDriverSwitch.isChecked();
                String origin = originEditText.getText().toString();
                String destination = destEditText.getText().toString();
                String dateText = dateTextView.getText().toString();
                String timeText = timeTextView.getText().toString();
                if (dateText.isEmpty() || timeText.isEmpty()) {
                    Toast.makeText(thisAcitivy, "Please enter both the date and the time", Toast.LENGTH_LONG).show();
                } else {
                    String time = dateText + " " + timeText;
                    long capacity = numPassengerSBar.getProgress();
                    String priceText = priceEditText.getText().toString();
                    if (priceText.isEmpty()) {
                        Toast.makeText(thisAcitivy, "Please enter a suggested price, which doesn't have to be the final price of this trip", Toast.LENGTH_LONG).show();
                    } else {
                        Long price = Long.parseLong(priceText);

                        if (origin == null || destination == null || time == null || capacity == 0) {
                            Toast.makeText(thisAcitivy, "Please fill out all the information", Toast.LENGTH_LONG).show();
                        } else {
                            if (isExpired(time)) {
                                Toast.makeText(thisAcitivy, "Your trip can not be earlier than now", Toast.LENGTH_LONG).show();
                            } else {
                                mCallback.insertTripPressed(trip, isEdit, origin, destination, time, price, capacity, isDriverChecked);
                            }
                        }
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.cancelInsertTripButtonPressed(isEdit);
            }
        });


        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_map_search).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_map_ok).setVisible(false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InsertTripCallBack) {
            mCallback = (InsertTripCallBack) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnContactListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    private boolean isExpired(String tripTime) {
        Calendar currentTime = Calendar.getInstance();
        int currentYear = currentTime.get(Calendar.YEAR);
        int currentMonth = currentTime.get(Calendar.MONTH) + 1;
        int currentDay = currentTime.get(Calendar.DAY_OF_MONTH);
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTime.get(Calendar.MINUTE);

        Log.d("Current: ", currentDay + "/" + currentMonth + "/" + currentYear + " " + currentHour + ":" + currentMinute);
        Log.d("Set: ", tripTime);

        String[] s = tripTime.split(" ");
        String date = s[0];
        String[] ddmmyy = date.split("/");
        int day = Integer.parseInt(ddmmyy[0]);
        int month = Integer.parseInt(ddmmyy[1]);
        int year = Integer.parseInt(ddmmyy[2]);

        String time = s[1];
        String[] hhmm = time.split(":");
        int hour = Integer.parseInt(hhmm[0]);
        int min = Integer.parseInt(hhmm[1]);

        if (year >= currentYear) {
            if (month >= currentMonth) {
                if (day >= currentDay) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


    public interface InsertTripCallBack {
        void insertTripPressed(Trip trip, boolean isEdit, String origin, String destination, String time, Long price, long capacity, boolean isDriverChecked);

        void leaveTripPressed(Trip trip);

        void cancelInsertTripButtonPressed(boolean isEdit);
    }
}
