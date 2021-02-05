package com.eostek.smartbox.eloud;

import java.util.ArrayList;

public interface LimitTimeUserInfoListener {
    /**
     * 从服务器获取用户列表结果
     * @param result
     * @param data
     */
    void onResult(int result, LimitTimeUserInfo data);
}
