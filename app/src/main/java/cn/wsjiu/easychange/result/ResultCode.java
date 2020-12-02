package cn.wsjiu.easychange.result;

public enum ResultCode {

    /**
     * operation success
     */
    SUCCESS(0, "success"),

    /**
     * operation have server error and msg about detail
     */
    SERRVER_ERROR(1, "server error"),

    /**
     * authorize have error
     */
    AUTHORIZE_ERROR(2, "authorize error"),

    /**
     *
     */
    ACCOUNT_ERROR(3, "account  error"),

    /**
     *
     */
    PASSWORD_ERROR(4, "password error"),

    /**
     *
     */
    MYSQL_ERROR(5, "mysql error"),

    /**
     *
     */
    LOGIN_ERROR(6, "login error"),

    /**
     *
     */
    REGISTER_ERROR(7, "register error"),

    /**
     * 图片上传失败
     */
    IMAGE_UPLOAD__ERROR(8, "图片上传失败");

    private int code;
    private String msg;
    ResultCode(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
