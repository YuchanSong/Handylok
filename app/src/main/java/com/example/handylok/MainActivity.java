package com.example.handylok;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Calendar calendar;
    int Year, Month, Day;
    int Hour, Minute;
    TextView date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init
        Button dialog_bt_date = findViewById(R.id.dialog_bt_date);

        // Btn_Click_Listener
        dialog_bt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 버튼이 클릭되면 현재 날짜/시간 불러오기
                calendar = Calendar.getInstance();
                Year = calendar.get(Calendar.YEAR) ;
                Month = calendar.get(Calendar.MONTH);
                Day = calendar.get(Calendar.DAY_OF_MONTH);
                Hour = calendar.get(calendar.HOUR_OF_DAY);
                Minute = calendar.get(calendar.MINUTE);
                date = findViewById(R.id.date);
                DatePickerDialog d_dialog = new DatePickerDialog(MainActivity.this, mDataSetListener, Year, Month, Day);
                d_dialog.show();
            }
        });

    }

    // DatePickerDialog
    private DatePickerDialog.OnDateSetListener mDataSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            TimePickerDialog t_dialog = new TimePickerDialog(MainActivity.this, mTimeSetListener, Hour, Minute, true);
            t_dialog.show();
            date.setText(year + "/" + (monthOfYear + 1) + "/" + dayOfMonth + " ");
        }
    };

    // TimePickerDialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            date.append(hourOfDay + "시 " + minute + "분");
        }
    };
}
