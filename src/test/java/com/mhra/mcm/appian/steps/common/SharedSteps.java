package com.mhra.mcm.appian.steps.common;

import com.mhra.mcm.appian.session.SessionKey;
import com.mhra.mcm.appian.utils.helpers.page.WaitUtils;
import com.mhra.mcm.appian.utils.reporter.CreatePrettyReport;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.springframework.context.annotation.Scope;

/**
 * @author TPD_Auto
 * 
 */
@Scope("cucumber-glue")
public class SharedSteps extends CommonSteps {

	public static CreatePrettyReport pr;

	/**
	 * Take screen shot
	 * @param scenario
	 */
	@After  
    public void embedScreenshot(Scenario scenario) {
		generatePrettyReportOnTheGo();
        if (driver!=null && scenario.isFailed()) {
        	log.info("Scenario Failed");
    		log.info("==================================\n");
            try {  
            	byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            	scenario.embed(bytes, "image/png");
            } catch (WebDriverException wde) {  
                System.err.println(wde.getMessage());  
            } catch (ClassCastException cce) {  
                cce.printStackTrace();  
            } catch (Exception e){
            	e.printStackTrace();
            }
        }  else{
        	log.info("Scenario Passed");
    		//log.info("\n==================================");
        	
        }
    }


	@Before
	public void logScenarioNames(Scenario scenario) {
		//WaitUtils.setImplicitWaits(driver);
		//PageUtils.setBrowserZoom(driver, currentBrowser);
		generatePrettyReportOnTheGo();
		if(driver!=null){
			//isThereAnAlert();
			log.info("\n==================================\n");
			log.info("NEW SCENARIO");
			log.info(scenario.getName());
			log.info("\n==================================\n");

			//store current scenario and test environment details
			driver.manage().deleteAllCookies();
			driver.manage().window().maximize();
			scenarioSession.putData(SessionKey.scenarioName, scenario);
			String env = System.getProperty("spring.profiles.active");
			scenarioSession.putData(SessionKey.environment, env);
		}

	}

	private void isThereAnAlert() {
		WaitUtils.waitForAlert(driver,2,false);
		boolean alertFound = WaitUtils.isAlertPresent(driver);
		if(alertFound){
			driver.switchTo().alert().accept();
		}
	}


	/**
     * This will generate pretty report on the go
     */
    private void generatePrettyReportOnTheGo() {
		String generateReport = System.getProperty("generate.report");
		if(pr == null && generateReport != null && ( generateReport.trim().equals("true") || generateReport.trim().equals("yes"))){
            log.info("Will Create Pretty Report On The Go");
			log.info("Have you configured it : --plugin json:target/report_name_what_ever_you_want.json");
            pr = new CreatePrettyReport();
            pr.monitorFolder(PRETTY_REPORT, false);
        }
    }

}
