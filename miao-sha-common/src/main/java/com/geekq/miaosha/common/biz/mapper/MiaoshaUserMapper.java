package com.geekq.miaosha.common.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhangc
 * @since 2021-03-29
 */
public interface MiaoshaUserMapper extends BaseMapper<MiaoshaUser> {
    public MiaoshaUser getByNickname(@Param("nickname") String nickname) ;

    public MiaoshaUser getById(@Param("id") long id) ;

    public void update(MiaoshaUser toBeUpdate);


    public int getCountByUserName(@Param("userName") String userName, @Param("userType") int userType);

}
