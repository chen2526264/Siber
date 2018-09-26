package ccc.cj.siber.database.model;

import java.util.List;

/**
 * @author chenjiong
 * @date 25/02/2018 21:28
 */
public class Metadata {
    //最大的目录 id
    private Integer maxId;

    private List<String> dirNames;

    public Integer getMaxId() {
        return maxId;
    }

    public void setMaxId(Integer maxId) {
        this.maxId = maxId;
    }

    public List<String> getDirNames() {
        return dirNames;
    }

    public void setDirNames(List<String> dirNames) {
        this.dirNames = dirNames;
    }
}
