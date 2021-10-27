package work.pomelo.admin.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageDto {
    //当前页
    private Integer page;
    //起始索引
    private Integer offset;
    //每页展示条数
    private Integer pageSize;
    //总页数
    private Integer total;
    //总条数
    private Integer count;

    public PageDto(Integer page, Integer pageSize){
        this.page = page;
        this.pageSize = pageSize;
    }
}
