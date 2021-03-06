package com.geekq.miaosha.common.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhangc
 * @since 2021-03-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Alias("miaoshaorder")
public class MiaoshaOrder extends Model<MiaoshaOrder> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long goodsId;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
