package cn.wsjiu.twoEasy.entity;

public enum GoodsState {
    UNSOLD(0, "待售出"),
    TRANSACTION(1, "交易中"),
    SOLD(1 << 1, "已售出"),
    OFFLINE(1 << 2, "已下架");

    public int mask;
    public String state;
    GoodsState(int mask, String state) {
        this.mask = mask;
        this.state = state;
    }

    public static GoodsState valueOf(int state) {
        for (GoodsState goodsState : GoodsState.values()
             ) {
            if((goodsState.mask & state) > 0) {
                return goodsState;
            }
        }
        return UNSOLD;
    }
}
