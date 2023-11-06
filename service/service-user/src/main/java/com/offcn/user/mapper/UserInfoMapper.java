package com.offcn.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.model.user.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
