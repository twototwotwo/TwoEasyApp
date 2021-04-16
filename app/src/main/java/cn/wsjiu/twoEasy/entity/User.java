package cn.wsjiu.twoEasy.entity;


import java.io.Serializable;

public class User implements Serializable {
    private Integer userId;
    private Integer cloudBean;
    private String yibanId;
    private String accountName;
    private String password;
    private String payPassword;
    private String headUrl;
    private String userName;
    private String userNickName;
    private String schoolId;
    private String schoolName;
    private String sex;
    private String declaration;

    /**
     * 以下是冗余字段，不属于用户的，用于某些特点场景
     */
    private String newPayPassword;

    public String getNewPayPassword() {
        return newPayPassword;
    }

    public void setNewPayPassword(String newPayPassword) {
        this.newPayPassword = newPayPassword;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getDeclaration() {
        return declaration;
    }

    public Integer getCloudBean() {
        return cloudBean;
    }

    public void setCloudBean(Integer cloudBean) {
        this.cloudBean = cloudBean;
    }

    public void setDeclaration(String declaration) {
        this.declaration = declaration;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getYibanId() {
        return yibanId;
    }

    public void setYibanId(String yibanId) {
        this.yibanId = yibanId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
