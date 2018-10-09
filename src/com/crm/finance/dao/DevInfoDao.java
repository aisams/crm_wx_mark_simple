package com.crm.finance.dao;

/**
 * Created by Dipa on 2018/1/11.
 */

public class DevInfoDao {

    /**
     * deviceid : 2015246248
     */

    private String deviceid;//设备号
    private String wxIMEI;//微信IMEI

    public String getWxIMEI() {
        return wxIMEI;
    }

    public void setWxIMEI(String wxIMEI) {
        this.wxIMEI = wxIMEI;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }
}
