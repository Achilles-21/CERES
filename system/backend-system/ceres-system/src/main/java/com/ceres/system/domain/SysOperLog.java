package com.ceres.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ceres.common.annotation.Excel;
import com.ceres.common.annotation.Excel.ColumnType;
import com.ceres.common.core.domain.BaseEntity;

import java.util.Date;

public class SysOperLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "操作序号", cellType = ColumnType.NUMERIC)
    private Long operId;

    @Excel(name = "操作模块")
    private String title;

    /** Business operation: 0 = other, 1 = create, 2 = update, 3 = delete. */
    @Excel(name = "业务类型", readConverterExp = "0=其它,1=新增,2=修改,3=删除,4=授权,5=导出,6=导入,7=强退,8=生成代码,9=清空数据")
    private Integer businessType;

    private Integer[] businessTypes;

    @Excel(name = "请求方法")
    private String method;

    @Excel(name = "请求方式")
    private String requestMethod;

    /** Operator type: 0 = other, 1 = backend user, 2 = mobile user. */
    @Excel(name = "操作类别", readConverterExp = "0=其它,1=后台用户,2=手机端用户")
    private Integer operatorType;

    @Excel(name = "操作人员")
    private String operName;

    @Excel(name = "部门名称")
    private String deptName;

    @Excel(name = "请求地址")
    private String operUrl;

    @Excel(name = "操作地址")
    private String operIp;

    @Excel(name = "操作地点")
    private String operLocation;

    @Excel(name = "请求参数")
    private String operParam;

    @Excel(name = "返回参数")
    private String jsonResult;

    /** Operation status: 0 = success, 1 = failure. */
    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    private Integer status;

    @Excel(name = "错误消息")
    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operTime;

    @Excel(name = "消耗时间", suffix = "毫秒")
    private Long costTime;

    public Long getOperId()
    {
        return operId;
    }

    public void setOperId(Long operId)
    {
        this.operId = operId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Integer getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(Integer businessType)
    {
        this.businessType = businessType;
    }

    public Integer[] getBusinessTypes()
    {
        return businessTypes;
    }

    public void setBusinessTypes(Integer[] businessTypes)
    {
        this.businessTypes = businessTypes;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public String getRequestMethod()
    {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod)
    {
        this.requestMethod = requestMethod;
    }

    public Integer getOperatorType()
    {
        return operatorType;
    }

    public void setOperatorType(Integer operatorType)
    {
        this.operatorType = operatorType;
    }

    public String getOperName()
    {
        return operName;
    }

    public void setOperName(String operName)
    {
        this.operName = operName;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getOperUrl()
    {
        return operUrl;
    }

    public void setOperUrl(String operUrl)
    {
        this.operUrl = operUrl;
    }

    public String getOperIp()
    {
        return operIp;
    }

    public void setOperIp(String operIp)
    {
        this.operIp = operIp;
    }

    public String getOperLocation()
    {
        return operLocation;
    }

    public void setOperLocation(String operLocation)
    {
        this.operLocation = operLocation;
    }

    public String getOperParam()
    {
        return operParam;
    }

    public void setOperParam(String operParam)
    {
        this.operParam = operParam;
    }

    public String getJsonResult()
    {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult)
    {
        this.jsonResult = jsonResult;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public Date getOperTime()
    {
        return operTime;
    }

    public void setOperTime(Date operTime)
    {
        this.operTime = operTime;
    }

    public Long getCostTime()
    {
        return costTime;
    }

    public void setCostTime(Long costTime)
    {
        this.costTime = costTime;
    }
}
