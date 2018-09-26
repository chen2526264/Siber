package ccc.cj.siber.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author chenjiong
 * @date 13/02/2018 19:45
 */
public class SiberFileUtils {
    private static final Logger logger = LoggerFactory.getLogger(SiberFileUtils.class);

    public static Boolean mkdirIfNotExist(String dirPath) {
        return mkdirIfNotExist(new File(dirPath));
    }

    public static Boolean mkdirIfNotExist(File dir) {
        try {
            if (dir.exists() && dir.isDirectory()) {
                return true;
            } else if (!dir.exists()) {
                FileUtils.forceMkdir(dir);
                return true;
            } else {
                logger.error(String.format("Make Directory failed, %s is not a directory!", dir.getCanonicalPath()));
                return false;
            }
        } catch (IOException e) {
            logger.error("", e);
            return false;
        }
    }
}
