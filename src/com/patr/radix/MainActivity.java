package com.patr.radix;

import java.io.File;
import java.io.InvalidClassException;

import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.patr.radix.adapter.KeyListAdapter2;
import com.patr.radix.ble.BluetoothLeService;
import com.patr.radix.ui.unlock.MyKeysActivity;
import com.patr.radix.ui.view.ListSelectDialog;
import com.patr.radix.ui.view.dialog.MsgDialog;
import com.patr.radix.utils.Constants;
import com.patr.radix.utils.TabDb;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.ECContentObservers;
import com.yuntongxun.ecdemo.common.utils.CrashHandler;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.FileAccessor;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
        OnTabChangeListener, OnItemClickListener {

    private Context mContext;

    private FragmentTabHost tabHost;

    private KeyListAdapter2 adapter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("MainActivity", action);
            if (Constants.ACTION_RELEASE_CALL.equals(action)) {
                final String callNumber = intent.getStringExtra("callNumber");
                MsgDialog.show(mContext, "提示", "您收到开门申请，请选择", "立即开门", "发送钥匙",
                        "取消", new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // 立即开门
                                ListSelectDialog.show(mContext, "请选择门禁",
                                        adapter, MainActivity.this);
                            }
                        }, new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                MyKeysActivity.startAfterIM(MainActivity.this,
                                        callNumber);
                            }
                        }, new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                            }
                        });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        tabHost = (FragmentTabHost) super.findViewById(android.R.id.tabhost);
        tabHost.setup(this, super.getSupportFragmentManager(),
                R.id.contentLayout);
        tabHost.getTabWidget().setDividerDrawable(null);
        tabHost.setOnTabChangedListener(this);
        initTab();
        registerReceiver();
        adapter = new KeyListAdapter2(this, MyApplication.instance.getLocks());

        // 云通讯初始化
        CCPAppManager.setContext(MyApplication.instance);
        FileAccessor.initFileAccess();
        setChattingContactId();
        initImageLoader();
        // CrashHandler.getInstance().init(MyApplication.instance);
        SDKInitializer.initialize(MyApplication.instance);
        ECContentObservers.getInstance().initContentObserver();
        CrashHandler.getInstance().setContext(this);
    }

    private void initTab() {
        int tabs[] = TabDb.getTabsTxt();
        for (int i = 0; i < tabs.length; i++) {
            TabSpec tabSpec = tabHost.newTabSpec(
                    getResources().getString(tabs[i])).setIndicator(
                    getTabView(i));
            tabHost.addTab(tabSpec, TabDb.getFragments()[i], null);
            tabHost.setTag(i);
        }
    }

    private View getTabView(int idx) {
        View view = LayoutInflater.from(this).inflate(R.layout.footer_tabs,
                null);
        int defaultTab = 0;
        ImageView iv = (ImageView) view.findViewById(R.id.iv);
        TextView tv = (TextView) view.findViewById(R.id.tv);
        TextView badge = (TextView) view.findViewById(R.id.badge);
        tv.setText(TabDb.getTabsTxt()[idx]);
        // TODO 设置badge
//        if (idx == 2) {
//            badge.setText("99");
//            badge.setVisibility(View.VISIBLE);
//        } else {
//            badge.setVisibility(View.GONE);
//        }
        if (idx == defaultTab) {
            iv.setImageResource(TabDb.getTabsImgLight()[idx]);
            tv.setTextColor(getResources().getColor(
                    R.color.buttombar_text_selected));
        } else {
            iv.setImageResource(TabDb.getTabsImg()[idx]);
            tv.setTextColor(getResources().getColor(R.color.buttombar_text));
        }
        return view;
    }

    @Override
    public void onTabChanged(String tabId) {
        updateTab();

    }

    private void updateTab() {
        TabWidget tabw = tabHost.getTabWidget();
        for (int i = 0; i < tabw.getChildCount(); i++) {
            View view = tabw.getChildAt(i);
            ImageView iv = (ImageView) view.findViewById(R.id.iv);
            TextView tv = (TextView) view.findViewById(R.id.tv);
            if (i == tabHost.getCurrentTab()) {
                iv.setImageResource(TabDb.getTabsImgLight()[i]);
                tv.setTextColor(getResources().getColor(
                        R.color.buttombar_text_selected));
            } else {
                iv.setImageResource(TabDb.getTabsImg()[i]);
                tv.setTextColor(getResources().getColor(R.color.buttombar_text));
            }

        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_RELEASE_CALL);
        registerReceiver(receiver, filter);
    }

    /**
     * 保存当前的聊天界面所对应的联系人、方便来消息屏蔽通知
     */
    private void setChattingContactId() {
        try {
            ECPreferences.savePreference(
                    ECPreferenceSettings.SETTING_CHATTING_CONTACTID, "", true);
        } catch (InvalidClassException e) {
            e.printStackTrace();
        }
    }

    private void initImageLoader() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(
                getApplicationContext(), "ECSDK_Demo/image");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).threadPoolSize(1)
                // 线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCache(new WeakMemoryCache())
                // .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(CCPAppManager.md5FileNameGenerator)
                // 将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCache(
                        new UnlimitedDiscCache(cacheDir, null,
                                CCPAppManager.md5FileNameGenerator))// 自定义缓存路径
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                // .writeDebugLogs() // Remove for release app
                .build();// 开始构建
        ImageLoader.getInstance().init(config);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        BluetoothLeService.close();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.disable();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
     * .AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        MyApplication.instance.setSelectedLock(adapter.getItem(position));
    }

}
