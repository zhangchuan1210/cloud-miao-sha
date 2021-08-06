package com.geekq.miaosha.order.vo;

import java.io.Serializable;

public class StockOverMessageVo implements Serializable {
    private long goodId;
    private boolean over;

    public long getGoodId() {
        return goodId;
    }

    public void setGoodId(long goodId) {
        this.goodId = goodId;
    }

    public boolean isOver() {
        return over;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    private String source;//消息来源


}
