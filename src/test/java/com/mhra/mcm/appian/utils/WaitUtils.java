package com.mhra.mcm.appian.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by TPD_Auto on 15/07/2016.
 */
public class WaitUtils {

    static int timeForTesting = 10;

    public static void waitForElementToBeClickable(WebDriver driver, WebElement element, int maxTimeToWait) {
        maxTimeToWait = resetMaxTime(maxTimeToWait);
        new WebDriverWait(driver, maxTimeToWait).until(ExpectedConditions.elementToBeClickable(element));
    }

    public static void waitForElementToBeClickable(WebDriver driver, By by,  int maxTimeToWait) {
        maxTimeToWait = resetMaxTime(maxTimeToWait);
        new WebDriverWait(driver, maxTimeToWait).until(ExpectedConditions.elementToBeClickable(by));
    }

    public static void waitForElementToBeVisible(WebDriver driver, WebElement element, int maxTimeToWait) {
        maxTimeToWait = resetMaxTime(maxTimeToWait);
        new WebDriverWait(driver, maxTimeToWait).until(ExpectedConditions.visibilityOf(element));
    }

    private static int resetMaxTime(int maxTimeToWait) {
        if(timeForTesting > 0){
            maxTimeToWait = timeForTesting;
        }
        return maxTimeToWait;
    }

    public static String getText(WebElement submitterName) {
        submitterName.click();
        String existingName = submitterName.getText();
        if(existingName.equals(""))
            existingName = submitterName.getAttribute("value");
        return existingName;
    }
}
