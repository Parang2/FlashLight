package com.example.help.nopermissionflashlight;

import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;


/*
1. 안드로이드에 플래시 기능이 있는지 확인한다
   (1)없을 경우
      -토스트 메세지를 띄운후 3초뒤 자동 종료
   (2)있을 경우
      계속-----------
2. 폴더의 이미지를 불러온다
3. 후면에 플래시가 있을 때 카메라 id 값을 String 변수에 세팅한다
4. 해당 이미지 버튼을 눌렀을 경우 플래시 On/OFF
5. 에러가 발생했을 경우 근원지를 찾아 출력한다
*/



public class MainActivity extends AppCompatActivity {
    private ImageButton mImageButtonFlashOnOff;
    private boolean mFlashOn;

    //!< 안드로이드 플래시 기능이 CameraManager 안에 포함되어 있어 플래시 기능을 쓰기 위해서 선언
    private CameraManager mCameraManager;   //!<
    private String mCameraId;
    private long backBtnTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //!<카메라 플래시가 없을 경우
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            //!<플래시가 없다는 토스트 메세지를 띄운다
            Toast.makeText(getApplicationContext(), "There is no camera flash.\n The app will finish!", Toast.LENGTH_LONG).show();

            //!<안드로이드에서 사용할 수 있는 스레드 통신 방법중 하나이다 (Handler)
            //!<앞의과정에 조금의 딜레이를 주고 싶을 때 사용한다 (.postDelayed)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);   //3초후 자동 종료
            return;
        }

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);  //!<모든 카메라 장치의 관리자 시스템 카메라를 여닫는다
        //!<xml과 액티비티를 연결
        mImageButtonFlashOnOff = findViewById(R.id.ibFlashOnOff);
        mImageButtonFlashOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            //!<클릭할때
            public void onClick(View v) {
                flashlight();
                //!<res/drawable/ 폴더에 있는 이미지를 로드하여 출력한다
                mImageButtonFlashOnOff.setImageResource(mFlashOn ? R.drawable.btn_on : R.drawable.btn_off);
            }
        });
    }

    void flashlight() {
        if (mCameraId == null) {
            try {
                for (String id : mCameraManager.getCameraIdList()) {
                    CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
                    //!<사용가능한 플래시가 있는지 없는지 판단
                    Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    //!<Integer 는 Int 와 달리 null 값을 처리할 수 있다
                    Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                    //!< 플래시하드웨어가 있으며 사용 가능하고 후면에 플래시가 있을 때 카메라 id 값을 String 변수에 세팅한다
                    if (flashAvailable != null && flashAvailable && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        mCameraId = id;
                        break;  //!< 이후 반복문 탈출
                    }
                }
            } catch (CameraAccessException e) {
                mCameraId = null;
                //!<에러메세지의 발생 근원지를 찾아 출력
                e.printStackTrace();
                return;
            }
        }

        mFlashOn = !mFlashOn;

        try {
            //플래시 켜기 : setTorchMode(cameraId, true)
            //플래시 끄기 : setTorchMode(cameraId, false)
            mCameraManager.setTorchMode(mCameraId, mFlashOn);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;
        //!<만약 뒤로가기버튼을 한번 누른뒤 2초 이내에 한번 더 누를 경우
        if (0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
            //종료
        } else {
            backBtnTime = curTime;
            //토스트 메세지 띄움
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
