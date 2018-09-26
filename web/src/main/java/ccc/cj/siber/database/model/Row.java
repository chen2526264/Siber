package ccc.cj.siber.database.model;

/**
 * @author chenjiong
 * @date 11/02/2018 14:12
 */
public class Row {
    /**
     * 该行的主键的值，多个值是用逗号分开
     */
    private String pks;
    /**
     * 该行的所有字段的值，多个值用逗号分开
     */
    private String data;
    public String getPks() {
        return pks;
    }

    public void setPks(String pks) {
        this.pks = pks;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


}
