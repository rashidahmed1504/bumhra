package com.mhra.mcm.appian.pageobjects;

import com.mhra.mcm.appian.pageobjects.sections.contents.CreateNotification;
import com.mhra.mcm.appian.pageobjects.sections.contents.ManageSubstances;
import com.mhra.mcm.appian.pageobjects.sections.contents.UpdateQAPercentage;
import com.mhra.mcm.appian.utils.helpers.page.PageUtils;
import com.mhra.mcm.appian.utils.helpers.page.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by TPD_Auto on 18/07/2016.
 */
@Component
public class ActionsPage extends _Page {

    @FindBy(partialLinkText = "Upload Sample Notification")
    WebElement uploadSampleNotification;

    @FindBy(partialLinkText = "Create Standard Invoices")
    WebElement createStandardNotification;

    @FindBy(partialLinkText = "Update QA Percentage")
    WebElement updateQAPercentage;

    @FindBy(partialLinkText = "Create Annual Invoices")
    WebElement createAnnualInvoices;

    @FindBy(partialLinkText = "Manage Substances")
    WebElement manageSubstances;

    @FindBy(partialLinkText = "Create Withdrawn Notifications")
    WebElement createWithdrawalInvoices;

    @Autowired
    public ActionsPage(WebDriver driver) {
        super(driver);
    }

    public CreateNotification clickUploadSampleNotification() {
        WaitUtils.isElementPartOfDomAdvanced2(driver, By.partialLinkText("Upload Sample Notification"), 10, false);
        WaitUtils.waitForElementToBeClickable(driver, uploadSampleNotification, 5);
        uploadSampleNotification.click();
        return new CreateNotification(driver);
    }

    public ActionsPage generateStandardInvoices() {
        WaitUtils.isElementPartOfDomAdvanced2(driver, By.partialLinkText("Create Standard Invoices"), 10, false);
        WaitUtils.waitForElementToBeClickable(driver, createStandardNotification, 5);
        PageUtils.singleClick(driver, createStandardNotification);
        return new ActionsPage(driver);
    }

    public ActionsPage generateAnnualInvoices() {
        WaitUtils.isElementPartOfDomAdvanced2(driver, By.partialLinkText("Create Annual Invoices"), 10, false);
        WaitUtils.waitForElementToBeClickable(driver, createAnnualInvoices, 5);
        PageUtils.singleClick(driver, createAnnualInvoices);
        return new ActionsPage(driver);
    }

    public boolean isNotificationGeneratedSuccessfully() {
        try {
            WaitUtils.waitForElementToBeClickable(driver, uploadSampleNotification, 15, false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSubstanceGeneratedSuccessfully() {
        try {
            WaitUtils.waitForElementToBeClickable(driver, manageSubstances, TIMEOUT_SMALL, false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UpdateQAPercentage clickUpdateQAPercentage() {
        WaitUtils.waitForElementToBeClickable(driver, updateQAPercentage, TIMEOUT_MEDIUM, false);
        PageUtils.singleClick(driver, updateQAPercentage);
        return new UpdateQAPercentage(driver);
    }

    public ManageSubstances clickManageSubstances() {
//        try {
//            WaitUtils.isElementPartOfDomAdvanced2(driver, By.partialLinkText("Manage Substances"), 10, false);
//            WaitUtils.waitForElementToBeClickable(driver, manageSubstances, 30, false);
//            PageUtils.doubleClick(driver, manageSubstances);
//            return new ManageSubstances(driver);
//        }catch (Exception e){
//            return null;
//        }
        WaitUtils.isElementPartOfDomAdvanced2(driver, By.partialLinkText("Manage Substances"), 10, false);
        WaitUtils.waitForElementToBeClickable(driver, manageSubstances, 30, false);
        PageUtils.doubleClick(driver, manageSubstances);
        return new ManageSubstances(driver);
    }

    public ActionsPage generateWithdrawalInvoice() {
        WaitUtils.waitForElementToBeClickable(driver, createWithdrawalInvoices, 15, false);
        PageUtils.singleClick(driver, createWithdrawalInvoices);
        return new ActionsPage(driver);
    }
}
