package com.example.handylok;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.Calendar;

public class WriteActivity extends AppCompatActivity {

    private static final int addMode = 100;
    private static final int modifyMode = 500;
    Intent intent;
    int MainRequestCode;

    final Context context = this;
    Calendar calendar;
    int Year, Month, Day;
    int Hour, Minute;

    DBAdapter db;
    EditText etName;
    EditText etPlace;
    EditText etDate;
    EditText etContext;

    int id;
    String name;
    String place;
    String date;
    String contexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        etName = findViewById(R.id.name);
        etPlace = findViewById(R.id.place);
        etDate = findViewById(R.id.date);
        etContext = findViewById(R.id.context);

        final Button insert = findViewById(R.id.insert);
        final Button update = findViewById(R.id.update);

        db = new DBAdapter(WriteActivity.this);

        calendar = Calendar.getInstance();
        Year = calendar.get(Calendar.YEAR) ;
        Month = calendar.get(Calendar.MONTH);
        Day = calendar.get(Calendar.DAY_OF_MONTH);
        Hour = calendar.get(calendar.HOUR_OF_DAY);
        Minute = calendar.get(calendar.MINUTE);
        etDate.setText(Year + "년 " + (Month + 1) + "월 " + Day + "일 " + Hour + "시 " + Minute + "분");

        intent = getIntent();
        MainRequestCode = intent.getIntExtra("MainRequestCode", 0);

        if (MainRequestCode == addMode) {
            Log.d("addMode", "추가 모드");
            update.setVisibility(View.GONE);
        } else if (MainRequestCode == modifyMode) {
            Log.d("modifyMode", "수정 모드");
            insert.setVisibility(View.GONE);

            id = intent.getIntExtra("_id", 0);
            name = intent.getStringExtra("name");
            place = intent.getStringExtra("place");
            date = intent.getStringExtra("date");
            contexts = intent.getStringExtra("contexts");

            etName.setText(name);
            etPlace.setText(place);
            etDate.setText(date);
            etContext.setText(contexts);
        } else {
            Log.d("RequestCode Error", "");
        }

        // etDate Click Listener
        etDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DatePickerDialog d_dialog = new DatePickerDialog(context, mDataSetListener, Year, Month, Day);
                    d_dialog.show();
                }
                return false;
            }
        });

        // onClickListener
        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.insert: // DB에 데이터 추가
                        name = etName.getText().toString();
                        place = etPlace.getText().toString();
                        date = etDate.getText().toString();
                        contexts = etContext.getText().toString();

                        if (name.length() > 0 && place.length() > 0 && contexts.length() > 0) {
                            db.open();
                            db.addData(name, place, date, contexts);
                            db.close();
                            okDialog("추가");
                        } else {
                            Toast.makeText(getApplicationContext(), "공란 없이 입력해주세요.", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.update: // DB에 있는 데이터 수정
                        name = etName.getText().toString();
                        place = etPlace.getText().toString();
                        date = etDate.getText().toString();
                        contexts = etContext.getText().toString();

                        if (name.length() > 0 && place.length() > 0 && contexts.length() > 0) {
                            db.open();
                            db.modifyData(id, name, place, date, contexts);
                            db.close();
                            okDialog("수정");
                        } else {
                            Toast.makeText(getApplicationContext(), "공란 없이 입력해주세요.", Toast.LENGTH_SHORT).show();
                        }

                        break;

                }
            }
        };

        insert.setOnClickListener(onClickListener);
        update.setOnClickListener(onClickListener);

    }

    private void okDialog(String type) {
        final SweetAlertDialog okDialog = new SweetAlertDialog(context);
        okDialog
                .setTitleText(type)
                .setContentText(type + " 성공했습니다.")
                .setConfirmText("확인")
                .show();

        okDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                finish();
            }
        });
    }

    // DatePickerDialog
    private DatePickerDialog.OnDateSetListener mDataSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            TimePickerDialog t_dialog = new TimePickerDialog(context, mTimeSetListener, Hour, Minute, true);
            t_dialog.show();
            etDate.setText(year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일 ");
        }
    };

    // TimePickerDialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            etDate.append(hourOfDay + "시 " + minute + "분");
        }
    };

}
