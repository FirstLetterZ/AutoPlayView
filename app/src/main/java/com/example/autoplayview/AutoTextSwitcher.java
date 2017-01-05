package com.example.autoplayview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AutoTextSwitcher extends TextSwitcher {
    private List<String> textList = new ArrayList<String>();//播放内容列表
    private long hold;//按住了多久
    private boolean canPlay=false;//是否可播放
    private boolean isRunning = false;//是否正在播放
    private boolean isFirst = true;//首次启动
    private int current = 0;//播放开始页码
    private ClickListener listener;//点击事件
    private Timer timer;
    private TimerTask timerTask;
    private long touchTime; //按下的时间

    public AutoTextSwitcher(Context context) {
        this(context, null);
    }

    public AutoTextSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.in_from_bottom));
        this.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.out_to_top));
    }

    //开始轮播
    public synchronized void startPlay() {
        canPlay = (textList != null && textList.size() > 0);
        if (!isRunning && canPlay) {
            play();
        }
    }

    //停止轮播
    public void stopPlay() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        canPlay=false;
        isRunning = false;
    }

    //轮播
    public void play() {
        isRunning = true;
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(0);
                }
            };
        }
        long wait;
        if (isFirst) {
            wait = 200;
        } else if (hold > 3000) {
            wait = 2000;//抬手后重新轮播事件不少于2秒
        } else {
            wait = 5000 - hold;
        }
        try {
            timer.schedule(timerTask, wait, 5000);//轮播间隔5秒
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (canPlay && msg.what == 0) {
                if (isFirst) {
                    try {
                        setText(textList.get(current));
                        isFirst = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        stopPlay();
                        isFirst = true;
                        startPlay();
                    }
                } else {
                    if (current < textList.size() - 1) {
                        current++;
                    } else {
                        current = 0;
                    }
                    setText(textList.get(current));
                }
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                hold = System.currentTimeMillis() - touchTime;
                startPlay();
                if (hold < 800 && listener != null) {
                    listener.onClick(current);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                touchTime = System.currentTimeMillis();
                stopPlay();
                return true;//返回false则无法监听MotionEvent.ACTION_UP
            default://正常不会是running状态
                if (isRunning) {
                    stopPlay();
                }
                break;
        }
        return false;
    }
    //设置轮播文字列表时根据内容,停止或启动轮播
    public void setTextList(List<String> textList) {
        this.textList.clear();
        if (textList == null || textList.size() == 0) {
            setText(null);
            stopPlay();
        } else {
            this.textList.addAll(textList);
            if(isRunning){
                stopPlay();
            }
            startPlay();
        }
    }
    //添加点击事件
    public void setListener(ClickListener listener) {
        this.listener = listener;
    }
}

