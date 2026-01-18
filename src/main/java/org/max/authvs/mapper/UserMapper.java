package org.max.authvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.max.authvs.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
