/**
 * radix
 * PrefSettingActivity
 * zhoushujie
 * 2016-10-6 下午7:11:26
 */
package com.patr.radix.ui.settings;

import com.patr.radix.LoginActivity;
import com.patr.radix.MyApplication;
import com.patr.radix.R;
import com.patr.radix.bean.UserInfo;
import com.patr.radix.ui.view.TitleBarView;
import com.patr.radix.ui.view.dialog.MsgDialog;
import com.patr.radix.ui.view.dialog.MsgDialog.BtnType;
import com.patr.radix.utils.Constants;
import com.patr.radix.utils.PrefUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * @author zhoushujie
 * 
 */
public class PrefSettingActivity extends Activity implements OnClickListener {

    Context context;

    private TitleBarView titleBarView;

    private LinearLayout pushLl;

    private LinearLayout shakeLl;

    private ImageView pushSwitchIv;

    private ImageView shakeSwitchIv;

    private Button logoutBtn;

    private boolean pushEnabled;

    private boolean shakeEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pref_setting);
        context = this;
        initView();
    }

    private void initView() {
        titleBarView = (TitleBarView) findViewById(R.id.pref_setting_titlebar);
        pushLl = (LinearLayout) findViewById(R.id.push_ll);
        shakeLl = (LinearLayout) findViewById(R.id.shake_ll);
        pushSwitchIv = (ImageView) findViewById(R.id.push_iv);
        shakeSwitchIv = (ImageView) findViewById(R.id.shake_iv);
        logoutBtn = (Button) findViewById(R.id.logout_btn);

        pushLl.setOnClickListener(this);
        shakeLl.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);

        titleBarView.setTitle("偏好设置");
        pushEnabled = PrefUtil.getBoolean(context, Constants.PREF_PUSH_SWITCH,
                true);
        shakeEnabled = PrefUtil.getBoolean(context,
                Constants.PREF_SHAKE_SWITCH, true);
        if (pushEnabled) {
            pushSwitchIv.setImageResource(R.drawable.switch_on);
        } else {
            pushSwitchIv.setImageResource(R.drawable.switch_off);
        }
        if (shakeEnabled) {
            shakeSwitchIv.setImageResource(R.drawable.switch_on);
        } else {
            shakeSwitchIv.setImageResource(R.drawable.switch_off);
        }
        refresh();
    }

    private void login() {
        LoginActivity.start(context);
    }

    private void logout() {
        MsgDialog.show(context, "确认", "确定要退出当前账号吗？", "确定",
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        MyApplication.instance.setUserInfo(new UserInfo());
                        PrefUtil.saveUserInfo(context, new UserInfo());
                        refresh();
                    }
                }, BtnType.TWO);
    }

    private void refresh() {
        if (TextUtils.isEmpty(MyApplication.instance.getUserInfo().getToken())) {
            logoutBtn.setText("登录");
        } else {
            logoutBtn.setText("退出登录");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.push_ll:
            pushEnabled = !pushEnabled;
            PrefUtil.save(context, Constants.PREF_PUSH_SWITCH, pushEnabled);
            if (pushEnabled) {
                pushSwitchIv.setImageResource(R.drawable.switch_on);
            } else {
                pushSwitchIv.setImageResource(R.drawable.switch_off);
            }
            break;

        case R.id.shake_ll:
            shakeEnabled = !shakeEnabled;
            PrefUtil.save(context, Constants.PREF_SHAKE_SWITCH, shakeEnabled);
            if (shakeEnabled) {
                shakeSwitchIv.setImageResource(R.drawable.switch_on);
            } else {
                shakeSwitchIv.setImageResource(R.drawable.switch_off);
            }
            break;

        case R.id.logout_btn:
            if (TextUtils.isEmpty(MyApplication.instance.getUserInfo()
                    .getAccount())) {
                login();
            } else {
                logout();
            }
            break;
        }
    }

}
