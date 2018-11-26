package jp.ac.doshisha.mikilab.huetimerforcontrol;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    int minute = 0, second = 0;
    int start_flag = 0;
    int count;
    int countup_flag = 0;
    int demo_flag = 0;
    int buttonNum = 0;
    long countNumber;
    long interval = 100;

    private TextView timerText;
    private Timer timer;
    private Handler handler = new Handler();
    private CountDown countDown;
    Button sb;

    private String address = "172.20.11.98";
    //private String address = "192.168.1.78";
    private int port = 8080;

    private static final int REQUESTCODE_TEST = 1;


    //起動時
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        minute = 0;
        timerText = findViewById(R.id.timer);
        timerText.setText(String.format("%1$02d:%2$02d", minute, second));

    }

    //ボタン押した時
    public void onButtonClick(View v){

        switch (v.getId()){
            case R.id.button_01:
                if(start_flag == 0 && demo_flag == 0) {
                    thread();
                    buttonNum = 1;
                    minute += 1;
                    timerText.setText(String.format("%1$02d:%2$02d", minute, second));
                }
                break;

            case R.id.button_05:
                if(start_flag == 0 && demo_flag == 0) {
                    thread();
                    buttonNum = 5;
                    minute += 5;
                    timerText.setText(String.format("%1$02d:%2$02d", minute, second));
                }
                break;

            case R.id.button_10:
                if(start_flag == 0 && demo_flag == 0) {
                    thread();
                    buttonNum = 10;
                    minute += 10;
                    timerText.setText(String.format("%1$02d:%2$02d", minute, second));
                }
                break;

            case R.id.button_15:
                if(start_flag == 0 && demo_flag == 0) {
                    thread();
                    buttonNum = 15;
                    minute += 15;
                    timerText.setText(String.format("%1$02d:%2$02d", minute, second));
                }
                break;

            case R.id.demo_button:
                if(start_flag == 0) {
                    thread();
                    buttonNum = 30;
                    if(start_flag == 0) {
                        sb = findViewById(R.id.demo_button);
                        if(demo_flag == 0) {
                            demo_flag = 1;
                            minute = 0;
                            second = 10;
                            timerText.setText(String.format("%1$02d:%2$02d", minute, second));
                            sb.setText("デモ中");
                        }
                    }
                    break;
                }
                break;

            case R.id.clear_button:
                if(start_flag == 0) {
                    thread();
                    buttonNum = 0;
                    countup_flag = 0;
                    minute = 0; second = 0;
                    timerText.setText(String.format("%1$02d:%2$02d", minute, second));
                    timerText.setTextColor(Color.parseColor("#40C4FF"));
                    if(demo_flag == 1){
                        sb = findViewById(R.id.demo_button);
                        sb.setText("デモ用");
                        demo_flag = 0;
                    }
                }
                break;

            case R.id.start_button:

                sb = findViewById(R.id.start_button);
                buttonNum = 50;

                thread();

                if(start_flag == 0){
                    if(countup_flag == 0) {
                        countNumber = (minute*60+second)*1000;
                        countDown = new CountDown(countNumber, interval);
                        countDown.start();
                    }else {
                        countDown.countup();
                    }

                    sb.setText("Stop");
                    start_flag = 1;
                }
                else{
                    countDown.cancel();
                    if(countup_flag == 1){
                        timer.cancel();
                    }
                    sb.setText("Start");
                    start_flag = 0;
                }
                break;
        }
    }

    //カウントダウン
    public class CountDown extends CountDownTimer {

        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // カウントダウン完了後に呼ばれる
            count = 0;
            countup_flag = 1;
            countup();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // インターバル(countDownInterval)毎に呼ばれる
            minute = (int)millisUntilFinished/1000/60;  second = (int)millisUntilFinished/1000%60;
            timerText.setText(String.format("%1$02d:%2$02d", minute, second));
        }

        //カウントアップ
        public void countup(){
            timer = new Timer();
            timerText.setTextColor(Color.RED);

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    // handlerdを使って処理をキューイングする
                    handler.post(new Runnable() {
                        public void run() {
                            count++;
                            minute = count * 100 / 1000 / 60;   second = count * 100 / 1000 % 60;
                            timerText.setText(String.format("%1$02d:%2$02d", minute, second));
                        }
                    });
                }
            }, 0, interval);
        }

    }

    void thread(){

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // サーバーへ接続
                    Socket socket = new Socket(address, port);

                    OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
                    BufferedWriter bw = new BufferedWriter(out);

                    String message = String.valueOf(minute);
                    message += ",";
                    message += String.valueOf(second);
                    message += ",";
                    message += String.valueOf(start_flag);
                    message += ",";
                    message += String.valueOf(buttonNum);
                    message += ",";
                    message += String.valueOf(countup_flag);
                    message += ",";
                    message += String.valueOf(count);
                    message += ",";
                    message += String.valueOf(demo_flag);
                    bw.write(message);
                    bw.newLine();
                    bw.flush();

                    Log.w("aaaaaaaaaaaaa",message);

                    socket.close();

                }catch( IOException e ) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            intent.putExtra("address", address);
            startActivityForResult(intent,REQUESTCODE_TEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //SettingActivityから値を持ってくる
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_TEST:
                if (RESULT_OK == resultCode) {
                    address = data.getStringExtra("address");
                }
                break;
        }
    }
}
