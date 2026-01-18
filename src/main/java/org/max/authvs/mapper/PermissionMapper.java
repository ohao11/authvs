package org.max.authvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.max.authvs.entity.Permission;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
