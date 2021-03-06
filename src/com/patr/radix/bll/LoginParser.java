package com.patr.radix.bll;

import org.json.JSONException;
import org.json.JSONObject;

import com.patr.radix.MyApplication;
import com.patr.radix.bean.Community;
import com.patr.radix.bean.LoginResult;
import com.patr.radix.bean.RequestResult;
import com.patr.radix.bean.UserInfo;
import com.patr.radix.bll.ServiceManager.ResponseKey;
import com.patr.radix.network.IAsyncListener;

/**
 * 登录结果解析器
 */
public class LoginParser extends AbsBaseParser<LoginResult> {

    public LoginParser(IAsyncListener<LoginResult> listener) {
        super(listener);
    }

    @Override
    public LoginResult parse(String response) {
        LoginResult result = null;
        try {
            JSONObject json = new JSONObject(response);
            if (json != null) {
                String retcode = json.optString(RequestResult.RET_CODE_KEY);
                String retinfo = json.optString(RequestResult.RET_INFO_KEY);
                result = new LoginResult(retcode, retinfo);
                if (result.isSuccesses()) {
                    JSONObject obj = json.optJSONObject(ResponseKey.MODEL);
                    if (obj != null) {
                        String id = obj.optString(ResponseKey.ID);
                        String account = obj.optString(ResponseKey.USER_ID);
                        String areaId = obj.optString(ResponseKey.AREA_ID);
                        String areaName = obj.optString(ResponseKey.AREA_NAME);
                        String name = obj.optString(ResponseKey.NAME);
                        String mobile = obj.optString(ResponseKey.MOBILE);
                        String home = obj.optString(ResponseKey.HOME);
                        String token = json.optString(ResponseKey.TOKEN);
                        String areaPic = obj.optString(ResponseKey.AREA_PIC);
                        String cardNo = obj.optString(ResponseKey.CARD_NO);
                        String userPic = obj.optString(ResponseKey.USER_PIC);
                        UserInfo userInfo = new UserInfo();
                        userInfo.setId(id);
                        userInfo.setAccount(account);
                        userInfo.setAreaId(areaId);
                        userInfo.setAreaName(areaName);
                        userInfo.setName(name);
                        userInfo.setMobile(mobile);
                        userInfo.setHome(home);
                        userInfo.setToken(token);
                        if (!areaPic.startsWith("http")) {
                            Community community = MyApplication.instance
                                    .getSelectedCommunity();
                            areaPic = String.format("%s:%s%s",
                                    community.getHost(), community.getPort(),
                                    areaPic);
                        }
                        userInfo.setAreaPic(areaPic);
                        userInfo.setCardNo(cardNo);
                        if (!userPic.startsWith("http")) {
                            Community community = MyApplication.instance
                                    .getSelectedCommunity();
                            userPic = String.format("%s:%s%s",
                                    community.getHost(), community.getPort(),
                                    userPic);
                        }
                        userInfo.setUserPic(userPic);
                        result.setUserInfo(userInfo);
                    }
                }
                result.setResponse(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onFailure(e);
        }
        return result;
    }

}
