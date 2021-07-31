package com.geekq.miaosha.common.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhangc
 * @since 2021-07-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MiaoshaFailMessage extends Model<MiaoshaFailMessage> {

    private static final long serialVersionUID = 1L;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务主键id
     */
    private Long businessId;

    private String businessContent;

    private Date createTime;

    /**
     * 消息处理状态，0 失败处理，1 成功处理
     */
    private Integer status;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
