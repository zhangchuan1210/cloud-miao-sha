package com.geekq.miaosha.common.biz.entity;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhangc
 * @since 2021-03-28
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Alias("OrderInfo")
@ApiModel(value="商品订单信息对象")
@EqualsAndHashCode(callSuper = false)
public class OrderInfo extends Model<OrderInfo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 收获地址ID
     */
    private Long deliveryAddrId;

    /**
     * 冗余过来的商品名称
     */
    private String goodsName;

    /**
     * 商品数量
     */
    private Integer goodsCount;

    /**
     * 商品单价
     */
    private Double goodsPrice;

    /**
     * 1pc，2android，3ios
     */
    private Integer orderChannel;

    /**
     * 订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退款，5已完成
     */
    private Integer status;

    /**
     * 订单的创建时间
     */
    private Date createDate;

    /**
     * 订单的创建时间
     */
    private Date expireDate;

    /**
     * 支付时间
     */
    private Date payDate;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public void setExpireDate(DateTime offsetMinute) {
    }
}
