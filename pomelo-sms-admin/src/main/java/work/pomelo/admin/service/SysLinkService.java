package work.pomelo.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import work.pomelo.admin.domain.SysLink;
import work.pomelo.admin.domain.dto.PageDto;
import work.pomelo.admin.mapper.SysLinkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
@Service
public class SysLinkService {
    @Autowired
    private SysLinkMapper sysLinkMapper;

    public SysLink get(Integer id) {
        return sysLinkMapper.selectById(id);
    }

    /**
     * 添加
     */
    public void add(SysLink link) {
        sysLinkMapper.insert(link);
    }

    public void update(SysLink link) {
        sysLinkMapper.updateById(link);
    }

    /**
     * 删除
     */
    public void del(int id) {
        sysLinkMapper.deleteById(id);
    }

    /**
     * 查询所有
     */
    public Page<SysLink> getAllLink(PageDto pageDto) {
        int skip = (pageDto.getPage() - 1) * pageDto.getPageSize();
        Page<SysLink> page = new Page<SysLink>(skip, pageDto.getPageSize());
        page = sysLinkMapper.selectPage(page, new QueryWrapper<SysLink>().eq("enable", 1));
        return page;
    }

    public List<SysLink> getAllLink() {
        return sysLinkMapper.selectList(new QueryWrapper<SysLink>().eq("enable", 1));
    }

    public int getAllCount() {
        return sysLinkMapper.selectCount(new QueryWrapper<SysLink>());
    }
}
