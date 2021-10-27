package work.pomelo.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import work.pomelo.admin.domain.SysUser;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
    List<SysUser> getAll();
}
