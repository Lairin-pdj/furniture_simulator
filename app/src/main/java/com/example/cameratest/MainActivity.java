package com.example.cameratest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static CameraPreview surfaceView;
    private SurfaceHolder holder;
    private static Camera mCamera;
    private int RESULT_PERMISSIONS = 100;
    public static MainActivity getInstance;
    private RecyclerView recyclerView = null;
    public static Bitmap bm = null;
    private boolean isUp = false;
    private Animation translate_up;
    private Animation translate_down;

    //추후 조정
    //private Animation button_up;
    //private Animation button_down;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        //멀티 권한 설정
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        //권한 승인 여부 체크
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for(String permission : permissions){
                int result = PermissionChecker.checkSelfPermission(this, permission);
                if(result == PermissionChecker.PERMISSION_GRANTED){
                    //성공시
                }
                else{
                    doRequestPermissions();
                    break;
                }
            }
        }

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        translate_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_up);
        translate_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_down);
        //추후 조정
        //button_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_up);
        //button_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_down);
    }

    @Override
    protected void onResume() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        int check = 0;

        super.onResume();

        for(String permission : permissions) {
            int result = PermissionChecker.checkSelfPermission(this, permission);
            if (result == PermissionChecker.PERMISSION_GRANTED) {
                //성공시
                check++;
            }
        }
        //모든 권한 획득시 화면 출력
        if(check == permissions.length) {
            setInit();
        }
        setFurnitureList();
    }

    @Override
    protected void onPause() {
        if(mCamera != null) {
            mCamera.setPreviewCallback(null);
        }
        isUp = false;
        super.onPause();
    }

    public void setFurnitureList(){
        recyclerView = findViewById(R.id.furniture_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        CustomerAdapter adapter = new CustomerAdapter(getApplicationContext());
        adapter.addItem(new String("234dfasdf"));
        adapter.addItem(new String("fs235e2df"));
        adapter.addItem(new String("asd2f24ff"));
        adapter.addItem(new String("21f244g2f"));

        recyclerView.setAdapter(adapter);
    }

    public void furnitureMenuClick(View view){
        RecyclerView listView = (RecyclerView)findViewById(R.id.furniture_list);
        Button button = (Button)findViewById(R.id.Button2);
        TextView textView = (TextView)findViewById(R.id.furniture_text);
        Resources r = getResources();
        if(!isUp){
            //열기
            listView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            textView.bringToFront();
            listView.startAnimation(translate_up);
            textView.startAnimation(translate_up);
            button.setVisibility(View.GONE);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    button.setY(button.getY() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 235, r.getDisplayMetrics()));
                    button.setX(button.getX() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));
                    button.setVisibility(View.VISIBLE);
                }
            }, 500);
            isUp = true;
        }
        else{
            //닫기
            listView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            listView.startAnimation(translate_down);
            textView.startAnimation(translate_down);
            button.setVisibility(View.GONE);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    button.setY(button.getY() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 235, r.getDisplayMetrics()));
                    button.setX(button.getX() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));
                    button.setVisibility(View.VISIBLE);
                }
            }, 500);
            isUp = false;
        }
    }

    public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder>{
        ArrayList<String> items = new ArrayList<>();
        Context context;

        public CustomerAdapter(Context context){
            this.context = context;
        }

        @Override
        public int getItemCount(){
            return items.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = vi.inflate(R.layout.list_view, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position){
            String item = items.get(position);
            holder.setItem(item);
        }

        public void addItem(String item){
            items.add(item);
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageButton imageButton;
            TextView textView;

            public ViewHolder(@NonNull View itemView){
                super(itemView);

                textView = (TextView)itemView.findViewById(R.id.textView);

                imageButton = (ImageButton)itemView.findViewById(R.id.button);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, textView.getText(), Toast.LENGTH_SHORT).show();
                        furnitureMenuClick(null);
                    }
                });
            }

            public void setItem(String item){
                imageButton.setImageResource(R.drawable.furniture_vector_icons_111434);
                textView.setText(item);
            }
        }
    }

    public void createMenuClick(View view){
        Toast.makeText(getApplicationContext(), "click createMenuClick", Toast.LENGTH_SHORT).show();
    }

    public void captureClick(View view) throws InterruptedException {
        MediaActionSound sound = new MediaActionSound();

        //최적화를 위한 딜레이 발생
        if(mCamera != null) {
            surfaceView = (CameraPreview)findViewById(R.id.preview);
            mCamera.setPreviewCallback(surfaceView);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    screenShot(bm);
                    mCamera.setPreviewCallback(null);
                }
            }, 300);
        }

        sound.play(MediaActionSound.SHUTTER_CLICK);
        Toast.makeText(getApplicationContext(), "Screen Captured", Toast.LENGTH_SHORT).show();
    }

    public void screenShot(Bitmap bm){
        if(bm != null) {
            Bitmap overlay = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
            Canvas canvas = new Canvas(overlay);
            canvas.drawBitmap(bm, 0, 0, null);

            /*
            FrameLayout layout = (FrameLayout) findViewById(R.id.frame_layout);
            layout.buildDrawingCache();
            Bitmap bm1 = layout.getDrawingCache();
            canvas.drawBitmap(bm, 0, 0, null);
            */
            //만약 추가시 여기에 필터링

            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/testCapture");
            //폴더 생성
            if (!path.exists()) {
                path.mkdirs();
            }
            try {
                //날짜서식 지정 및 파일 저장
                SimpleDateFormat day = new SimpleDateFormat("yyMMddHHmmss");
                Date date = new Date();
                FileOutputStream os = new FileOutputStream(path + "/capture" + day.format(date) + ".jpg");
                overlay.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.close();
            } catch (IOException e) {
                Log.e("Save_Image", e.getMessage(), e);
            }
            //미디어 스캔
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(path));
            sendBroadcast(intent);
        }
        else{
            //시간을 변수로 잡아서 실패시 시간 증가 고려중
            AlertDialog.Builder alert  = new AlertDialog.Builder(this);
            alert.setTitle("사진촬영 실패");
            alert.setMessage("최적화 시간 증가 필요.");
            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alert.show();
        }
    }

    public static Camera getCamera(){
        return mCamera;
    }

    private void setInit(){
        getInstance = this;
        mCamera = Camera.open();

        setContentView(R.layout.activity_main);
        surfaceView = (CameraPreview)findViewById(R.id.preview);

        holder = surfaceView.getHolder();
        holder.addCallback(surfaceView);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void doRequestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        //거부된 권한 목록 파악 후 요청
        ArrayList<String> notGrantedPermissions = new ArrayList<>();
        for(String perm : permissions){
            if(PermissionChecker.checkSelfPermission(this, perm) == PermissionChecker.PERMISSION_DENIED){
                notGrantedPermissions.add(perm);
            }
        }
        ActivityCompat.requestPermissions(this, notGrantedPermissions.toArray(new String[]{}), 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] gramtResults){
        switch(requestCode){
            case 101:
                if(gramtResults.length > 0 && gramtResults[0] == PackageManager.PERMISSION_GRANTED){
                    //권한 허가시
                    setInit();
                }else {
                    //권한 거부시
                    AlertDialog.Builder alert  = new AlertDialog.Builder(this);
                    alert.setTitle("APP permissions");
                    alert.setMessage("해당 앱을 이용하시려면 권한이 필요합니다.");
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    });
                    alert.show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, gramtResults);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
    }
}