package com.example.handylok;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    DBAdapter.OpenHelper mHelper;
    SQLiteDatabase dbs = null;

    Cursor monthCursor = null;

    EditText etName;
    EditText etPlace;
    EditText etDate;
    EditText etContext;

    int id = -1;
    String name;
    String place;
    String date;
    String contexts;

    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private static final int PICK_FROM_CAMERA = 1; //카메라 촬영으로 사진 가져오기
    private static final int PICK_FROM_ALBUM = 2; //앨범에서 사진 가져오기
    private static final int CROP_FROM_CAMERA = 3; //가져온 사진을 자르기 위한 변수
    Uri photoUri;

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
    private ImageView mImageView;
    byte[] byteImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        setActionBar("내 손안에 작은 회의록");

        mImageView = findViewById(R.id.mImageView);
        etName = findViewById(R.id.name);
        etPlace = findViewById(R.id.place);
        etDate = findViewById(R.id.date);
        etContext = findViewById(R.id.context);

        final Button insert = findViewById(R.id.insert);
        final Button update = findViewById(R.id.update);


        db = new DBAdapter(WriteActivity.this);
        mHelper = db.new OpenHelper(context);
        dbs = mHelper.getWritableDatabase();

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

            // etPlace 수정
            etPlace.setText(currentLocation());

            update.setVisibility(View.GONE);
        } else if (MainRequestCode == modifyMode) {
            Log.d("modifyMode", "수정 모드");
            insert.setVisibility(View.GONE);

            id = intent.getIntExtra("_id", -1);
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

        // ImageView Touch Listener
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (byteImage == null)
                            break;

                        final Bitmap bm = getImage(byteImage); // 이미지 가져오기

                        DialogInterface.OnClickListener saveListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    saveBitmaptoJpeg(bm, "Handyrok", "save");
                                }
                            };

                            DialogInterface.OnClickListener cancelListner = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setPositiveButton("저장", saveListener)
                                    .setNegativeButton("닫기", cancelListner);

                            final AlertDialog dialog = builder.create();
                            LayoutInflater inflater = getLayoutInflater();
                            View dialogLayout = inflater.inflate(R.layout.dialog_image, null);

                            ImageView dialogImage = dialogLayout.findViewById(R.id.DialogImage);

                            // 이미지 set
                            dialogImage.setImageBitmap(bm);

                            // 이미지 크게
                            dialogImage.getLayoutParams().height = 600;
                            dialogImage.requestLayout();

                            // setView
                            dialog.setView(dialogLayout);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                            dialog.show();

                        break;
                }
                return true;
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
                            // 작성글 디비에 추가
                            db.open();
                            long i = (db.addData(name, place, date, contexts)) - 1;
                            Log.d("추가 인덱스 return", String.valueOf(i));

                            // 이미지 byte 불러오기
                            byte[] byteImage = getByteArray();

                            // 이미지가 추가되지 않았다면 insert 하지 않는다.
                            if (byteImage != null) {
                                String sql = "INSERT INTO table_image values(?, ?)";
                                SQLiteStatement insertStmt = dbs.compileStatement(sql);
                                insertStmt.clearBindings();
                                insertStmt.bindString(1, String.valueOf(i));
                                insertStmt.bindBlob(2, byteImage);
                                insertStmt.execute();
                                Log.d("추가한 인덱스", String.valueOf(i));
                            }

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

        // 수정모드 일때만 이미지 조회하기
        if (MainRequestCode == modifyMode) {
            try {
                String selectSql = "SELECT image_data FROM table_image WHERE image_id = '" + id + "'";
                monthCursor = dbs.rawQuery(selectSql, null);
                Log.d("조회 인덱스", String.valueOf(id));

                while (monthCursor.moveToNext()) {
//                    byte[] byteImage = monthCursor.getBlob(0);
                    byteImage = monthCursor.getBlob(0);
                    Bitmap bm = getImage(byteImage);
                    mImageView.setImageBitmap(bm);
                }
                monthCursor.close();
            } catch (Exception e) {
            } finally {
                mHelper.close();///
            }
        }
    }

    private String currentLocation() {
        gpsTracker = new GpsTracker(WriteActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);
        return address;
    }

    public void saveBitmaptoJpeg(Bitmap bitmap, String folder, String name){
        String ex_storage =Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d("ex_storage", ex_storage);
        // Get Absolute Path in External Sdcard
        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg";
        String string_path = ex_storage+foler_name;

        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(WriteActivity.this, "저장되었음", Toast.LENGTH_SHORT).show();
            out.close();

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
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
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                email.putExtra(Intent.EXTRA_SUBJECT, "회의 제목 : " + name);
                email.putExtra(android.content.Intent.EXTRA_STREAM, photoUri);
                email.putExtra(Intent.EXTRA_TEXT,"장소 : " + place + "\n"
                + "시간 : " + date + "\n" + "내용 : " + contexts + "\n");

                startActivity(Intent.createChooser(email, "Send mail"));
                break;
            case R.id.action_attach:
                checkPermissions(); //권한 묻기

                DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        takePhoto();
                    }
                };

                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToAlbum();
                    }
                };


                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                };

                new AlertDialog.Builder(this)
                        .setTitle("업로드할 이미지 선택")
                        .setPositiveButton("사진촬영", cameraListener)
                        .setNeutralButton("앨범선택", albumListener)
                        .setNegativeButton("취소", cancelListener)
                        .show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //사진을 찍기 위하여 설정합니다.
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(WriteActivity.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();              finish();
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(WriteActivity.this,
                    "com.example.handylok.provider", photoFile); //FileProvider의 경우 이전 포스트를 참고하세요.
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); //사진을 찍어 해당 Content uri를 photoUri에 적용시키기 위함
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "IP" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/test/"); //test라는 경로에 이미지를 저장하기 위함
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK); //ACTION_PICK 즉 사진을 고르겠다!
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    //권한 획득에 동의를 하지 않았을 경우 아래 Toast 메세지를 띄우며 해당 Activity를 종료시킵니다.
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
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
        else if (permsRequestCode == MULTIPLE_PERMISSIONS) {
            if (grandResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(this.permissions[0])) {
                        if (grandResults[i] != PackageManager.PERMISSION_GRANTED) {
                            showNoPermissionToastAndFinish();
                        }
                    } else if (permissions[i].equals(this.permissions[1])) {
                        if (grandResults[i] != PackageManager.PERMISSION_GRANTED) {
                            showNoPermissionToastAndFinish();

                        }
                    } else if (permissions[i].equals(this.permissions[2])) {
                        if (grandResults[i] != PackageManager.PERMISSION_GRANTED) {
                            showNoPermissionToastAndFinish();
                        }
                    }
                }
            } else {
                showNoPermissionToastAndFinish();
            }
            return;
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

        //지오코더 GPS를 주소로 변환
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
            case PICK_FROM_ALBUM:
                if(data==null){
                    return;
                }
                photoUri = data.getData();
                cropImage();
                break;
            case PICK_FROM_CAMERA:
                cropImage();
                MediaScannerConnection.scanFile(WriteActivity.this, //앨범에 사진을 보여주기 위해 Scan을 합니다.
                        new String[]{photoUri.getPath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
                break;
            case CROP_FROM_CAMERA:
                try {
                    byte[] byteImage = getByteArray();
                    Bitmap thumbImage = getImage(byteImage);
                    mImageView.setImageBitmap(thumbImage);
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage().toString());
                }
                break;
        }
    }

    //Android N crop image (이 부분에서 몇일동안 정신 못차렸습니다 ㅜ)
    //모든 작업에 있어 사전에 FALG_GRANT_WRITE_URI_PERMISSION과 READ 퍼미션을 줘야 uri를 활용한 작업에 지장을 받지 않는다는 것이 핵심입니다.
    public void cropImage() {
        this.grantUriPermission("com.android.camera", photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 4);
            intent.putExtra("aspectY", 3);
            intent.putExtra("scale", true);
            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/test/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());

            photoUri = FileProvider.getUriForFile(WriteActivity.this,
                    "com.example.handylok.provider", tempFile);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission(res.activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }

    // 이미지 저장하기
    public byte[] getByteArray() {
        byte[] data = null;

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(bitmap, 128, 128);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, bs); //이미지가 클 경우 OutOfMemoryException 발생이 예상되어 압축
            data = bs.toByteArray();
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }

        return data;
    }

    // 이미지 가져오기
    public Bitmap getImage(byte[] b) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        return bitmap;
    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}
