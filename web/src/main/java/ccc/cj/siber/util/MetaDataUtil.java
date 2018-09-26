package ccc.cj.siber.util;

import ccc.cj.siber.database.model.Metadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author chenjiong
 * @date 07/03/2018 10:16
 */
public class MetaDataUtil {
    private static final String METADATA_FILE_NAME = "metadata.json";
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static File getMetadataFile(String parentDir) {
        return new File(parentDir + File.separator + METADATA_FILE_NAME);
    }

    public static File getMetadataFile(File parentFile) {
        return new File(parentFile, METADATA_FILE_NAME);
    }


    public static Metadata initMetadata(File metadataFile) throws Exception {
        Metadata metadata = new Metadata();
        metadata.setMaxId(1);
        metadata.setDirNames(new ArrayList<>());
        writeMetadataFile(metadataFile, metadata);
        return metadata;
    }

    public static void compareAndFresh(File metadataFile, Metadata metadata, String parentDirStr) throws Exception {
        compareAndFresh(metadataFile, metadata, new File(parentDirStr));
    }

    public static void compareAndFresh(File metadataFile, Metadata metadata, File parentDir) throws Exception {
        List<String> diaNames = new ArrayList<>(getDirs(parentDir).values());
        if (!diaNames.containsAll(metadata.getDirNames())
                || !metadata.getDirNames().containsAll(diaNames)) {
            metadata.setDirNames(diaNames);
            writeMetadataFile(metadataFile, metadata);
        }
    }

    /**
     * 删除最早的一个目录
     */
    public static String deleteFirst(File metadataFile, Metadata metadata, File parentDir) throws Exception {
        TreeMap<Integer, String> dirs = getDirs(parentDir);
        Map.Entry<Integer, String> firstEntry = dirs.firstEntry();
        if (firstEntry != null) {
            metadata.getDirNames().remove(firstEntry.getValue());
            writeMetadataFile(metadataFile, metadata);
            return firstEntry.getKey() + "_" + firstEntry.getValue();
        }
        return null;
    }

    private static TreeMap<Integer, String> getDirs(File parentDir) {
        TreeMap<Integer, String> dirs = new TreeMap<>();
        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String dirName = file.getName();
                    int index = dirName.indexOf("_");
                    if (index <= 0 || dirName.length() - 1 == index) {
                        continue;
                    }
                    String id = dirName.substring(0, index);
                    if (!StringUtils.isNumeric(id)) {
                        continue;
                    }

                    String datasourceName = dirName.substring(index + 1);
                    dirs.put(Integer.valueOf(id), datasourceName);
                }
            }
        }
        return dirs;
    }

    public static Metadata parseMetadataFile(File metadataFile) throws Exception {
        String json = FileUtils.readFileToString(metadataFile, Constant.CHARSET);
        return objectMapper.readValue(json, Metadata.class);
    }

    public static String newDir(Metadata metadata, String dirName) {
        if (metadata.getDirNames().contains(dirName)) {
            return null;
        }
        return metadata.getMaxId() + "_" + dirName;
    }

    public static void saveNewDir(File metadataFile, Metadata metadata, String dirName) throws Exception {
        metadata.setMaxId(metadata.getMaxId() + 1);
        metadata.getDirNames().add(dirName);
        writeMetadataFile(metadataFile, metadata);
    }

    public static void removeDir(File metadataFile, Metadata metadata, String dirName) throws Exception {
        metadata.getDirNames().remove(dirName);
        writeMetadataFile(metadataFile, metadata);
    }

    public static void updateDir(File metadataFile, Metadata metadata, String oldDirName, String newDirName) throws Exception {
        metadata.getDirNames().remove(oldDirName);
        metadata.getDirNames().add(newDirName);
        writeMetadataFile(metadataFile, metadata);
    }

    private static void writeMetadataFile(File metadataFile, Metadata metadata) throws Exception {
        FileUtils.writeStringToFile(metadataFile, objectMapper.writeValueAsString(metadata), Constant.CHARSET, false);
    }
}
