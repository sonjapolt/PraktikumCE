package at.jku.ce.CoMPArE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Created by oppl on 23/11/2016.
 */
public class LogHelper {

    private static Logger logger = LoggerFactory.getLogger("VirtualEnactment");
    {

    }

    private static String groupID = new String("Anonymous");

    public static void setGroupID(String group) {
        groupID = group;
    }

    public static void logThrowable(Throwable throwable) {
        logger.error(/*Instant.now().toString()+" - "+*/groupID+" - exception:\t" + throwable.getMessage(),
                throwable);
    }

    public static void logError(String string) {
        logger.error(/*Instant.now().toString()+" - "+*/groupID+ " - error:\t"
                + string);
    }

    public static void logInfo(String string) {
        logger.info(/*Instant.now().toString()+" - "+*/groupID+ ": "+string);
    }

    public static void logDebug(String string) {
        logger.info(/*Instant.now().toString()+" - "+*/groupID+" - debug: "+string);
    }

}
