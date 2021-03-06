package com.mhra.mcm.appian.domain.xmlPojo.sub;

import com.mhra.mcm.appian.domain.excelpojo.DO_Submitter;
import com.mhra.mcm.appian.domain.xmlPojo.sub.submitter.HasAffiliate;
import com.mhra.mcm.appian.domain.xmlPojo.sub.submitter.HasEnterer;
import com.mhra.mcm.appian.domain.xmlPojo.sub.submitter.Parent;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Map;

/**
 * Created by TPD_Auto on 22/07/2016.
 */
public class Submitter {

    public String name;

    @XmlAttribute
    public boolean confidential;
    @XmlAttribute
    public String submitterID;

    @XmlElement(name = "SubmitterType")
    public String submitterType;
    @XmlElement(name = "HasEnterer")
    public boolean hasEnterer;
    @XmlElement(name = "HasParent")
    public boolean hasParent;
    @XmlElement(name = "HasAffiliate")
    public boolean hasAffiliate;

    @XmlElement(name = "Parent")
    public Parent parent;
    @XmlElement(name = "Enterer")
    public HasEnterer enterer;
    @XmlElement(name = "Affiliate")
    public HasAffiliate affiliate;

    public Submitter(String euID){
        this.confidential = false;
        this.submitterID = euID;
        this.submitterType = "MANUFACTURER";

        this.hasAffiliate = false;
        this.hasEnterer = false;
        this.hasParent = false;
    }

    /**
     *
     * @param confidential
     * @param submitterId
     * @param submitterType
     * @param hasEnterer
     * @param hasAffiliate
     * @param hasParent
     */
    public Submitter(boolean confidential, String submitterId, String submitterType, boolean hasEnterer, boolean hasAffiliate, boolean hasParent) {
        this.confidential = confidential;
        this.submitterID = submitterId;
        this.submitterType = submitterType;

        this.hasParent = hasParent;
        if(hasParent)
            parent = new Parent();

        this.hasAffiliate = hasAffiliate;
        if(hasEnterer)
            enterer = new HasEnterer();

        this.hasEnterer = hasEnterer;
        if(hasAffiliate)
            affiliate = new HasAffiliate();
    }

    public void evaluate() {

        if(hasParent)
            parent = new Parent();

        if(hasEnterer)
            enterer = new HasEnterer();

        if(hasAffiliate)
            affiliate = new HasAffiliate();
    }

    public void addSubmitter(Map<String, String> dataValues, DO_Submitter doSubmitter) {

        String submitterType = doSubmitter.submitterType;
        String hasParent = doSubmitter.hasParent;
        String hasEnterer = doSubmitter.hasEnterer;
        String hasAffiliate = doSubmitter.hasAffiliate;

        if (submitterType != null) {
            this.submitterType = submitterType;
        }
        if (hasParent != null) {
            this.hasParent = Boolean.valueOf(hasParent);
        }
        if (hasEnterer != null) {
            this.hasEnterer = Boolean.valueOf(hasEnterer);
        }
        if (hasAffiliate != null) {
            this.hasAffiliate = Boolean.valueOf(hasAffiliate);
        }

//        if(submitter1.equals("random") || submitter1.equals("default")){
//            this.hasEnterer = RandomDataUtils.getRandomBooleanValue();
//            this.hasAffiliate = RandomDataUtils.getRandomBooleanValue();
//            this.hasParent = RandomDataUtils.getRandomBooleanValue();
//        }else {
//
//            String hasEnterer = dataValues.get("hasEnterer");
//            String hasAffiliate = dataValues.get("hasAffiliate");
//            String hasParent = dataValues.get("hasParent");
//
//            if (submitterType != null) {
//                this.submitterType = submitterType;
//            }
//            if (hasEnterer != null) {
//                this.hasEnterer = Boolean.valueOf(hasEnterer);
//            }
//            if (hasAffiliate != null) {
//                this.hasAffiliate = Boolean.valueOf(hasAffiliate);
//            }
//            if (hasParent != null) {
//                this.hasParent = Boolean.valueOf(hasParent);
//            }
//        }
    }

    @Override
    public String toString() {
        return "Submitter{" +
                "confidential=" + confidential +
                ", submitterID='" + submitterID + '\'' +
                ", submitterType='" + submitterType + '\'' +
                ", hasEnterer=" + hasEnterer +
                ", hasParent=" + hasParent +
                ", hasAffiliate=" + hasAffiliate +
                ", parent=" + parent +
                ", enterer=" + enterer +
                ", affiliate=" + affiliate +
                '}';
    }

    public void setName(String submitterName) {
        this.name = submitterName;
    }
}
