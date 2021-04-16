package cn.wsjiu.twoEasy.entity;

/**
 * 交易类型枚举类
 * @author wsj
 */
public enum TransactionMode{
    /**
     * 线下交易
     */
    TRANSACTION_OFFLINE_MODE(1, "线下交易"),

    /**
     * 快递交易
     */
    TRANSACTION_EXPRESS_MODE(1 << 1, "快递到付"),

    /**
     * 自定义,由双方协商
     */
    TRANSACTION_CUSTOM_MODE(1 << 2, "自定义"),

    /**
     * 自定义,由双方协商
     */
    TRANSACTION_ONLINE_MODE(1 << 3, "线上交易");

    public int mode;
    public String modeStr;

    TransactionMode(int mode, String modeStr) {
        this.mode = mode;
        this.modeStr = modeStr;
    }
}
