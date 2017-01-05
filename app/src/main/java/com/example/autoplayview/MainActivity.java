package com.example.autoplayview;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private List<ImageView> iList = new ArrayList<>();//图片列表
    private List<String> tList = new ArrayList<>();//文字列表
    private AutoPlayView autoplayview;
    private AutoTextSwitcher autoTextSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        autoplayview = (AutoPlayView) findViewById(R.id.auto_iv);
        autoTextSwitcher = (AutoTextSwitcher) findViewById(R.id.auto_tv);
        initList();
        autoplayview.setAdapter(mAdapter);
        autoplayview.setClickListener(new ClickListener() {
            @Override
            public void onClick(int i) {
                Toast.makeText(MainActivity.this, "点击第" + (i + 1) + "个视图", Toast.LENGTH_SHORT).show();
            }
        });
        autoTextSwitcher.setTextList(tList);
        //设置轮播字体格式
        autoTextSwitcher.setFactory( new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView txt = new TextView(MainActivity.this);
                txt.setMaxLines(1);//单行
                txt.setEllipsize(TextUtils.TruncateAt.END);//结尾省略
                txt.setLayoutParams(new TextSwitcher.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                txt.setTextColor(Color.BLACK);
                txt.setGravity(Gravity.CENTER_VERTICAL);//竖直居中
                txt.setTextSize(20);
                return txt;
            }
        });
        autoTextSwitcher.setListener(new ClickListener() {
            @Override
            public void onClick(int i) {
                Toast.makeText(MainActivity.this, "点击第" + (i + 1) + "条信息", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initList() {
        ImageView iv01 = new ImageView(this);
        iv01.setImageResource(R.drawable.image_01);
        iv01.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iList.add(iv01);
        ImageView iv02 = new ImageView(this);
        iv02.setImageResource(R.drawable.image_02);
        iv02.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iList.add(iv02);
        ImageView iv03 = new ImageView(this);
        iv03.setImageResource(R.drawable.image_03);
        iv03.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iList.add(iv03);
        ImageView iv04 = new ImageView(this);
        iv04.setImageResource(R.drawable.image_04);
        iv04.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iList.add(iv04);
        for (int i = 0; i < 5; i++) {
            tList.add("第" + (i + 1) + "条测试信息,测试信息详细内容：" +
                    "北国风光，千里冰封，万里雪飘。" +
                    "望长城内外，惟馀莽莽；大河上下，顿失滔滔。" +
                    "山舞银蛇，原驰蜡象，欲与天公试比高。" +
                    "须晴日，看红妆素裹，分外妖娆。");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoplayview.startPlay();
        autoTextSwitcher.startPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        autoplayview.stopPlay();
        autoTextSwitcher.stopPlay();
    }

    private PagerAdapter mAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return iList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            container.addView(iList.get(position));
            return iList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(iList.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    };
}
