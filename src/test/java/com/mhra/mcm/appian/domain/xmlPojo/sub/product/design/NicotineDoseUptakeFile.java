package com.mhra.mcm.appian.domain.xmlPojo.sub.product.design;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * Created by TPD_Auto on 22/08/2016.
 */
public class NicotineDoseUptakeFile {

    @XmlAttribute
    private String attachmentID;
    //@XmlValue
    //private String value;

    public NicotineDoseUptakeFile(String value) {
        //this.value = value;
        this.attachmentID = "65da4094-d0cd-4322-ab6c-782e2f46bb21";
    }
}