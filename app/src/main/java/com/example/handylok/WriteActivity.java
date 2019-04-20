package com.example.handylok;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WriteActivity extends AppCompatActivity {

    final Context context = this;

    Intent intent;
    int MainRequestCode;
    private static final int addMode = 100;
    private static final int modifyMode = 500;

    Calendar calendar;
    SimpleDateFormat format = new SimpleDateFormat();
    SimpleDateFormat formater = new SimpleDateFormat();

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

        setActionBar("내 손안에 작은 회의록");

        etName = findViewById(R.id.name);
        etPlace = findViewById(R.id.place);
        etDate = findViewById(R.id.date);
        etContext = findViewById(R.id.context);

        final Button insert = findViewById(R.id.insert);
        final Button update = findViewById(R.id.update);

        db = new DBAdapter(WriteActivity.this);

        // 현재 날짜 저장 (datePicker 초기화 데이터)
        calendar = Calendar.getInstance();
        format.applyPattern("yyyy년 MM월 dd일 HH시 mm분");
        Year = calendar.get(calendar.YEAR);
        Month = calendar.get(calendar.MONTH);
        Day = calendar.get(calendar.DAY_OF_MONTH);

        // 현재 시간 저장 (timePicker 초기화 데이터)
        Hour = calendar.get(calendar.HOUR_OF_DAY);
        Minute = calendar.get(calendar.MINUTE);
        etDate.setText(format.format(calendar.getTime()));

        // intent
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

        // etDate Touch Listener
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
                        loadData();

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
                        loadData();

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

    //액션버튼 메뉴 액션바에 집어 넣기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write_menu, menu);
        return true;
    }

    //액션버튼을 클릭했을때의 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_share:
                break;
            case R.id.action_attach:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        name = etName.getText().toString();
        place = etPlace.getText().toString();
        date = etDate.getText().toString();
        contexts = etContext.getText().toString();
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

            // 선택한 날짜로 formatting
            format.applyPattern("yyyy년 MM월 dd일 ");
            calendar.set(calendar.YEAR, year);
            calendar.set(calendar.MONTH, monthOfYear);
            calendar.set(calendar.DAY_OF_MONTH, dayOfMonth);

            // DatePicker 날짜 변경
            Year = year;
            Month = monthOfYear;
            Day = dayOfMonth;

            // etDate 수정
            etDate.setText(format.format(calendar.getTime()));
        }
    };

    // TimePickerDialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // 선택한 시간으로 formatting
            format.applyPattern("HH시 mm분");
            calendar.set(calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(calendar.MINUTE, minute);

            // TimePicker 날짜 변경
            Hour = hourOfDay;
            Minute = minute;

            // etDate 수정
            etDate.append(format.format(calendar.getTime()));
        }
    };

    private void setActionBar(String title) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF339999));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
