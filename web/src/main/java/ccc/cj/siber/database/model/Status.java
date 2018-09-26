package ccc.cj.siber.database.model;

/**
 * @author chenjiong
 * @date 10/03/2018 09:59
 */
public class Status implements Comparable<Status> {
    private Integer datasourceId;
    private Integer id;
    private String name;

    public Status(Integer datasourceId, Integer id, String name) {
        this.datasourceId = datasourceId;
        this.id = id;
        this.name = name;
    }

    public Integer getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(Integer datasourceId) {
        this.datasourceId = datasourceId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Status o) {
        if (id < o.getId()) {
            return -1;
        } else if (id > o.getId()) {
            return 1;
        } else {
            return 0;
        }
    }
}
