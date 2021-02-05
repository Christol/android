package com.eostek.smartbox.wsn.ssdp;

/**
 * Msg的实体类，格式详见toString()
 */
public class WsnSsdpearchMsg {
    private int mMX = 5; /* seconds to delay response */
    private String mST; /* Search target */
 
    public WsnSsdpearchMsg(String ST) {
        mST = ST;
    }
 
    public int getmMX() {
        return mMX;
    }
 
    public void setmMX(int mMX) {
        this.mMX = mMX;
    }
 
    public String getmST() {
        return mST;
    }
 
    public void setmST(String mST) {
        this.mST = mST;
    }
 
    /**
     * @ruturn 发送格式：
     * M-SEARCH * HTTP/1.1
     * Host:239.255.255.250:1900
     * Man:"ssdp:discover"
     * MX:5
     * ST:miivii
     */
    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        content.append(WsnSsdpConstants.SL_MSEARCH).append(WsnSsdpConstants.NEWLINE);
        content.append(WsnSsdpConstants.HOST).append(WsnSsdpConstants.NEWLINE);
        content.append(WsnSsdpConstants.MAN).append(WsnSsdpConstants.NEWLINE);
        content.append("MX:" + mMX).append(WsnSsdpConstants.NEWLINE);
        content.append(mST).append(WsnSsdpConstants.NEWLINE);
        content.append(WsnSsdpConstants.NEWLINE);
        return content.toString();
    }
}
