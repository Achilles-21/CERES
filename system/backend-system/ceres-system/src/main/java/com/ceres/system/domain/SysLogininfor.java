package com.ceres.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ceres.common.annotation.Excel;
import com.ceres.common.annotation.Excel.ColumnType;
import com.ceres.common.core.domain.BaseEntity;

import java.util.Date;

public class SysLogininfor extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "序号", cellType = ColumnType.NUMERIC)
    private Long infoId;

    @Excel(name = "用户账号")
    private String username;

    /** Login status: 0 = success, 1 = failure. */
    @Excel(name = "登录状态", readConverterExp = "0=成功,1=失败")
    private String status;

    @Excel(name = "登录地址")
    private String ipaddr;

    @Excel(name = "登录地点")
    private String loginLocation;

    @Excel(name = "浏览器")
    private String browser;

    @Excel(name = "操作系统")
    private String os;

    @Excel(name = "提示消息")
    private String msg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

    public Long getInfoId()
    {
        return infoId;
    }

    public void setInfoId(Long infoId)
    {
        this.infoId = infoId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public String getLoginLocation()
    {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation)
    {
        this.loginLocation = loginLocation;
    }

    public String getBrowser()
    {
        return browser;
    }

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }

    public String getOs()
    {
        return os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public Date getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Date loginTime)
    {
        this.loginTime = loginTime;
    }
}
