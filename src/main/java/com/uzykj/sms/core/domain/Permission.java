package com.uzykj.sms.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    private String pms_name;
    private String pms_ico;
    private String pms_url;
    private List<Permission> sub_permissions;

    public Permission(String pms_name, String pms_url) {
        this.pms_name = pms_name;
        this.pms_url = pms_url;
    }

    public Permission(String pms_name, String pms_ico, List<Permission> sub_permissions) {
        this.pms_name = pms_name;
        this.pms_ico = pms_ico;
        this.sub_permissions = sub_permissions;
    }
}
