package com.mhra.mcm.appian.utils.helpers.others;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by TPD_Auto on 14/07/2016.
 */
public class FileUtils {

    private final static String resourceFolder = "src" + File.separator + "test" + File.separator + "resources" + File.separator;
    private final static String userFileLocation = "configs" + File.separator + "users.properties";
    private final static String driverFileLocation = "configs" + File.separator + "driver.properties";
    private final static Map<String, Properties> mapOfProperties = new HashMap<String, Properties>();

    /**
     * Load properties files from system
     *
     * @param fileName
     * @return
     */
    public static Properties loadPropertiesFile(String fileName) {

        Properties prop = mapOfProperties.get(fileName);

        if (prop == null) {
            try {
                String root = new File("").getAbsolutePath();
                String location = root + File.separator + resourceFolder + userFileLocation;
                prop = new Properties();
                InputStream in = new FileInputStream(new File(location));
                prop.load(in);
                in.close();

                //update map
                mapOfProperties.put(fileName, prop);
            } catch (Exception e) {
                prop = null;
                e.printStackTrace();
            }
        }

        return prop;
    }


    /**
     * Get full path to a specific file related to root of project
     *
     * @param fileName
     * @return
     */
    public static String getDataFileFullPath(String fileName) {
        File file = new File("");
        String rootFolder = file.getAbsolutePath();
        String data = (rootFolder + File.separator + resourceFolder + fileName);
        return data;
    }

    /**
     * @return
     */
    public static String getTempFileFullPath() {
        File file = new File("");
        String rootFolder = file.getAbsolutePath();
        String data = (rootFolder + File.separator + resourceFolder + "tmp");
        return data;
    }


    public static String getFileFullPath(String tmpFolderName, String fileName) {
        File file = new File("");
        String rootFolder = file.getAbsolutePath();
        String data = (rootFolder + File.separator + resourceFolder + tmpFolderName + File.separator + fileName);
        return data;
    }


    /**
     * If no xmlFileName is supplied than it defaults to Text.xml
     *
     * @param xmlFileName
     * @return
     */
    public static String getXMLNotificationDataFileName(String xmlFileName) {
        boolean randomOrEmpty = false;
        if (xmlFileName != null && (xmlFileName.contains(".xml") || xmlFileName.trim().equals("random"))) {
            if (xmlFileName.trim().equals("") || xmlFileName.trim().equals("random")) {
                randomOrEmpty = true;
                if (xmlFileName.equals("random"))
                    xmlFileName = "Test" + new Date().toString().substring(0, 17).replaceAll(" ", "").replace(":", "") + ".xml";
                else
                    xmlFileName = "Test.xml";
            }
        } else {
            randomOrEmpty = true;
            xmlFileName = "Test.xml";
        }

        if(!randomOrEmpty){
            if(xmlFileName.contains(".")) {
                String data[] = xmlFileName.split("\\.");
                xmlFileName = data[0] + "_" + RandomDataUtils.getTimeMinHour(true) + "." + data[1];
            }
        }
        return xmlFileName;
    }


    public static Properties driverProp;

    public static String getASpecificDriverProperty(String propertyName) {
        String value = null;
        if (driverProp == null) {
            try {
                String root = new File("").getAbsolutePath();
                String location = root + File.separator + resourceFolder + driverFileLocation;
                driverProp = new Properties();
                InputStream in = new FileInputStream(new File(location));
                driverProp.load(in);
                in.close();

            } catch (Exception e) {
                driverProp = null;
                e.printStackTrace();
            }
        }else{
            value = driverProp.getProperty(propertyName);
        }
        return value;
    }

    public static void deleteFile(String zipFile) {
        File f = new File(zipFile);
        f.delete();
    }
}
