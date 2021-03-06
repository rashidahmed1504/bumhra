package com.mhra.mcm.appian.utils.helpers.page;

import com.mhra.mcm.appian.domain.excelpojo.*;
import com.mhra.mcm.appian.domain.webPagePojo.Notification;
import com.mhra.mcm.appian.domain.xmlPojo.EcigProductSubmission;
import com.mhra.mcm.appian.domain.xmlPojo.sub.Product;
import com.mhra.mcm.appian.domain.xmlPojo.sub.SubmissionType;
import com.mhra.mcm.appian.domain.xmlPojo.sub.Submitter;
import com.mhra.mcm.appian.session.ScenarioSession;
import com.mhra.mcm.appian.session.SessionKey;
import com.mhra.mcm.appian.utils.helpers.datadriven.ExcelUtils;
import com.mhra.mcm.appian.utils.helpers.others.FileUtils;
import com.mhra.mcm.appian.utils.helpers.others.RandomDataUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by TPD_Auto on 02/08/2016.
 */
public class NotificationUtils {

    public static final String TMP_ZIP_FILES_LOCATION = "tmp" + File.separator + "xml" + File.separator + "zips";

    /**
     * Change notification data with the values passed into the system
     *
     * @param dataValues
     * @return
     */
    public static Notification updateDefaultNotification(Map<String, String> dataValues) {
        Notification notification = new Notification(2, 2, null);

        if (dataValues != null) {
            String type = dataValues.get("type");
            String tcaNumber = dataValues.get("tcaNumber");
            String submitterName = dataValues.get("submitterName");
            String ingredient = dataValues.get("ingredient");
            String eLiquidVolume = dataValues.get("eLiquidVolume");
            String batteryWattageLiquidVolume = dataValues.get("batteryWattageLiquidVolume");
            String nicotineConcetration = dataValues.get("nicotineConcetration");
            String productType = dataValues.get("productType");
            String brandName = dataValues.get("brandName");

            if (type != null  && !type.trim().equals("")) {
                notification.getSummary().submissionType = type;
            }
            if (tcaNumber != null) {
                notification.getSubmitter().tcaNumber = tcaNumber;
            }
            if (submitterName != null && !submitterName.trim().equals("")) {
                if (submitterName.equals("random")) {
                    String sn = notification.getSubmitter().name;
                    sn = sn + RandomDataUtils.getRandomNumberBetween(1000, 10000);
                    notification.getSubmitter().name = sn;
                }
            }
            if (ingredient != null && !ingredient.trim().equals("") && !ingredient.trim().equals("stored")) {
                notification.getIngredient().ingredientName = ingredient;
                System.out.println("Ingredient : " + ingredient);
            }

            if(eLiquidVolume!=null && !eLiquidVolume.equals("")){
                notification.getProductDesign().volumeELiquid = Double.parseDouble(eLiquidVolume);
            }

            if(batteryWattageLiquidVolume!=null && !batteryWattageLiquidVolume.equals("")){
                notification.getBatteryWattageVoltage().liquidVolumeCapacity = batteryWattageLiquidVolume;
            }

            if(nicotineConcetration!=null && !nicotineConcetration.equals("")){
                notification.getBatteryWattageVoltage().nicotineConcentration = nicotineConcetration;
            }

            if(productType!=null && !productType.equals("")){
                notification.getProduct().type = productType;
            }

            if(brandName!=null && !brandName.equals("")){
                notification.getProduct().brandName = brandName;
            }



        }


        return notification;
    }

    /**
     * Upload notification data using the File Upload Dialogue
     * @param docNumber
     * @param driver
     * @param documentType
     * @param fileName
     * @param description
     * @param confidential
     * @param active
     * @param name
     */
    public static void addDocumentNumber(int docNumber, WebDriver driver, String documentType, String fileName, String description, boolean confidential, boolean active, String name) {
        int countFromSelect = (docNumber - 1) * 2;  //Theres only 2 select box
        int countFromInput = (docNumber - 1) * 8;   //Position of last input element is 8

        //Wait for elements to be clickable
        WaitUtils.waitForElementToBeClickable(driver, By.xpath(".//h2[.='Document Type']//following::select[" + (countFromSelect + 1) + "]"), 10);
        WaitUtils.waitForElementToBeVisible(driver, By.xpath(".//h2[.='Document Type']//following::select[" + (countFromSelect + 2) + "]"), 10);

        //Get elements
        WebElement documentTypeSelectBox = driver.findElement(By.xpath(".//h2[.='Document Type']//following::select[" + (countFromSelect + 1) + "]"));
        WebElement ingredientSelectBox = driver.findElement(By.xpath(".//h2[.='Document Type']//following::select[" + (countFromSelect + 2) + "]"));
        WebElement descriptionElement = driver.findElement(By.xpath(".//h2[.='Document Type']//following::input[" + (countFromInput + 4) + "]"));
        WebElement confidentialYes = driver.findElement(By.xpath(".//h2[.='Document Type']//following::input[" + (countFromInput + 5) + "]"));
        WebElement confidentialNo = driver.findElement(By.xpath(".//h2[.='Document Type']//following::input[" + (countFromInput + 6) + "]"));
        WebElement activeYes = driver.findElement(By.xpath(".//h2[.='Document Type']//following::input[" + (countFromInput + 7) + "]"));
        WebElement activeNo = driver.findElement(By.xpath(".//h2[.='Document Type']//following::input[" + (countFromInput + 8) + "]"));

        //Fill details
        try {
            Integer.valueOf(documentType);
            PageUtils.selectByIndex(documentTypeSelectBox, documentType);
        }catch(Exception e){
            PageUtils.selectByText(documentTypeSelectBox, documentType);
        }
        PageUtils.typeText(descriptionElement, description);
        PageUtils.clickOptionAdvanced(driver, confidentialYes, confidentialNo, confidential);
        PageUtils.clickOptionAdvanced(driver, activeYes, activeNo, active);

        WaitUtils.waitForElementToBeClickable(driver, ingredientSelectBox, 5);
        PageUtils.selectByText(ingredientSelectBox, name);

        //Browse and load document
        System.out.println(fileName);
        WebElement browseElement = driver.findElement(By.xpath(".//h2[.='Document Type']//following::input[" + (countFromInput + 3) + "]"));
        PageUtils.uploadDocument(browseElement, fileName);

        //Submit the form
        WebElement submit = driver.findElement(By.xpath(".//button[.='Submit']"));
        PageUtils.doubleClick(driver, submit);

        WaitUtils.nativeWait(1);
    }


    /**
     * Upload a zip file
     * @param driver
     * @param zipFileToUpload
     */
    public static void uploadZipFile(WebDriver driver, String zipFileToUpload) {
        System.out.println(zipFileToUpload);
        WaitUtils.nativeWait(2);
        WaitUtils.waitForElementToBeClickable(driver, By.partialLinkText("Add Document"), 10);
        WebElement browseElement = driver.findElement(By.xpath(".//*[.='Upload Zip Files']//following::input[@type='file']"));
        PageUtils.uploadDocument(browseElement, zipFileToUpload);

        //Submit the form
        WebElement submit = driver.findElement(By.xpath(".//button[.='Submit']"));
        PageUtils.doubleClick(driver, submit);

        WaitUtils.nativeWait(1);
    }
    /**
     * Create XML data for notification IMPORT function
     * @param dataValues
     * @param mapOfExcelData
     * @return
     */
    public static EcigProductSubmission generateDefaultXMLNotificationData(Map<String, String> dataValues, Map<String, Map> mapOfExcelData) {

        EcigProductSubmission notification = new EcigProductSubmission();

        //for (int i = 1; i <= 3; i++) {
        if (dataValues != null) {
            String submitterValue = dataValues.get("submitter");
            String productValue = dataValues.get("product");
            String emissionValue = dataValues.get("listOfEmissions");
            String manufacturerValue = dataValues.get("listOfManufacturers");
            String presentationValue = dataValues.get("listOfPresentations");
            String designValue = dataValues.get("design");

            //Set submission type
            String submissionType = dataValues.get("submissionType");
            SubmissionType st = notification.getSubmissionType();
            if (submissionType != null) {
                st.type = submissionType;
            }

            //Submitter
            Submitter submitter = notification.getSubmitter();
            if (submitterValue != null && !submitterValue.equals("none")) {
                DO_Submitter doSubmitter = (DO_Submitter) mapOfExcelData.get("Submitter").get(submitterValue);
                submitter.addSubmitter(dataValues, doSubmitter);
            }
            submitter.evaluate();

            //Product: Emissions,Ingredient,Presentations,Design,Manufacturer,Toxicology
            Product product = notification.getProduct();
            DO_Product doProduct = null;
            String casNumber = product.getCasNumber();
            if (productValue != null && !productValue.equals("none")) {
                doProduct = (DO_Product) mapOfExcelData.get("Product").get(productValue);
                if (doProduct != null)
                    product.addProductDetail(notification.getEcIDNumber(), dataValues, doProduct);
            }

            //Add only if there is a product
            if (doProduct != null) {

                String ingredientValues = dataValues.get("ingredientAndToxicologyReportPairs");
                String[] dataPair = ingredientValues.split(":");
                for (String pair : dataPair) {
                    if (pair != null && !pair.equals("none")) {

                        String[] data = pair.split(",");
                        DO_Ingredient doIngredient = null;
                        boolean toxReportAvailable = true;
                        for (String key : data) {
                            if (key.contains("ingredient")) {
                                doIngredient = (DO_Ingredient) mapOfExcelData.get("Ingredient").get(key.trim());
                            }
                            if (key.contains("toxicology") && doIngredient != null) {
                                DO_ToxicologyDetails doToxicologyDetails = (DO_ToxicologyDetails) mapOfExcelData.get("ToxicologicalDetails").get(key.trim());
                                product.addIngredients(pair, casNumber, dataValues, doIngredient, doToxicologyDetails);
                                toxReportAvailable = true;
                            }else{
                                toxReportAvailable = false;
                            }
                        }
                        if(!toxReportAvailable){
                            product.addIngredients(pair, casNumber, dataValues, doIngredient, null);
                        }
                    }
                }

                if (emissionValue != null && !emissionValue.equals("none")) {

                    String[] data = emissionValue.split(",");
                    for (String key : data) {
                        if (key.contains("emission")) {
                            DO_Emission doEmission = (DO_Emission) mapOfExcelData.get("Emission").get(key.trim());
                            if (doEmission != null) {
                                casNumber = product.addEmission(key, casNumber, dataValues, doEmission);
                            }
                        }

                    }
                }

                if (manufacturerValue != null && !manufacturerValue.equals("none")) {
                    String[] data = manufacturerValue.split(",");
                    for (String key : data) {
                        if (key.contains("manufacturer")) {
                            DO_Manufacturer doManufacturer = (DO_Manufacturer) mapOfExcelData.get("Manufacturer").get(key.trim());
                            if (doManufacturer != null) {
                                product.addManufacturer(mapOfExcelData, doManufacturer);
                            }
                        }
                    }
                }

                if (presentationValue != null && !presentationValue.equals("none")) {
                    String[] data = presentationValue.split(",");
                    for (String key : data) {
                        if (key.contains("presentation")) {
                            DO_Presentation doPresentation = (DO_Presentation) mapOfExcelData.get("Presentation").get(key.trim());
                            if (doPresentation != null) {
                                product.addPresentation(key, dataValues, doPresentation);
                            }
                        }
                    }
                }

                if (designValue != null && !designValue.equals("none")) {
                    String[] data = designValue.split(",");
                    for (String key : data) {
                        if (key.contains("design")) {
                            DO_Design doDesign = (DO_Design) mapOfExcelData.get("Design").get(key.trim());
                            if (doDesign != null) {
                                product.addDesign(key, dataValues, doDesign);
                            }
                        }
                    }
                }

            }
        }
        //}

        return notification;
    }


    /**
     * Generate XML notification data for the IMPORT function
     * @param dataValues
     * @param mapOfExcelData
     * @return
     */
    public static EcigProductSubmission generateDefaultXMLNotificationDataSimple(Map<String, String> dataValues, Map<String, Map> mapOfExcelData) {

        EcigProductSubmission notification = new EcigProductSubmission();

        for (int i = 1; i <= 3; i++) {

            if (dataValues != null) {
                String submitter1 = dataValues.get("submitter" + i);
                String product1 = dataValues.get("product" + i);
                //String ingredient1 = dataValues.get("ingredient" + i);
                //String toxicology1 = dataValues.get("toxicologySet" + i);
                String ingredientAndToxicologyReports1 = dataValues.get("ingredientAndToxicologyReports" + i);
                String manufacturer1 = dataValues.get("manufacturer" + i);
                String presentation1 = dataValues.get("presentation" + i);
                String design1 = dataValues.get("design");
                String emission1 = dataValues.get("emission" + i);

                //Set submission type
                String submissionType = dataValues.get("submissionType");
                SubmissionType st = notification.getSubmissionType();
                if (submissionType != null) {
                    st.type = submissionType;
                }

                //Submitter
                Submitter submitter = notification.getSubmitter();
                if (submitter1 != null && !submitter1.equals("none")) {
                    DO_Submitter doSubmitter = (DO_Submitter) mapOfExcelData.get("Submitter").get(submitter1);
                    submitter.addSubmitter(dataValues, doSubmitter);
                }
                submitter.evaluate();

                //Product: Emissions,Ingredient,Presentations,Design
                Product product = notification.getProduct();
                String casNumber = product.getCasNumber();
                DO_Product doProduct = null;
                if (product1 != null && !product1.equals("none")) {
                    doProduct = (DO_Product) mapOfExcelData.get("Product").get(product1);
                    product.addProductDetail(notification.getEcIDNumber(), dataValues, doProduct);
                }


//                if (ingredient1 != null && !ingredient1.equals("none")) {
//                    DO_Ingredient doIngredient = (DO_Ingredient) mapOfExcelData.get("Ingredient").get(ingredient1);
//                    if (doIngredient != null) {
//                        DO_ToxicologyDetails doToxicologyDetails = (DO_ToxicologyDetails) mapOfExcelData.get("ToxicologicalDetails").get(toxicology1);
//                        product.addIngredients(ingredient1, casNumber, dataValues, doIngredient, doToxicologyDetails);
//                    }
//                }

                if (ingredientAndToxicologyReports1 != null && !ingredientAndToxicologyReports1.equals("none")) {
                    String[] data = ingredientAndToxicologyReports1.split(",");
                    DO_Ingredient doIngredient = null;
                    boolean toxReportAvailable = true;
                    for (String key : data) {
                        if (key.contains("ingredient")) {
                            doIngredient = (DO_Ingredient) mapOfExcelData.get("Ingredient").get(key.trim());
                        }
                        if (key.contains("toxicology") && doIngredient != null) {
                            DO_ToxicologyDetails doToxicologyDetails = (DO_ToxicologyDetails) mapOfExcelData.get("ToxicologicalDetails").get(key.trim());
                            product.addIngredients(key, casNumber, dataValues, doIngredient, doToxicologyDetails);
                            toxReportAvailable = true;
                        }else{
                            toxReportAvailable = false;
                        }
                    }

                    if(!toxReportAvailable){
                        product.addIngredients("", casNumber, dataValues, doIngredient, null);
                    }
                }

                if (emission1 != null && !emission1.equals("none")) {
                    DO_Emission doEmission = (DO_Emission) mapOfExcelData.get("Emission").get(emission1);
                    casNumber = product.addEmission(emission1, casNumber, dataValues, doEmission);
                }

                if (manufacturer1 != null && !manufacturer1.equals("none")) {
                    DO_Manufacturer doManufacturer = (DO_Manufacturer) mapOfExcelData.get("Manufacturer").get(manufacturer1);
                    if (doManufacturer != null) {
                        product.addManufacturer(mapOfExcelData, doManufacturer);
                    }
                }

                if (presentation1 != null && !presentation1.equals("none")) {
                    DO_Presentation doPresentation = (DO_Presentation) mapOfExcelData.get("Presentation").get(presentation1);
                    product.addPresentation(presentation1, dataValues, doPresentation);
                }

                if (design1 != null && !design1.equals("none")) {
                    DO_Design doDesign = (DO_Design) mapOfExcelData.get("Design").get(design1);
                    product.addDesign(design1, dataValues, doDesign);
                }

            }
        }


        return notification;
    }


    public static EcigProductSubmission generateXMLFromExcelData(){

        ExcelUtils excelUtils = new ExcelUtils();
        //mapOfExcelData = excelUtils.getAllData("configs/data/xmlTestData1.xlsx");
        Map<String, Map> mapOfExcelData = excelUtils.getAllDataAsMap("configs/data/xmlTestData2.xlsx");
        System.out.println("TEST DATA LOADED FROM : configs/data/xmlTestData2.xlsx");

        EcigProductSubmission notification = new EcigProductSubmission();
        Map<String, String> dataValues = null;
        for(int i = 1; i <= 1; i++){
            if(true){
                //SET UP DATA FOR NOTIFICATIONS

                String submitter1 = "valid.submitter." + i;
                String product1 = "valid.product." + i;
                String ingredient1 = "valid.ingredient." + i;
                String toxicology1 = "valid.toxicology." + i;
                String emission1 = "valid.emission." + i;
                String manufacturer1 = "valid.manufacturer." + i;
                String presentation1 = "valid.presentation." + i;
                String design1 = "valid.design." + i;

                //Set submission type
                String submissionType = "1";
                SubmissionType st = notification.getSubmissionType();
                if(submissionType!=null){
                    st.type = submissionType;
                }

                //Submitter
                Submitter submitter = notification.getSubmitter();
                if(submitter1!=null && !submitter1.equals("none")){
                    DO_Submitter doSubmitter = (DO_Submitter) mapOfExcelData.get("Submitter").get(submitter1);
                    submitter.addSubmitter(dataValues, doSubmitter );
                }
                submitter.evaluate();

                //Product: Emissions,Ingredient,Presentations,Design
                Product product = notification.getProduct();
                String casNumber = product.getCasNumber();
                if(product1!=null && !product1.equals("none")) {
                    DO_Product doProduct = (DO_Product) mapOfExcelData.get("Product").get(product1);
                    if(doProduct!=null)
                        product.addProductDetail(notification.getEcIDNumber(), dataValues, doProduct);
                }

                if(manufacturer1!=null && !manufacturer1.equals("none")){
                    DO_Manufacturer doManufacturer = (DO_Manufacturer) mapOfExcelData.get("Manufacturer").get(manufacturer1);
                    if (doManufacturer != null) {
                        product.addManufacturer(mapOfExcelData, doManufacturer);
                    }
                }

                if(presentation1!=null && !presentation1.equals("none")){
                    DO_Presentation doPresentation = (DO_Presentation) mapOfExcelData.get("Presentation").get(presentation1);
                    if(doPresentation!=null)
                        product.addPresentation(presentation1, dataValues, doPresentation);
                }
                if(emission1!=null && !emission1.equals("none")){
                    DO_Emission doEmission = (DO_Emission) mapOfExcelData.get("Emission").get(emission1);
                    if(doEmission!=null)
                        casNumber = product.addEmission(emission1, casNumber, dataValues, doEmission);
                }
                if(ingredient1!=null && !ingredient1.equals("none")){
                    DO_Ingredient doIngredient = (DO_Ingredient) mapOfExcelData.get("Ingredient").get(ingredient1);
                    DO_ToxicologyDetails doToxicologyDetails = (DO_ToxicologyDetails) mapOfExcelData.get("ToxicologicalDetails").get(toxicology1);
                    if(doToxicologyDetails!=null && doIngredient!=null)
                        product.addIngredients(ingredient1, casNumber, dataValues, doIngredient, doToxicologyDetails);
                }
                if(design1!=null && !design1.equals("none")){
                    DO_Design doDesign = (DO_Design) mapOfExcelData.get("Design").get(design1);
                    if(doDesign!=null)
                        product.addDesign(design1, dataValues, doDesign);
                }
            }
        }

        return notification;
    }


    /**
     * Creates an XML file which will be uploaded
     * @param notification
     * @return
     */
    public static String createXmlNotificationData(EcigProductSubmission notification, String xmlFileName) {
        String fileName = null;
        try {
            JAXBContext context = JAXBContext.newInstance(EcigProductSubmission.class);

            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            m.marshal(notification, System.out);

            String tmp = FileUtils.getFileFullPath("tmp"+File.separator+"xml"+File.separator+"data", xmlFileName);
            System.out.println(tmp);
            m.marshal(notification, new File(tmp));

            fileName = tmp;
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return fileName;
    }

    public static String createXmlNotificationData(EcigProductSubmission notification) {
        String fileName = null;
        try {
            JAXBContext context = JAXBContext.newInstance(EcigProductSubmission.class);

            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            m.marshal(notification, System.out);

            String sn = new Date().toString().substring(0, 18).replaceAll(" ", "").replace(":", "");
            sn = "";
            String tmp = FileUtils.getFileFullPath("tmp"+File.separator+"xml"+File.separator+"data", "test" + sn + ".xml");
            System.out.println(tmp);
            m.marshal(notification, new File(tmp));

            fileName = tmp;
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return fileName;
    }


    /**
     * Use this to update data with previous ecid
     * @param scenarioSession
     * @param random
     * @return
     */
    public static Notification updateOtherDetails(ScenarioSession scenarioSession, Notification random) {
        String prevEcid = (String) scenarioSession.getData(SessionKey.previousECID);
        String storedIngredient = (String) scenarioSession.getData(SessionKey.substance);
        if(prevEcid==null || !prevEcid.equals("")){
            random.getSummary().previousEcId = prevEcid;
        }

        if(storedIngredient!=null && !storedIngredient.equals("")){
            random.getIngredient().ingredientName = storedIngredient;
        }
        return random;
    }

    public static String createZipFile(String xmlDataFileLocation, String xmlFileName) {
        //Zipfile name
        String zipFileLocation = FileUtils.getFileFullPath(TMP_ZIP_FILES_LOCATION, RandomDataUtils.getRandomTestNameAdvanced("NewNotifications")+".zip");
        try {

            // input file to zip
            File file = new File(xmlDataFileLocation);
            FileInputStream in = new FileInputStream(file);
            // out put file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileLocation));

            // name the file inside the zip  file
            out.putNextEntry(new ZipEntry(file.getName()));

            // buffer size
            byte[] b = new byte[1024];
            int count;

            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
            out.close();
            in.close();
        }catch(Exception e){
            e.printStackTrace();
            zipFileLocation = null;
        }
        return zipFileLocation;
    }


    public static String createZipFile(List<String> files) {
        //Zipfile name
        String zipFileLocation = FileUtils.getFileFullPath(TMP_ZIP_FILES_LOCATION, RandomDataUtils.getRandomTestNameAdvanced("NewNotifications")+".zip");
        try {
            FileOutputStream fos = new FileOutputStream(zipFileLocation);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for(String fileName: files){
                addToZipFile(fileName, zos);
            }
            zos.close();
            fos.close();



        } catch (Exception e) {
            e.printStackTrace();
            zipFileLocation = null;
        }
        return zipFileLocation;
    }



    public static void addToZipFile(String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {

        System.out.println("Writing '" + fileName + "' to zip file");

        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }


    public static String createZipFileOthers(String xmlDataFileLocation, String xmlFileName) {
        //Zipfile name
        String zipFileLocation = FileUtils.getFileFullPath(TMP_ZIP_FILES_LOCATION, RandomDataUtils.getRandomTestNameAdvanced("NewNotifications")+".zip");
        try {

            //Copy files to zip into a tmpzip folder
            File file = new File(xmlDataFileLocation);
            String tmpZip = FileUtils.getTempFileFullPath() + File.separator + "tmpzip";
            String tmpZipXml = tmpZip + File.separator + xmlFileName;
            Files.copy(file.toPath(), new File(tmpZipXml).toPath()  );

            // input file to zip
            File tmpZipFolder = new File(tmpZip);
            File tmpZipFile = new File(tmpZipXml);
            FileInputStream in = new FileInputStream(tmpZipFile);

            // out put file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileLocation));

            // name the file inside the zip  file
            //out.putNextEntry(new ZipEntry(xmlDataFileLocation));
            for(File f: tmpZipFolder.listFiles()){
                out.putNextEntry(new ZipEntry(f.getAbsolutePath()));
            }

            // buffer size
            byte[] b = new byte[1024];
            int count;

            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
            out.close();
            in.close();
        }catch(Exception e){
            e.printStackTrace();
            zipFileLocation = null;
        }
        return zipFileLocation;
    }
}
