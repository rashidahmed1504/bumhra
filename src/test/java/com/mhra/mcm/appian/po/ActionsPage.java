package com.mhra.mcm.appian.po;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mhra.mcm.appian.po.sections.contents.CreateNotification;
import com.mhra.mcm.appian.utils.helpers.WaitUtils;
import com.mhra.mcm.appian.utils.helpers.page.PageUtils;

/**
 * Created by TPD_Auto on 18/07/2016.
 */
@Component
public class ActionsPage extends _Page {

    @FindBy(partialLinkText = "Upload Sample Notification")
    WebElement uploadSampleNotification;

    @FindBy(partialLinkText = "Create Standard Invoices")
    WebElement createStandardNotification;

    @Autowired
    public ActionsPage(WebDriver driver) {
        super(driver);
    }

    public CreateNotification clickUploadSampleNotification() {

        WaitUtils.waitForElementToBeClickable(driver, uploadSampleNotification, 5);
        uploadSampleNotification.click();
        return new CreateNotification(driver);
    }

    public ActionsPage generateStandardInvoices() {
        WaitUtils.waitForElementToBeClickable(driver, createStandardNotification, 5);
        PageUtils.singleClick(driver, createStandardNotification);
        //createStandardNotification.click();
        return new ActionsPage(driver);
    }

    public boolean isInCorrectPage() {
        WaitUtils.waitForElementToBeClickable(driver, uploadSampleNotification, 20, false);
        return true;
    }
}
