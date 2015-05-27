package kr.ftlab.samples.temphumicheckertest;

import kr.ftlab.lib.SmartSensor;
import kr.ftlab.lib.SmartSensorEventListener;
import kr.ftlab.lib.SmartSensorResultMDI;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SmartSensorEventListener {
    private SmartSensor mMI = null;
    private SmartSensorResultMDI mResultMDI;

    private Button btnStart;
    private TextView txtResult01;
    private TextView txtResult02;

    int mProcess_Status = 0;
    int Process_Stop = 0;
    int Process_Start = 1;

    BroadcastReceiver mHeadSetConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG)) { // 이어폰 단자에 센서 결함 유무 확인
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {//센서 분리 시
                        stopSensing();
                        btnStart.setEnabled(false);//센서가 분리되면 START/STOP 버튼 비활성화, 클릭 불가
                        Toast.makeText(MainActivity.this,"Sensor not found", Toast.LENGTH_SHORT).show();
                    } else if (intent.getIntExtra("state", 0) == 1) {//센서 결합 시
                        Toast.makeText(MainActivity.this,"Sensor found", Toast.LENGTH_SHORT).show();
                        btnStart.setEnabled(true);//센서가 연결되면 START/STOP 버튼 활성화
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnStart = (Button) findViewById(R.id.button_on);
        txtResult01 = (TextView) findViewById(R.id.textresult01);
        txtResult02 = (TextView) findViewById(R.id.textresult02);

        mMI = new SmartSensor(MainActivity.this, this);
        mMI.selectDevice(SmartSensor.MDI);
    }

    public void mOnClick(View v) {
        if (mProcess_Status == Process_Start) {//버튼 클릭 시의 상태가 Start 이면 stop process 수행
            stopSensing();
        }
        else {//버튼 클릭 시의 상태가 Stop 이면 start process 수행
            startSensing();
        }
    }

    public void startSensing() {
        btnStart.setText("STOP");
        mProcess_Status = Process_Start;//현재 상태를 start로 설정
        mMI.start();//측정 시작
    }

    public void stopSensing() {
        btnStart.setText("START");
        mProcess_Status = Process_Stop;//현재 상태를 stop로 설정
        mMI.stop();//측정 종료
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //상단 메뉴 버튼 생성
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intflt = new IntentFilter();
        intflt.addAction(Intent.ACTION_HEADSET_PLUG);
        this.registerReceiver(mHeadSetConnectReceiver, intflt);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mHeadSetConnectReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() { //앱 종료 시 수행
        mMI.quit();
        finish();
        System.exit(0);
        super.onDestroy();
    }

    @Override
    public void onMeasured() {
        String str ="";
        mResultMDI = mMI.getResultMDI(); // 측정된 값을 가져옴

        switch(mResultMDI.MDI_Type){    // MDI Type에 따라 측정값의 의미가 다르다.
            case SmartSensor.MDI_TEMP : // 온도센서인 경우
                // default는 섭씨 온도(℃)이므로 화씨 온도(℉)를 사용하고 싶은경우 아래 주석처리한 코드를 사용하세요.
                // mResultMDI.MDI_Value = (mResultMDI.MDI_Value * 1.8) + 32;
                // mResultMDI.MDI_Unit= ℉
                str = String.format("%1.1f", mResultMDI.MDI_Value)+mResultMDI.MDI_Unit;
                txtResult01.setText(str);
                break;
            case SmartSensor.MDI_HUMI : // 습도센서인 경우
                str = String.format("%1.1f", mResultMDI.MDI_Value)+mResultMDI.MDI_Unit;
                txtResult02.setText(str);
                break;
        }
    }

    @Override
    public void onSelfConfigurated() {
        mProcess_Status = 0;
        btnStart.setText("START");
    }
}
