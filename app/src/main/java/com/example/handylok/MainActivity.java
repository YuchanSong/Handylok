package com.example.handylok;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    CustomAdapter listAdapter;
    DBAdapter db;
    boolean dbOpen;
    Cursor currentCursor;
    int nowIndex;

    private final static int addRequestCode = 100;
    private final static int modifyRequestCode = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // make and open database
        db = new DBAdapter(context);
        db.open();
        dbOpen = true;

        // get column index
        currentCursor = db.fetchAllData();

        // setup custom listview
        ListView listView = findViewById(R.id.listView);
        listAdapter = new CustomAdapter(this);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);
    }

    protected void onResume() {
        super.onResume();

        if (!dbOpen) {
            db.open();
            dbOpen = true;
        }
        currentCursor = db.fetchAllData();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (dbOpen) {
            db.close();
            dbOpen = false;
        }
    }

    // addButton Click Listner (Add - RequestCode 100)
    public void clickAdd(View view) {
        startActivityForResult(new Intent(context, WriteActivity.class).putExtra("MainRequestCode", addRequestCode), addRequestCode);
    }

    // list Item Click Listener (Modify - RequestCode 500)
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            currentCursor.moveToPosition(position);
            nowIndex = currentCursor.getInt(0);
            String name = String.valueOf(currentCursor.getString(1));
            String place = String.valueOf(currentCursor.getString(2));
            String date = String.valueOf(currentCursor.getString(3));
            String contexts = String.valueOf(currentCursor.getString(4));

            Intent intent = new Intent(context, WriteActivity.class);
            intent.putExtra("_id", nowIndex);
            intent.putExtra("name", name);
            intent.putExtra("place", place);
            intent.putExtra("date", date);
            intent.putExtra("contexts", contexts);
            intent.putExtra("MainRequestCode", modifyRequestCode);

            startActivityForResult(intent, modifyRequestCode);
        }
    };

    // list Item Long Click Listener (Delete)
    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            final SweetAlertDialog dDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
            dDialog.setTitleText("정말 지우시겠습니까?");
            dDialog.setContentText("저장된 모든 내용이 제거됩니다.");
            dDialog.setCancelText("안 지울래요");
            dDialog.setConfirmText("지울게요!");
            dDialog.showCancelButton(true);
            dDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    sDialog.cancel();
                }
            });
            dDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    currentCursor.moveToPosition(position);
                    nowIndex = currentCursor.getInt(0); // id 열
                    db.delData(nowIndex);
                    currentCursor = db.fetchAllData();
                    listAdapter.notifyDataSetChanged();

                    dDialog.showCancelButton(false);
                    sDialog
                            .setTitleText("삭제되었습니다!")
                            .setContentText("등록된 정보를 깔끔하게 지웠습니다.")
                            .setConfirmText("확인")
                            .setConfirmClickListener(null)
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                }
            });
            dDialog.show();

            return true; // 다음 이벤트 계속 진행 false, 이벤트 완료 true
        }
    };

    public class CustomAdapter extends BaseAdapter {
        Context my_context;

        CustomAdapter(Context context) {
            my_context = context;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getCount() {
            return currentCursor.getCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHolder viewHolder;

            // 캐시된 뷰가 없을 경우 새로 생성
            if (convertView == null) { //Scroll in
                LayoutInflater inflater = (LayoutInflater) my_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_row, parent, false);

                viewHolder = new ListViewHolder();
                viewHolder.nameView = convertView.findViewById(R.id.nameView);
                viewHolder.dateView = convertView.findViewById(R.id.dateView);
                convertView.setTag(viewHolder);
                // 캐시된 뷰가 있을 경우 저장된 뷰홀더 사용
            } else { //Scroll out
                // findViewById를 이용하지 않기위해 getTag, setTag 이용
                viewHolder = (ListViewHolder)convertView.getTag();
            }

            currentCursor.moveToPosition(position);
            viewHolder.nameView.setText(currentCursor.getString(1)); // 이름 열
            viewHolder.dateView.setText(currentCursor.getString(3)); // 일시 열

            return convertView;
        }

        private class ListViewHolder {
            TextView nameView;
            TextView dateView;
        };
    }

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

}
