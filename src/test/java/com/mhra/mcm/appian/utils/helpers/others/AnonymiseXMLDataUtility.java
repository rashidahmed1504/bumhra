package com.mhra.mcm.appian.utils.helpers.others;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Help with Anonymizing XML data
 */
public class AnonymiseXMLDataUtility {

    public static void XMLReplaceTextWith(boolean useBarylsChanges, boolean overrideSpecificTags, String original, String created, String searchForTag, String replaceWith, List<String> listOfIgnoreTags) {
        try {
            //Beryl has specified list of tags which will be displayed even if users have set it to confidential=true
            if(!useBarylsChanges){
                listOfIgnoreTags = new ArrayList<>();
            }

            String filepath = original;
            String writeTo = created;
            System.out.println("Input : " + filepath);
            System.out.println("Output : " + writeTo);
            System.out.println("Search for tags matching xpath : " + searchForTag);
            System.out.println("Replace with following text : " + replaceWith);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = null;
            try {
                expr = xpath.compile(searchForTag);
                NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                //element count
                System.out.println("Number of items to annonymise : " + list.getLength());

                for (int i = 0; i < list.getLength(); i++) {

                    Node node = list.item(i);
                    String parentName = node.getNodeName();
                    boolean contains = listOfIgnoreTags.contains(parentName);
                    if(!contains) {
                        NodeList children = node.getChildNodes();
                        if (children.getLength() > 1) {
                            for (int x = 0; x < children.getLength(); x++) {
                                Node child = children.item(x);
                                if (child.getNodeType() == Node.ELEMENT_NODE) {
                                    String childName = child.getNodeName();
                                    contains = listOfIgnoreTags.contains(childName);
                                    if(!contains) {
                                        child.setTextContent(replaceWith);
                                    }
                                    //String childText = child.getTextContent();
                                    //System.out.println(child.getNodeName() + " " + childText);
                                }
                            }
                        } else {
                            //System.out.println("No children");
                            node.setTextContent(replaceWith);
                        }
                    }
//                    Node node = list.item(i);
//                    node.setTextContent("CONFIDENTIAL_DATA");


                }

                //Override and replace specific ones with any info which can identify the product owner or company details
                if(overrideSpecificTags)
                overrideAndReplaceSpecificTags(replaceWith, xpath, doc);

                //Write new file to disk
                Transformer xformer = TransformerFactory.newInstance().newTransformer();
                xformer.transform(new DOMSource(doc), new StreamResult(new File(writeTo)));

            } catch (XPathExpressionException e) {
                e.printStackTrace();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }

            System.out.println("Done\n");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void replaceSpecificTags(String expr, String replaceWith, XPath xpath, Document doc) throws XPathExpressionException {

        XPathExpression expr2 = xpath.compile(expr);
        NodeList list2 = (NodeList) expr2.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < list2.getLength(); i++) {
            //System.out.println(list2.getLength());
            //System.out.println(node.getTextContent());
            Node node = list2.item(i);
            node.setTextContent(replaceWith);
        }
    }


    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    /**
     * This list of items received from Chris Dale, requested by Beryl
     * @return
     */
    private static List<String> getListOfIgnoreTags() {

        //Beryl has highlighted in the list appended below, those XML data fields which will never be Confidential
        List<String> listOfIgnoreTags = new ArrayList<>();
        listOfIgnoreTags.add("ProductID");
        listOfIgnoreTags.add("SubmitterType");
        listOfIgnoreTags.add("ProductType");

        listOfIgnoreTags.add("Volume");                 //not tested
        listOfIgnoreTags.add("BrandName");

        listOfIgnoreTags.add("Description");
        listOfIgnoreTags.add("LiquidVolumeCapacity");
        listOfIgnoreTags.add("NicotineConcentration");
        listOfIgnoreTags.add("ProductionConformity");
        listOfIgnoreTags.add("QualitySafety");
        listOfIgnoreTags.add("ChildTamperProof");
        listOfIgnoreTags.add("HighPurity");
        listOfIgnoreTags.add("NonRisk");
        listOfIgnoreTags.add("ConsistentDosing");
        listOfIgnoreTags.add("LeafletFile");

        //Company_Name
        //Company_Country
        //Ingredient_Name – unless Ingredient_Recipe_Quantity <0.1%
        listOfIgnoreTags.add("Name");
        listOfIgnoreTags.add("Country");


        return listOfIgnoreTags;
    }


    /**
     * ONLY USED FOR ReferenceData Team
     * @param replaceWith
     * @param xpath
     * @param doc
     * @throws XPathExpressionException
     */
    private static void overrideAndReplaceSpecificTags(String replaceWith, XPath xpath, Document doc) throws XPathExpressionException {
        String testPhone= "+44 207 845 1234567";
        String testEmail = "mhra.uat@gmail.com";
        replaceSpecificTags(".//PhoneNumber", testPhone, xpath, doc);
        replaceSpecificTags(".//Email", testEmail, xpath, doc);

        //Requested by Lambros 22/09/2016
        applyLamrosesRequest(xpath, doc);
    }

    private static void applyLamrosesRequest(XPath xpath, Document doc) throws XPathExpressionException {
        String submitterID = RandomDataUtils.getSimpleRandomNumberBetween(10000, 99999);
        String versioningID = RandomDataUtils.getSimpleRandomNumberBetween(10000, 99999);
        String addressNumber = RandomDataUtils.getSimpleRandomNumberBetween(1, 500);
        replaceSpecificTags(".//GeneralComment", "Random Comment", xpath, doc);
        replaceSpecificTags(".//ProductID", submitterID + "-16-" + versioningID, xpath, doc);
        replaceSpecificTags(".//BrandName", "MHRA Brand Name", xpath, doc);
        replaceSpecificTags(".//ProductIdentification", "Product Identification Text", xpath, doc);
        replaceSpecificTags(".//Description", "Product Description of this e-liquid", xpath, doc);
        replaceSpecificTags(".//Submitter/@submitterID", submitterID, xpath, doc);
        replaceSpecificTags(".//Manufacturer/@submitterID", submitterID, xpath, doc);
        replaceSpecificTags(".//Address", addressNumber + " Buckingham Palace Road, London, SW1W 9SZ", xpath, doc);

        replaceSpecificTags(".//IdentificationRefillContainerCartridge", "Refill container Description eg Cherry 2mg", xpath, doc);
        replaceSpecificTags(".//IdentificationEcigDevice", "Refill container Description eg Cherry 2mg", xpath, doc);
        replaceSpecificTags(".//ProductNumber", submitterID + "-16-" + versioningID, xpath, doc);
        replaceSpecificTags(".//ProductCombination", "20W eCig device with Cherry 2mg", xpath, doc);
        replaceSpecificTags(".//BrandSubtypeName", RandomDataUtils.getRandomTestName("SubType Brand"), xpath, doc);
        replaceSpecificTags(".//Parent/Name", RandomDataUtils.getRandomTestName("Parent Name"), xpath, doc);
        replaceSpecificTags(".//Enterer/Name", RandomDataUtils.getRandomTestName("Enterer Name"), xpath, doc);
        replaceSpecificTags(".//Affiliate/Name", RandomDataUtils.getRandomTestName("Affiliate Name"), xpath, doc);
        //replaceSpecificTags(".//Ingredient/Name", "Ingredient", xpath, doc);
        replaceSpecificTags(".//Manufacturer/Name", RandomDataUtils.getRandomTestName("Manufacturer Name"), xpath, doc);
    }


    public static void main(String args[]) {
        //CONTROLS
        boolean applyTagsSpecifiedByBeryl = true;
        boolean overrideSpecificTags = true;
        boolean makeDataConfidential = false;

        String confidentialXPath = ".//*[@confidential='true']";
        if(!makeDataConfidential){
            confidentialXPath = ".//*[@confidential='trues']";
        }

        String os = System.getProperty("os.name");
        String xmlFolderLocation = "C:\\Selenium\\xmlData\\originalFiles";
        File locationOutput = new File("C:\\Selenium\\xmlData\\" + getFileName(applyTagsSpecifiedByBeryl) + new Date().toString().substring(0, 16).replace(":", "").replace(" ", "").trim());

        if(os.toLowerCase().contains("mac")){
            xmlFolderLocation = "/Users/tayyibah/Downloads/xml/originalFiles";
            locationOutput = new File("/Users/tayyibah/Downloads/xml/AnonymisedFiles" + new Date().toString().substring(0, 16).replace(":", "").replace(" ", "").trim());
        }

        locationOutput.mkdir();
        File location = new File(xmlFolderLocation);
        String locationInputFullPath = location.getAbsolutePath();
        String locationOutputFullPath = locationOutput.getAbsolutePath();

        List<String> listOfIgnoreTags = getListOfIgnoreTags();


        String[] listOfXmlFiles = location.list();

        AnonymiseXMLDataUtility fr = new AnonymiseXMLDataUtility();
        for (String fileName : listOfXmlFiles) {
            try {
                String createdFileName = fileName.replaceAll(".xml", "_ANON.xml");
                String original = locationInputFullPath + File.separator + fileName;
                String created = locationOutputFullPath + File.separator + createdFileName;

                File isFile = new File(original);
                if(isFile.isFile()) {
                    fr.XMLReplaceTextWith(applyTagsSpecifiedByBeryl, overrideSpecificTags, original, created, confidentialXPath, "CONFIDENTIAL DATA", listOfIgnoreTags);
                }
            } catch (Exception e) {

            }
        }

    }

    private static String getFileName(boolean useBarylsChanges) {
        String beginsWith = "Anonymised";
        if(useBarylsChanges){
            beginsWith = "AnonymisedWithBerylsChanges";
        }
        return beginsWith;
    }
}