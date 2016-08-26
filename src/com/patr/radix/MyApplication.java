package com.patr.radix;

import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.text.TextUtils;

import java.io.File;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import org.xutils.x;

import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.patr.radix.bean.Community;
import com.patr.radix.bean.GetCommunityListResult;
import com.patr.radix.bean.GetLockListResult;
import com.patr.radix.bean.MService;
import com.patr.radix.bean.RadixLock;
import com.patr.radix.bean.UserInfo;
import com.patr.radix.bll.CacheManager;
import com.patr.radix.bll.GetCommunityListParser;
import com.patr.radix.bll.GetLockListParser;
import com.patr.radix.network.RequestListener;
import com.patr.radix.network.WebService;
import com.patr.radix.utils.Constants;
import com.patr.radix.utils.PrefUtil;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.CrashHandler;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.FileAccessor;

public class MyApplication extends Application {

    /**
     * 打包发布要改为false
     */
    public static final boolean DEBUG = false;
    
    public static final boolean TEST = true;

    private final List<MService> services = new ArrayList<MService>();
    private final List<BluetoothGattCharacteristic> characteristics = new ArrayList<BluetoothGattCharacteristic>();
    
    private final List<Community> communities = new ArrayList<Community>();
    
    private final List<RadixLock> locks = new ArrayList<RadixLock>();
    
    private final List<RadixLock> selectedLocks = new ArrayList<RadixLock>();
    
    private RadixLock selectedLock;
    
    private Community selectedCommunity;
    
    private String selectedCommunityId;
    
    private String selectedLockId;
    
    private UserInfo userInfo = new UserInfo();
    
    private String mName;
    
    private String cardNum = "FF FF FF FF ";
    
    private String csn;

    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(DEBUG);
        instance = this;
        // 从缓存读取用户信息
        userInfo = PrefUtil.getUserInfo(instance);
//        if (DEBUG) {
//            if (TextUtils.isEmpty(userInfo.getAccount())) {
//                userInfo = new UserInfo();
//                userInfo.setAccount("admin");
//                userInfo.setName("admin");
//                userInfo.setId("0");
//            }
//        }
        // 从缓存读取小区列表和当前选择小区
        getCommunityListFromCache();
        // 从缓存读取门禁钥匙列表和当前选择门禁钥匙
        getLockListFromCache();
        
        CCPAppManager.setContext(instance);
        FileAccessor.initFileAccess();
        setChattingContactId();
        initImageLoader();
        CrashHandler.getInstance().init(this);
        SDKInitializer.initialize(instance);
    }
    
    private void getCommunityListFromCache() {
        CacheManager.getCacheContent(instance, CacheManager.getCommunityListUrl(),
                new RequestListener<GetCommunityListResult>() {

                    @Override
                    public void onSuccess(int stateCode,
                            GetCommunityListResult result) {
                        if (result != null) {
                            setCommunities(result.getCommunities());
                            if (getSelectedCommunityId() != null) {
                                for (Community community : getCommunities()) {
                                    if (selectedCommunityId.equals(community.getId())) {
                                        setSelectedCommunity(community);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                }, new GetCommunityListParser());
    }
    
    private void getLockListFromCache() {
        if (getSelectedCommunity() != null) {
            CacheManager.getCacheContent(instance, CacheManager.getLockListUrl(), 
                    new RequestListener<GetLockListResult>() {
    
                        @Override
                        public void onSuccess(int stateCode,
                                GetLockListResult result) {
                            if (result != null) {
                                setLocks(result.getLocks());
                                if (getSelectedLockId() != null) {
                                    for (RadixLock lock : getLocks()) {
                                        if (selectedLockId.equals(lock.getId())) {
                                            setSelectedLock(lock);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        
                    }, new GetLockListParser());
        }
    }

    /**
     * 保存当前的聊天界面所对应的联系人、方便来消息屏蔽通知
     */
    private void setChattingContactId() {
        try {
            ECPreferences.savePreference(ECPreferenceSettings.SETTING_CHATTING_CONTACTID, "", true);
        } catch (InvalidClassException e) {
            e.printStackTrace();
        }
    }

    private void initImageLoader() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "ECSDK_Demo/image");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .threadPoolSize(1)//线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCache(new WeakMemoryCache())
                // .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(CCPAppManager.md5FileNameGenerator)
                        // 将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCache(new UnlimitedDiscCache(cacheDir ,null ,CCPAppManager.md5FileNameGenerator))//自定义缓存路径
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                // .writeDebugLogs() // Remove for release app
                .build();//开始构建
        ImageLoader.getInstance().init(config);
    }

    public List<MService> getServices() {
        return services;
    }

    public void setServices(List<MService> services) {
        this.services.clear();
        this.services.addAll(services);
    }

    public List<BluetoothGattCharacteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<BluetoothGattCharacteristic> characteristics) {
        this.characteristics.clear();
        this.characteristics.addAll(characteristics);
    }

    public List<RadixLock> getLocks() {
        return locks;
    }
    
    public void setLocks(List<RadixLock> locks) {
        this.locks.clear();
        this.locks.addAll(locks);
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getName() {
        if (TextUtils.isEmpty(mName)) {
            mName = PrefUtil.getString(instance, Constants.PREF_NAME);
        }
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public RadixLock getSelectedLock() {
        return selectedLock;
    }

    public void setSelectedLock(RadixLock selectedLock) {
        this.selectedLock = selectedLock;
        if (selectedLock != null) {
            PrefUtil.save(instance, Constants.PREF_SELECTED_KEY, selectedLock.getId());
        } else {
            PrefUtil.save(instance, Constants.PREF_SELECTED_KEY, null);
        }
    }
    
    public String getSelectedLockId() {
        if (TextUtils.isEmpty(selectedLockId)) {
            selectedLockId = PrefUtil.getString(instance, Constants.PREF_SELECTED_KEY);
        }
        return selectedLockId;
    }

    public Community getSelectedCommunity() {
        return selectedCommunity;
    }

    public void setSelectedCommunity(Community selectedCommunity) {
        this.selectedCommunity = selectedCommunity;
        if (selectedCommunity != null) {
            WebService.URL = selectedCommunity.getUrl();
            PrefUtil.save(instance, Constants.PREF_SELECTED_COMMUNITY, selectedCommunity.getId());
        } else {
            PrefUtil.save(instance, Constants.PREF_SELECTED_COMMUNITY, null);
        }
    }
    
    public String getSelectedCommunityId() {
        if (TextUtils.isEmpty(selectedCommunityId)) {
            selectedCommunityId = PrefUtil.getString(instance, Constants.PREF_SELECTED_COMMUNITY);
        }
        return selectedCommunityId;
    }

    public List<Community> getCommunities() {
        return communities;
    }
    
    public void setCommunities(List<Community> communities) {
        this.communities.clear();
        this.communities.addAll(communities);
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getCsn() {
        return csn;
    }

    public void setCsn(String csn) {
        this.csn = csn;
    }

    public List<RadixLock> getSelectedLocks() {
        return selectedLocks;
    }
    
    public void setSelectedLocks(List<RadixLock> list) {
        this.selectedLocks.clear();
        this.selectedLocks.addAll(list);
    }

}
