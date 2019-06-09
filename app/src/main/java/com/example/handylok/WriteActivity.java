package com.example.handylok;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WriteActivity extends AppCompatActivity {

    final Context context = this;

    Intent intent;
    int MainRequestCode;
    private static final int addMode = 100;
    private static final int modifyMode = 500;

    Calendar calendar;
    SimpleDateFormat format = new SimpleDateFormat();

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

    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


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

        // place Touch Listener
        etPlace.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!checkLocationServicesStatus()) {

                        showDialogForLocationServiceSetting();
                    }else {
                        checkRunTimePermission();
                    }

                    gpsTracker = new GpsTracker(WriteActivity.this);

                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();

                    String address = getCurrentAddress(latitude, longitude);
                    etPlace.setText(address);

                    Toast.makeText(WriteActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

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
        okDialog.setTitleText("성공");
        okDialog.setContentText(type + " 성공했습니다.");
        okDialog.setConfirmText("확인");

        okDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                okDialog.dismiss();
                finish();
            }
        });
        okDialog.show();

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

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(WriteActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(WriteActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(WriteActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(WriteActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(WriteActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(WriteActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(WriteActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(WriteActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(WriteActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}
