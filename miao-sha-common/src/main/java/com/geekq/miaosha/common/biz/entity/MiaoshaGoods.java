package com.geekq.miaosha.common.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhangc
 * @since 2021-03-29
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Alias("MiaoshaGoods")
@EqualsAndHashCode(callSuper = false)
public class MiaoshaGoods extends Model<MiaoshaGoods> {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀的商品表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品Id
     */
    private Long goodsId;

    /**
     * 秒杀价
     */
    private BigDecimal miaoshaPrice;

    /**
     * 库存数量
     */
    private Integer stockCount;

    /**
     * 秒杀开始时间
     */
    private LocalDateTime startDate;

    /**
     * 秒杀结束时间
     */
    private LocalDateTime endDate;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
