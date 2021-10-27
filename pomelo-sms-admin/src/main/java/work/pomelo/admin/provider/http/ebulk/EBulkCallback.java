package work.pomelo.admin.provider.http.ebulk;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import work.pomelo.admin.common.ApplicationContextUtil;
import work.pomelo.admin.common.http.HttpClient4;
import work.pomelo.admin.domain.SmsCollect;
import work.pomelo.admin.domain.SmsDetails;
import work.pomelo.admin.enums.SmsEnum;
import work.pomelo.admin.mapper.SmsCollectMapper;
import work.pomelo.admin.mapper.SmsDetailsMapper;

import java.util.Date;

/**
 * @author ghostxbh
 * @date 2020/10/13
 */
@Slf4j
public class EBulkCallback {
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);

    public void reqMsgId(SmsDetails details) {
        String url = "https://e-bulksms.com/sendsms/dlr?smscid=" + details.getRespMessageId();
        String resStatus = HttpClient4.doGet(url);
        String status = getStatus(resStatus);
        excute(status, details);
    }

    private String getStatus(String resStatus) {
        if (resStatus.contains(":")) {
            return resStatus.split(":")[1].trim();
        }
        return null;
    }

    public void excute(String status, SmsDetails details) {
        log.info("回调成功，msgId: " + details.getRespMessageId() + " , status: " + status);

        SmsDetails set = new SmsDetails();
        if ("SUBMITTED".equalsIgnoreCase(status) || "An error has occurred".contains(status)) {
            set.setStatus(3);
            set.setReportStat(status);
            smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", details.getDetailsId()));
            return;
        } else if ("DELIVRD".equalsIgnoreCase(status)) {
            set.setStatus(10);
        } else {
            set.setStatus(-1);
        }

        set.setReceiveTime(new Date());
        set.setReportStat(status);
        smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", details.getDetailsId()));

        SmsCollect collect = smsCollectMapper.selectOne(new QueryWrapper<SmsCollect>().eq("collect_id", details.getCollectId()));
        SmsCollect collectSet = new SmsCollect();
        if (collect.getPendingNum() > 0) {
            collectSet.setPendingNum(collect.getPendingNum() - 1);
            if (collect.getPendingNum() == 1) {
                collectSet.setStatus(SmsEnum.SUCCESS.getStatus());
            }
            if (set.getStatus() == 3) {
                return;
            } else if (set.getStatus() == 10) {
                collectSet.setSuccessNum(collect.getSuccessNum() + 1);
            } else if (set.getStatus() == -1) {
                collectSet.setFailNum(collect.getFailNum() + 1);
            }
            smsCollectMapper.update(collectSet, new QueryWrapper<SmsCollect>().eq("collect_id", details.getCollectId()));
        }
    }
}
