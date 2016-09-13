package com.mhra.mcm.appian.session;

/**
 * Will be used in ScenarioSession
 * 
 * Helps with maintaining data between steps
 * 
 * @author TPD_Auto
 *
 */
public class SessionKey {
	//Current env and scenario
	public static String environment;
	public static String scenarioName;

	public static final String ECID = "EC ID";
	public static String storedValue = "StoredValue";

	public static String storedNotification = "Notification";
	public static String invoice = "Invoice";
	public static String notificationStatus = "Notification Status";
	public static String submitter = "Submitter";
	public static String listOfInvoices = "List Of Invoices";
	public static String notificationCount = "Notifications Count";
	public static String comment = "Comments";
	public static String substance = "Substance";
	public static String bannedTxt = "Product Banned";
}
