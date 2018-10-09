package com.crm.finance.dao;

/**
 * Created by Administrator on 2018/9/1 0001.
 */

public class DeviceStateDao {

    /**
     * deviceid : A100005DC255C0
     * version : 1.3.0
     * statedetails :
     */

    private String deviceid= "";//主微信
    private String wxIMEI= "";//微信IMEI
    private String version="";
    private String statedetails="";

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatedetails() {
        return statedetails;
    }

    public void setStatedetails(String statedetails) {
        this.statedetails = statedetails;
    }
}
