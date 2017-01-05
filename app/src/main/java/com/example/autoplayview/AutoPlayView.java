package com.example.autoplayview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class AutoPlayView extends RelativeLayout {
    private Context mContext;
    private ViewPager mPager;
    private LinearLayout dots;//计数小原点的布局
    private RelativeLayout indicator;//指示小原点
    private int width;//10dp（标准）
    private int dotWidth;//指示小原点的宽度
    private int dotDx;//指示小原点水平位移
    private LayoutParams mParams;//指示小原点布局参数（改变参数制作动画）
    private int current = 0;//当前页码
    private boolean canPlay;//可否播放
    private boolean isRunning;//是否正在播放；
    private int pageSize=0;//轮播数量
    private boolean isFirst = true;//第一次启动，为避免空指针问题，所以延迟0.2秒启动
    private Timer timer;
    private TimerTask timerTask;
    private float x;//用于判断是否为水平滑动
    private float y;//用于判断是否为竖直滑动
    private boolean isMove;//滑动不触发点击事件
    private long touchTime; //按下的时间
    private long hold;//按住了多久
    private ClickListener mListener;//点击事件

    public AutoPlayView(Context context) {
        this(context, null, 0);
    }

    public AutoPlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics());
        dotWidth = width;
        initView();
        initListener();

    }
    private void initView(){
       //图片容器ViewPager
       mPager = new ViewPager(mContext);
       RelativeLayout.LayoutParams lp01 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
       this.addView(mPager, lp01);
       //计数小圆点
       dots = new LinearLayout(mContext);
       dots.setId(R.id.auto_play_dots);//添加自定义id（res/value/ids）
       RelativeLayout.LayoutParams lp02 = new RelativeLayout.LayoutParams(
               RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
       lp02.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
       lp02.setMargins(0, 0, 0, width);
       lp02.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
       this.addView(dots, lp02);
       //指示小圆点
       indicator = new RelativeLayout(mContext);
       indicator.setBackgroundResource(R.drawable.dot_indicator);
       RelativeLayout.LayoutParams lp03 = new LayoutParams(width, width);
       lp03.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.auto_play_dots);//相对定位
       lp03.addRule(RelativeLayout.ALIGN_LEFT, R.id.auto_play_dots);//相对定位
       this.addView(indicator, lp03);
   }

    private void initListener() {
        mPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        hold = System.currentTimeMillis() - touchTime;
                        startPlay();
                        if (hold < 800 && mListener != null && !isMove) {//按住小于0.8秒，点击事件不为空，不是滑动状态，则出发点击
                            mListener.onClick(current);
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        touchTime = System.currentTimeMillis();
                        x = event.getRawX();
                        y = event.getRawY();
                        isMove = false;
                        stopPlay();
                        return true;//返回false则无法监听MotionEvent.ACTION_UP
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - x) > 8 || Math.abs(event.getRawY() - y )> 8) {//水平或竖直方向位移绝对值超过8像素视为滑动
                            isMove = true;
                        }
                    default://正常不会是running状态
                        if (isRunning) {
                            stopPlay();
                        }
                        break;
                }
                return false;
            }
        });
        mPager.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClick(current);
            }
        });
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //通过改变指示小圆点的宽度和水平位来达到滑动指示动画效果
                if (positionOffset < 0.5f) {
                    dotWidth = (int) (width + 2 * width * positionOffset / 0.5f);
                    dotDx = 3 * width * position;
                } else {
                    dotWidth = (int) (3 * width - 2 * width * (positionOffset - 0.5f) / 0.5f);
                    dotDx = 3 * width * position + 4 * width - dotWidth;
                }
                try {
                    if (mParams == null) {
                        mParams = (RelativeLayout.LayoutParams) indicator.getLayoutParams();
                    }
                    mParams.setMargins(dotDx, 0, 0, 0);
                    mParams.width = dotWidth;
                    indicator.setLayoutParams(mParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (mParams == null) {
                    mParams = (RelativeLayout.LayoutParams) indicator.getLayoutParams();
                }
                current = position;
                dotDx = 3 * width * position;
                mParams.setMargins(dotDx, 0, 0, 0);
                mParams.width = dotWidth;
                indicator.setLayoutParams(mParams);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //添加adapter时,尝试重启轮播
    public void setAdapter(PagerAdapter adapter) {
        mPager.setAdapter(adapter);
        pageSize = adapter.getCount();
        //添加计数小圆点
        for (int i = 0; i < pageSize; i++) {
            if (i > 0) {
                View view01 = new View(mContext);
                ViewGroup.LayoutParams lp01 = new ViewGroup.LayoutParams(2 * width, width);
                view01.setLayoutParams(lp01);
                dots.addView(view01);
            }
            View view02 = new View(mContext);
            ViewGroup.LayoutParams lp02 = new ViewGroup.LayoutParams(width, width);
            view02.setLayoutParams(lp02);
            view02.setBackgroundResource(R.drawable.dot_pager_count);
            dots.addView(view02);
        }
        if (isRunning) {
            stopPlay();
        }
        startPlay();
    }

    //开始轮播
    public synchronized void startPlay() {
        canPlay = (pageSize > 0 && mPager.getAdapter() != null);
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
        current = mPager.getCurrentItem();
        canPlay = false;
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
        } else if (hold > 4000) {
            wait = 2000;//抬手后重新轮播事件不少于2秒
        } else {
            wait = 6000 - hold;
        }
        try {
            timer.schedule(timerTask, wait, 6000);//轮播间隔6秒
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
                        mPager.setCurrentItem(current, true);
                        isFirst = false;
                    } catch (Exception e) {//首次播放失败则尝试重新播放
                        e.printStackTrace();
                        stopPlay();
                        isFirst = true;
                        startPlay();
                    }
                } else {
                    if (current < pageSize - 1) {
                        current++;
                    } else {
                        current = 0;
                    }
                    mPager.setCurrentItem(current, true);
                }
            }
        }
    };

    //添加点击事件（若对viewpager子布局添加点击事件会消费掉ACTION_DOWN，会导致OnTouchListener监听失效）
    public void setClickListener(ClickListener mListener) {
        this.mListener = mListener;
    }

    public ViewPager getViewPager() {
        return mPager;
    }
}
