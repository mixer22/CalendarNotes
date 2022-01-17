package com.emc.thye;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.emc.verticalweekcalendar.VerticalWeekCalendar;
import com.emc.verticalweekcalendar.controller.VerticalWeekAdapter;
import com.emc.verticalweekcalendar.interfaces.DateWatcher;
import com.emc.verticalweekcalendar.interfaces.OnDateClickListener;
import com.emc.verticalweekcalendar.model.CalendarDay;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;



public class MainActivity extends AppCompatActivity {

    private GregorianCalendar selected; // check
    private VerticalWeekCalendar calendarView;
    EditText editTextLabel;
    EditText editTextTime;
    TimePickerDialog timePickerDialog; // check
    Button btn_add, btn_clean;
    ListView listView; // check
    SimpleCursorAdapter userAdapter; // check
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    DataBaseHelper databaseHelper;
    Cursor userCursor;
    SQLiteDatabase db; // check
    Calendar calendar;
    int currentHour;
    int currentMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextLabel = findViewById(R.id.editText1);
        editTextTime = findViewById(R.id.editTextTime);
        btn_add = findViewById(R.id.button);
        btn_clean = findViewById(R.id.button2);
        listView = findViewById(R.id.listView);
        databaseHelper = new DataBaseHelper(getApplicationContext());
        Log.e("Table", DataBaseHelper.TABLE);
        setDataToAdaper();

        editTextTime.setOnClickListener(new View.OnClickListener() { // check
            @Override
            public void onClick(View view) {
                calendar= Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                        editTextTime.setText(String.format("%02d:%02d", hourOfDay, minutes));
                    }
                }, currentHour, currentMinute, true);

                timePickerDialog.show();
            }
        });

        calendar= Calendar.getInstance();

        selected = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        calendarView = new VerticalWeekCalendar.Builder()
                .setView(R.id.verticalCalendar)
                .init(this);

        calendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onCalenderDayClicked(int year, int month, int day) {
                GregorianCalendar selectedDay = new GregorianCalendar(year, month, day);
                if(selected.compareTo(selectedDay) != 0) {
                    selected = selectedDay;
                    DataBaseHelper.TABLE = "Notes" + String.valueOf(year) + String.valueOf(month) + String.valueOf(day);
                    Log.e("Table", DataBaseHelper.TABLE);
                    db = databaseHelper.getReadableDatabase();
                    databaseHelper.onCreate(db);
                    setDataToAdaper();
                }
            }
        });

        calendarView.setDateWatcher(new DateWatcher() {
            @Override
            public int getStateForDate(int year, int month, int day, VerticalWeekAdapter.DayViewHolder view) {
                return selected.compareTo(new GregorianCalendar(year, month, day)) == 0 ?
                        CalendarDay.SELECTED : CalendarDay.DEFAULT;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setDataToAdaper();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        db.close();
        userCursor.close();
    }

    public void onClickBtn(View v) throws ParseException {
        switch (v.getId()){
            case R.id.button2:
                String name = editTextLabel.getText().toString(); // check
                String time = editTextTime.getText().toString();
                if(!name.equals("")){
                    databaseHelper.addNote(db, name, time);
                    setDataToAdaper();
                    userAdapter.notifyDataSetChanged();
                    editTextLabel.setText("");
                }
                break;
            case R.id.button:
                databaseHelper.cleanTable(db);
                setDataToAdaper();
                break;
        }
    }

    public void setDataToAdaper(){
        db = databaseHelper.getReadableDatabase();
        userCursor =  db.rawQuery("select * from "+ DataBaseHelper.TABLE, null);
        String[] headers = new String[] {DataBaseHelper.COLUMN_NAME, DataBaseHelper.COLUMN_DATE};
        userAdapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item,
                userCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        listView.setAdapter(userAdapter);
    }
}
