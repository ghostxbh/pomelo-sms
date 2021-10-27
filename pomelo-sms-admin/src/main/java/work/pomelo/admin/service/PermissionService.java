package work.pomelo.admin.service;

import work.pomelo.admin.domain.Permission;
import work.pomelo.admin.domain.SysUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionService {
    private static final String SMS = "短信";
    private static final String SMS_ICO = "fa fa-envelope-o";
    private static final String SMS_LIST = "短信列表";
    private static final String SMS_LIST_URL = "/sms/list";
    private static final String SMS_ADD = "短信发送";
    private static final String SMS_ADD_URL = "/sms/add";

    private static final String USER = "用户";
    private static final String USER_ICO = "fa fa-users";
    private static final String USER_INFO = "用户资料";
    private static final String USER_INFO_URL = "/user/info";
    private static final String USER_PWD = "重置密码";
    private static final String USER_PWD_URL = "/user/pwd";

    private static final String STATIS = "统计";
    private static final String STATIS_ICO = "fa fa-line-chart";
    private static final String STATIS_LIST = "统计列表";
    private static final String STATIS_LIST_URL = "/statis/list";

    private static final String MANAGER = "管理";
    private static final String MANAGER_ICO = "fa fa-user-circle-o";
    private static final String CREATE_USER = "添加用户";
    private static final String CREATE_USER_URL = "/manager/add";
    private static final String MANAGER_USER = "管理用户";
    private static final String MANAGER_USER_URL = "/manager/users";

    private static final String CHANNEL = "通道";
    private static final String CHANNEL_ICO = "fa fa-ils";
    private static final String CHANNEL_ADD = "添加通道";
    private static final String CHANNEL_ADD_URL = "/channel/addPage";
    private static final String CHANNEL_LIST = "通道管理";
    private static final String CHANNEL_LIST_URL = "/channel/list";

    private static final String LINK = "地址";
    private static final String LINK_ICO = "fa fa-chain";
    private static final String LINK_ADD = "添加地址";
    private static final String LINK_ADD_URL = "/link/addPage";
    private static final String LINK_LIST = "地址管理";
    private static final String LINK_LIST_URL = "/link/list";

    public List<Permission> permissions(SysUser vilde) {
        List<Permission> list = new ArrayList<Permission>(4);

        List<Permission> smsList = new ArrayList<Permission>(2);
        Permission smsadd = new Permission(SMS_ADD, SMS_ADD_URL);
        Permission smslist = new Permission(SMS_LIST, SMS_LIST_URL);
        smsList.add(smsadd);
        smsList.add(smslist);
        Permission sms = new Permission(SMS, SMS_ICO, smsList);

        List<Permission> userList = new ArrayList<Permission>(3);
        Permission userinfo = new Permission(USER_INFO, USER_INFO_URL);
        Permission userpwd = new Permission(USER_PWD, USER_PWD_URL);
        userList.add(userinfo);
        userList.add(userpwd);
        Permission user = new Permission(USER, USER_ICO, userList);

        List<Permission> statisList = new ArrayList<Permission>(1);
        Permission statislist = new Permission(STATIS_LIST, STATIS_LIST_URL);
        statisList.add(statislist);
        Permission cllection = new Permission(STATIS, STATIS_ICO, statisList);

        list.add(sms);
        list.add(cllection);
        list.add(user);

        if (vilde.getRole() != null && vilde.getRole().equals("admin")) {
            List<Permission> managerList = new ArrayList<Permission>(2);
            Permission adduser = new Permission(CREATE_USER, CREATE_USER_URL);
            Permission manageruser = new Permission(MANAGER_USER, MANAGER_USER_URL);
            managerList.add(adduser);
            managerList.add(manageruser);
            Permission manager = new Permission(MANAGER, MANAGER_ICO, managerList);
            list.add(manager);

            List<Permission> channelList = new ArrayList<Permission>(2);
            Permission addChannel = new Permission(CHANNEL_ADD, CHANNEL_ADD_URL);
            Permission channels = new Permission(CHANNEL_LIST, CHANNEL_LIST_URL);
            channelList.add(addChannel);
            channelList.add(channels);

            Permission channel = new Permission(CHANNEL, CHANNEL_ICO, channelList);
            list.add(channel);

            List<Permission> LinkList = new ArrayList<Permission>(2);
            Permission addLink = new Permission(LINK_ADD, LINK_ADD_URL);
            Permission links = new Permission(LINK_LIST, LINK_LIST_URL);
            LinkList.add(addLink);
            LinkList.add(links);

            Permission link = new Permission(LINK, LINK_ICO, LinkList);
            list.add(link);
        }
        return list;
    }
}
