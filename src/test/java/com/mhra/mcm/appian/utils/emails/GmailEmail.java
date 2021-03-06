package com.mhra.mcm.appian.utils.emails;

import com.mhra.mcm.appian.domain.sort.SortByMessageNumber;
import com.mhra.mcm.appian.domain.webPagePojo.sub.Invoice;
import com.mhra.mcm.appian.utils.helpers.others.FileUtils;
import com.mhra.mcm.appian.utils.helpers.others.RandomDataUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.*;

/**
 * Created by TPD_Auto on 29/07/2016.
 */


public class GmailEmail {

    public static final String UNINVOICED_NOTIFICATIONS = "Uninvoiced Notifications";
    public static final String ANNUAL_INVOICED_NOTIFICATIONS = "Annual Notification Invoices";
    public static final String REFUSAL_FOR_NOTIFICATION = "Refusal For Notification";
    public static final String NO_NEW_NOTIFICATIONS = "No new notifications";
    public static final String WITHDRAWAL = "Withdrawn Notifications";
    public static final String NO_WITHDRAWN_NOTIFICATIONS = "No withdrawn notifications";
    private static final String resourceFolder = "src" + File.separator + "test" + File.separator + "resources" + File.separator;

    private static List<Invoice> listOfInvoices = new ArrayList<>();
    private static StringBuilder refusalForEmailContent;
    private static boolean refusalEmailReceived;
    private static boolean withdrawalEmailReceived;
    private static boolean noNewNotificaitonsEmailReceived;
    private static boolean noWithdrawnNotificaitonsEmailReceived;

    public static void main(String[] args) {

        GmailEmail gmail = new GmailEmail();
        //gmail.getListOfInvoicesFromGmail(155);
        String i = "TPD_252253_20160729,2016-07-29,,252253,TestE2E_29_6,,TPD Initial Notification,1,150,150,0,150,86551-16-35315,M0707,1770,,,TestE2EBrand 301,Standard Invoice,mhra.uat@gmail.com";
        gmail.sendPaidEmailToAppian(new Invoice(i));

    }


    public static List<Invoice> getListOfInvoicesFromGmail(double min, String ecID, String subjectHeading) {
        //generate list of invoices
        read(min, subjectHeading);
        filterListOfInvoicesByEcid(ecID);
        return listOfInvoices;
    }

    public static List<Invoice> getListOfInvoicesFromGmail(double min, String subjectHeading) {
        //generate list of invoices
        read(min, subjectHeading);
        return listOfInvoices;
    }

    private static void filterListOfInvoicesByEcid(String ecID) {
        boolean contains = false;
        //check invoice with ecid exists
        for (Invoice invoice : listOfInvoices) {
            String descECid = invoice.Description;
            if (descECid != null && descECid.equals(ecID)) {
                contains = true;
                break;
            }
        }

        if (!contains) {
            //remove all invoices
            listOfInvoices.clear();
        }
    }

    public static void read(double min, String subjectHeading) {

        Properties props = new Properties();
        try {

            //props.load(new FileInputStream(new File(getDataFileFullPath("smtp.properties") + File.separator )));
            //Session session = Session.getDefaultInstance(props, null);

            final String username = "mhra.uat@gmail.com";
            final String password = "MHRA1234";

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            Store store = session.getStore("imaps");
            store.connect("smtp.gmail.com", username, password);

            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);

            int messageCount = inbox.getMessageCount();
            Message[] messages = inbox.getMessages();
            List<Message> lom = Arrays.asList(messages);
            Collections.sort(lom, new SortByMessageNumber());
            messages = lom.toArray(new Message[lom.size()]);
            //System.out.println("Number of messages : " + messages.length);


            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                Date sentDate = message.getSentDate();
                String subject = message.getSubject();
                //System.out.println(subject);
                Address[] froms = message.getFrom();

                for (Address from : froms) {
                    String emailAddress = froms == null ? null : ((InternetAddress) from).getAddress();
                    if (emailAddress != null && emailAddress.contains("appian")) {

                        boolean isMessageReceivedToday = isMessageReceivedToday(subject, subjectHeading, sentDate);
                        if ((isMessageReceivedToday && subject.contains(subjectHeading)) || subject.contains(REFUSAL_FOR_NOTIFICATION) || subject.contains(UNINVOICED_NOTIFICATIONS)
                                || subject.contains(WITHDRAWAL) || subject.contains(NO_NEW_NOTIFICATIONS) || subject.contains(NO_WITHDRAWN_NOTIFICATIONS)) {

                            //If recent
                            boolean isRecent = receivedInLast(min, sentDate);
                            if (isRecent && subject.contains(UNINVOICED_NOTIFICATIONS)) {
                                System.out.println("---------------------------------");
                                System.out.println("Recent email received");
                                System.out.println("---------------------------------");
                                writePart(message);
                                System.out.println("Number of invoices : " + listOfInvoices.size());
                            } else {

                                if (isRecent && subject.contains(ANNUAL_INVOICED_NOTIFICATIONS)) {
                                    System.out.println("---------------------------------");
                                    System.out.println("Recent email received for : Annual Invoiced Notifications");
                                    System.out.println("---------------------------------");
                                    writePart(message);
                                    System.out.println("Number of invoices : " + listOfInvoices.size());
                                }

                                if (isRecent && subject.contains(WITHDRAWAL)) {
                                    System.out.println("---------------------------------");
                                    System.out.println("Recent email received for : Withdrawal For Notification");
                                    System.out.println("---------------------------------");
                                    withdrawalEmailReceived = true;
                                    writePart(message);
                                    System.out.println("Number of invoices : " + listOfInvoices.size());
                                    break;
                                }

                                if (isRecent && subject.contains(REFUSAL_FOR_NOTIFICATION)) {
                                    System.out.println("---------------------------------");
                                    System.out.println("Recent email received for : Refusal For Notification");
                                    System.out.println("---------------------------------");
                                    refusalEmailReceived = true;
                                    writePartPDF(message);
                                    break;
                                }

                                if (isRecent && subject.contains(NO_NEW_NOTIFICATIONS)) {
                                    System.out.println("---------------------------------");
                                    System.out.println("Recent email received for : No New Notifications");
                                    System.out.println("---------------------------------");
                                    noNewNotificaitonsEmailReceived = true;
                                    break;
                                }

                                if (isRecent && subject.contains(NO_WITHDRAWN_NOTIFICATIONS)) {
                                    System.out.println("---------------------------------");
                                    System.out.println("Recent email received for : No Withdrawn Notifications");
                                    System.out.println("---------------------------------");
                                    noWithdrawnNotificaitonsEmailReceived = true;
                                    break;
                                }
                            }
                        } else {
                            //System.out.println("Message is old or not relevant" );
                        }
                    }
                }
            }

            System.out.println("Total Messages : " + messageCount);
            inbox.close(true);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Send email to appian for a Invoice.
     *
     * @param invoice
     * @return
     */
    public static boolean sendPaidEmailToAppian(Invoice invoice) {

        final String username = "mhra.uat@gmail.com";
        final String password = "MHRA1234";
        final String from = "mhra.uat@gmail.com";
        final String to = "processmodeluuid0017dd1e-4fb8-8000-f92f-7f0000014e7a@mhratest.appiancloud.com," + from;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            //create a temporary file
            File temp = generateCSVResponse(invoice);

            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject("Test Payment");

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText("This should pay the invoice : " + invoice.Description);

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            String filename = temp.getAbsolutePath();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(temp.getName());
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);

            // Send message
            Transport.send(message);

            //remove temp file
            temp.delete();

            System.out.println("\nSent PAYMENT response message to APPIAN successfully....");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    /**
     * Creates an response which will be added as an attachment
     *
     * @param invoice
     * @return
     * @throws Exception
     */
    public static File generateCSVResponse(Invoice invoice) throws Exception {

        String tp2 = FileUtils.getTempFileFullPath() + File.separator + "workbook" + RandomDataUtils.getRandomNumberBetween(1000, 10000) + ".xlsx";
        File temp2 = new File(tp2);
        temp2.createNewFile();

        Workbook wb = new XSSFWorkbook();
        CreationHelper helper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet("Sheet1");

        XSSFCellStyle cellStyle = (XSSFCellStyle) wb.createCellStyle();
        cellStyle.setDataFormat(
                helper.createDataFormat().getFormat("dd/MM/yyyy"));

        Row row = sheet.createRow((short) 0);
        row.createCell(0).setCellValue(("ecId"));
        row.createCell(1).setCellValue(("invoiceNumber"));
        row.createCell(2).setCellValue(("invoiceDate"));
        Row row2 = sheet.createRow((short) 1);
        row2.createCell(0).setCellValue((invoice.Description));
        row2.createCell(1).setCellValue((invoice.Invoice_Id));
        //row2.createCell(2).setCellValue((getTodaysDate(1)));
        Cell cell = row2.createCell(2);
        cell.setCellValue(new Date());
        cell.setCellStyle(cellStyle);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(tp2);
        wb.write(fileOut);
        fileOut.close();

        return temp2;
    }

    private static String getTodaysDate(int format) {
        Calendar calendar = Calendar.getInstance();
        int dom = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        String f1 = dom + "/0" + month + "/" + year;
        return f1;
    }

    /**
     * We are only interested in todays email
     *
     * @param subject
     * @param subjectHeading
     * @param sentDate       @return
     */
    private static boolean isMessageReceivedToday(String subject, String subjectHeading, Date sentDate) {

        if (subjectHeading == null || subjectHeading.equals("") || subjectHeading.contains("Annual Notification Invoices")) {
            return true;
        } else {
            Calendar calendar = Calendar.getInstance();
            int dom = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);

            String f1 = dom + "/" + month + "/" + year;
            String f2 = dom + "/0" + month + "/" + year;

            if (subject.contains(f1) || subject.contains(f2))
                return true;
            else
                return false;
        }
    }

    /**
     * Check if email was received in the last X minuit
     *
     * @param i
     * @param receivedDate
     * @return
     */
    private static boolean receivedInLast(double i, Date receivedDate) {
        long receivedTime = receivedDate.getTime();
        long currentTime = new Date().getTime();
        double time = i * 60 * 1000;

        if (receivedTime > (currentTime - time))
            return true;
        else
            return false;
    }



    /*
    * This method checks for content-type
    * based on which, it processes and
    * fetches the content of the message
    */

    public static void writePart(Part p) throws Exception {
        //if (p instanceof Message)
        //Call methos writeEnvelope
        //writeEnvelope((Message) p);

        if (p.isMimeType("multipart/*")) {
            System.out.println("----------------------------");
            System.out.println("CONTENT-TYPE: " + p.getContentType());
            System.out.println("This is a Multipart");
            System.out.println("---------------------------");
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                writePart(mp.getBodyPart(i));

        } else {

            Object o = p.getContent();
            if (o instanceof InputStream) {
                System.out.println("This is just an input stream");
                System.out.println("---------------------------");
                InputStream is = (InputStream) o;

                BufferedReader br = null;
                StringBuilder sb = new StringBuilder();
                String line;
                try {
                    int lineNumber = 0;
                    br = new BufferedReader(new InputStreamReader(is));
                    while ((line = br.readLine()) != null) {
                        //Ignore first line
                        if (lineNumber > 0) {
                            sb.append(line + "\n");
                            Invoice invoice = new Invoice(line);
                            //if (!listOfInvoices.contains(invoice))
                            listOfInvoices.add(invoice);
                        }
                        lineNumber++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println(sb.toString());
            }
        }
    }


    public static void writePartPDF(Part p) throws Exception {


        if (p.isMimeType("multipart/*")) {

            refusalForEmailContent = new StringBuilder();
            System.out.println("----------------------------");
            System.out.println("CONTENT-TYPE: " + p.getContentType());
            System.out.println("This is a Multipart");
            System.out.println("---------------------------");
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                try {
                    BodyPart bodyPart = mp.getBodyPart(i);
                    InputStream is = bodyPart.getInputStream();
                    PDDocument document = null;
                    document = PDDocument.load(is);
                    document.getClass();
                    if (!document.isEncrypted()) {
                        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                        stripper.setSortByPosition(true);
                        PDFTextStripper Tstripper = new PDFTextStripper();
                        String st = Tstripper.getText(document);
                        //System.out.println("Text:" + st);
                        refusalForEmailContent.append(st);
                    }
                } catch (Exception e) {
                }
            }

        } else {

            Object o = p.getContent();
            if (o instanceof InputStream) {
                System.out.println("This is just an input stream");
                System.out.println("---------------------------");
                InputStream is = (InputStream) o;

                BufferedReader br = null;
                StringBuilder sb = new StringBuilder();
                String line;
                try {
                    int lineNumber = 0;
                    br = new BufferedReader(new InputStreamReader(is));
                    while ((line = br.readLine()) != null) {
                        //Ignore first line
                        //if (lineNumber > 0) {
                        sb.append(line + "\n");
                        //Invoice invoice = new Invoice(line);
                        //if (!listOfInvoices.contains(invoice))
                        //listOfInvoices.add(invoice);
                        //}
                        lineNumber++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println(sb.toString());
            }
        }
    }

    /*

    * This method would print FROM,TO and SUBJECT of the message

    */

    public static void writeEnvelope(Message m) throws Exception {
        System.out.println("This is the message envelope");
        System.out.println("---------------------------");
        Address[] a;
        // FROM
        if ((a = m.getFrom()) != null) {
            for (int j = 0; j < a.length; j++)
                System.out.println("FROM: " + a[j].toString());
        }

        // TO
        if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++)
                System.out.println("TO: " + a[j].toString());
        }

        // SUBJECT
        if (m.getSubject() != null)
            System.out.println("SUBJECT: " + m.getSubject());

    }


    public static boolean isWithdrawalEmailReceived() {
        return withdrawalEmailReceived;
    }

    public static boolean isRefusalEmailReceived() {
        return refusalEmailReceived;
    }

    public static boolean isNoNewNotificationsEmailReceived() {
        return noNewNotificaitonsEmailReceived;
    }

    public static boolean isNoWithdrawnNotificationsEmailReceived() {
        return noWithdrawnNotificaitonsEmailReceived;
    }

    public static String getRefusalEmailContent() {
        return refusalForEmailContent.toString();
    }
}