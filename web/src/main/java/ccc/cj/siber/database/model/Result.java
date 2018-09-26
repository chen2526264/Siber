package ccc.cj.siber.database.model;

import ccc.cj.siber.util.Constant;

/**
 * @author chenjiong
 * @date 11/02/2018 13:55
 */
public class Result<T> {
    private Integer code;
    private T result;
    private String message;
    private Object extra;

    public Result() {
    }

    public Result(T result) {
        this.code = Constant.SUCCESS_CODE;
        this.result = result;
    }

    public Result(Integer code, String message, T result) {
        this.code = code;
        this.result = result;
        this.message = message;
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static <T> Result<T> success() {
        Result result = new Result();
        result.code = Constant.SUCCESS_CODE;
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result result = new Result();
        result.code = Constant.FAILED_CODE;
        result.setMessage(message);
        result.setResult(null);
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}
