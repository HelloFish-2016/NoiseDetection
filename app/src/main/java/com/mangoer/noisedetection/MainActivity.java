package com.mangoer.noisedetection;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mangoer.noisedetection.recorder.NoiseRecorder;
import com.mangoer.noisedetection.view.NoiseChartline;
import com.mangoer.noisedetection.view.NoiseboardView;

import org.achartengine.GraphicalView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity{


    @Bind(R.id.text_vip)//噪音等级
    TextView text_vip;
    @Bind(R.id.bt_start)
    Button bt_start;
    @Bind(R.id.chart)
    LinearLayout chart;
    @Bind(R.id.left_temperature_curve)
    LinearLayout left_temperature_curve;
    @Bind(R.id.noiseboardView)
    NoiseboardView dashboardView;

    private GraphicalView mView;
    private NoiseChartline mService;
    private NoiseRecorder media;

    private float     degree = 0.0f;  //记录指针旋转
    private boolean   is_start=true;

    List<Integer> degreeList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        ButterKnife.bind(this);
//        init();
        media=new NoiseRecorder(handler);
        bt_start.setText("开始测试");
        setChartLineView();
    }

    private void init() {

        View rootView = findViewById(R.id.rl_root);

        RotateAnimation rotateAnima = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnima.setDuration(1000);// 设置动画持续时间
        rotateAnima.setFillAfter(true);// 设置动画执行完毕时, 停留在完毕的状态下.

        ScaleAnimation scaleAnima = new ScaleAnimation(
                0, 1,
                0, 1,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnima.setDuration(1000);
        scaleAnima.setFillAfter(true);

        AlphaAnimation alphaAnima = new AlphaAnimation(0, 1);
        alphaAnima.setDuration(2000);
        alphaAnima.setFillAfter(true);


        // 把三个动画合在一起, 组成一个集合动画
        AnimationSet setAnima = new AnimationSet(false);
        setAnima.addAnimation(rotateAnima);
        setAnima.addAnimation(scaleAnima);
        setAnima.addAnimation(alphaAnima);

        rootView.startAnimation(setAnima);
    }


    private void setChartLineView() {
        mService=new NoiseChartline(this);
        mService.setXYMultipleSeriesDataset("分贝图");
        mService.setXYMultipleSeriesRenderer(150, "分贝图", "时间（S）", "分贝数值");
        mView = mService.getGraphicalView();
        left_temperature_curve.addView(mView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    Handler handler=new Handler(){

        @SuppressLint("HandlerLeak")
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0X00:
                    if ("-Infinity".equals( msg.obj.toString())) {
                        degree=0f;
                    }else {
                        degree =(Float.parseFloat( msg.obj.toString()));//获取到的值
                    }

                    mService.updateChart(degree);

                    String subString;
                    //数据采集格式化
                    if (degree>100) {
                        subString=String.valueOf(degree).substring(0, 3);
                    } else if (degree<10) {
                        subString=String.valueOf(degree).substring(0, 1);
                    }else{
                        subString=String.valueOf(degree).substring(0, 2);
                    }
                    Integer integer=Integer.parseInt(subString);
                    if (integer != 0)
                        degreeList.add(integer);

                    if (dashboardView != null)
                        dashboardView.setRealTimeValue(integer);
                    break;
                default:
                    break;
            }
        }
    };

    @OnClick(R.id.bt_start)
    public void start(){
        if (!chart.isShown()) {
            chart.setVisibility(View.VISIBLE);
        }
        if (is_start) {
            degreeList.clear();
            bt_start.setText("停止测试");
            media.startRecord();
            is_start=false;
        }else {
            Collections.sort(degreeList);
            float total = 0;
            for(int i=0; i<degreeList.size(); i++){
                total += degreeList.get(i);
            }
            if (degreeList.size()>0) {
                text_vip.setText("最小值："+degreeList.get(0)+",最大值："+degreeList.get(degreeList.size()-1) +",平均值："+total/degreeList.size());
            }

            bt_start.setText("开始测试");
            media.stopRecord();
            is_start=true;
        }
    }



    private long firstTime;
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstTime < 3000) {
            ButterKnife.unbind(this);
            finish();
        } else {
            firstTime = System.currentTimeMillis();
            Toast.makeText(this, "连续按两次退出程序", Toast.LENGTH_LONG).show();
        }
    }

}
