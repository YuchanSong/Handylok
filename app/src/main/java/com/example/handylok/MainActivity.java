package com.example.handylok;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Calendar calendar;
    int Year, Month, Day;
    int Hour, Minute;
    TextView date;

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init
        Button add = findViewById(R.id.add);

        // Btn_Click_Listener
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 버튼이 클릭되면 현재 날짜/시간 불러오기
                show_add();
//                calendar = Calendar.getInstance();
//                Year = calendar.get(Calendar.YEAR) ;
//                Month = calendar.get(Calendar.MONTH);
//                Day = calendar.get(Calendar.DAY_OF_MONTH);
//                Hour = calendar.get(calendar.HOUR_OF_DAY);
//                Minute = calendar.get(calendar.MINUTE);
//                date = findViewById(R.id.date);
//                DatePickerDialog d_dialog = new DatePickerDialog(MainActivity.this, mDataSetListener, Year, Month, Day);
//                d_dialog.show();
//                Intent intent = new Intent(getApplicationContext(), WriteActivity.class);
//                startActivity(intent);
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

    // back key 2번 종료
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
            super.onBackPressed();
        else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "뒤로 가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /* add 다이얼로그 */
    void show_add() {
        /* dialog_login */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_write, null);
        builder.setView(view);

//        final Button submit = (Button) view.findViewById(R.id.buttonSubmit);
//        final Button register = (Button) view.findViewById(R.id.buttonRegister);
//        final EditText login_idText = (EditText) view.findViewById(R.id.idText);
//        final EditText login_passwordText = (EditText) view.findViewById(R.id.passwordText);
//        final CheckBox autoLoginCheck = (CheckBox) view.findViewById(R.id.autoLoginCheck);
//        final CheckBox saveID = (CheckBox) view.findViewById(R.id.saveID);

//        SharedPreferences id = getSharedPreferences("save", MODE_PRIVATE);
//        login_idText.setText(id.getString("userID", ""));
//        saveID.setChecked(id.getBoolean("saveIDCheck", false));

        final AlertDialog dialog = builder.create();

        dialog.show();

//        submit.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                userID = login_idText.getText().toString();
//                userPassword = login_passwordText.getText().toString();
//
//                Response.Listener<String> responseListener = new Response.Listener<String>() {
//
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject jsonResponse = new JSONObject(response);
//                            boolean success = jsonResponse.getBoolean("success");
//                            if (success) {
//                                dialog.dismiss();
//                                Intent intent = new Intent(context, MainActivity.class);
//
//                                userID = jsonResponse.getString("userID");
//                                userPassword = jsonResponse.getString("userPassword");
//                                userName = jsonResponse.getString("userName");
//                                userAge = jsonResponse.getInt("userAge");
//
//                                /* SharedPreference */
//                                SharedPreferences id = getSharedPreferences("save", MODE_PRIVATE);
//                                SharedPreferences.Editor saveID_info = id.edit();
//
//                                자동로그인 체크 이벤트
//                                if (autoLoginCheck.isChecked()) {
//                                    autoCheck++;
//                                    savePreference();
//                                } else {
//                                    intent.putExtra("userID", userID);
//                                    intent.putExtra("userPassword", userPassword);
//                                    intent.putExtra("userName", userName);
//                                    intent.putExtra("userAge", userAge);
//                                }
//                                if (saveID.isChecked()) {
//                                    saveID_info.putString("userID", userID);
//                                    saveID_info.putBoolean("saveIDCheck", true);
//                                    saveID_info.commit();
//                                } else {
//                                    saveID_info.putString("userID", null);
//                                    saveID_info.putBoolean("saveIDCheck", false);
//                                    saveID_info.commit();
//                                }
//
//                                finish();
//                                startActivity(intent);
//                            } else {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                                builder.setMessage("로그인에 실패하였습니다.")
//                                        .setNegativeButton("다시 시도", null)
//                                        .create()
//                                        .show();
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
            }
//        });
//                LoginRequest loginRequest = new LoginRequest(userID, userPassword, responseListener);
//                RequestQueue queue = Volley.newRequestQueue(context);
//                queue.add(loginRequest);
//            }
//        });
//

}
