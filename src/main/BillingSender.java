package main;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class BillingSender {

    private static final Logger log = Logger.getLogger(BillingSender.class);

    private static String getHash(String str)throws Exception {

        //String fullStr = str + "TestHashKey";
        String fullStr = str + "ApiHashKeyTM";
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(fullStr.getBytes());

        byte[] dataBytes = md.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataBytes.length; i++) {
            sb.append(Integer.toString((dataBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }



    private static String makeVerifyXML(final String terminal, String procProduct, String phoneNumber, String amount, String commission) throws Exception {

        String hash = getHash(terminal+procProduct+phoneNumber+amount+commission);

        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope " +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                "<soap:Body>" +
                "<Verify xmlns=\"http://tempuri.org/\">" +
                "<terminalName>" + terminal + "</terminalName>" +
                "<processingProductId>" + procProduct + "</processingProductId>" +
                "<invoiceData>" + phoneNumber + "</invoiceData>" +
                "<amount>" + amount + "</amount>" +
                "<commission>" + commission + "</commission>" +
                "<hash>" + hash + "</hash>" +
                "</Verify>" +
                "</soap:Body>" +
                "</soap:Envelope>";
    }



    private static String makeGenerateXML(){


        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope " +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                "<soap:Body>" +
                "<GenerateIRRN xmlns=\"http://tempuri.org/\">" +
                "</GenerateIRRN>" +
                "</soap:Body>" +
                "</soap:Envelope>";
    }


    private static String makePayXML(final String terminal, String procProduct, String phoneNumber, String amount, String commission, String irrn) throws Exception {

        String hash = getHash(terminal+procProduct+phoneNumber+amount+commission+irrn);
        //System.out.println("HASH : " + hash);

        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope " +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                "<soap:Body>" +
                "<Pay xmlns=\"http://tempuri.org/\">" +
                "<terminalName>" + terminal + "</terminalName>" +
                "<processingProductId>" + procProduct + "</processingProductId>" +
                "<invoiceData>" + phoneNumber + "</invoiceData>" +
                "<amount>" + amount + "</amount>" +
                "<commission>" + commission + "</commission>" +
                "<IRRN>" + irrn + "</IRRN>" +
                "<hash>" + hash + "</hash>" +
                "</Pay>" +
                "</soap:Body>" +
                "</soap:Envelope>";
    }

    private static Element getRootTag(String resXml) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(resXml)));

        //Element rootTag = document.getDocumentElement();
        //System.out.println("ROOT ELEMENT : " + rootTag);


        //return rootTag;
        return document.getDocumentElement();
    }



    private static String getTagValue(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();
            if (subList != null && subList.getLength() > 0) {
                return subList.item(0).getNodeValue();
            }
        }
        return null;
    }


    public String sendRequest(String ip, String bill_ip, int bill_port, String request, String terminalName, String processingProduct, String phoneNumber, String amount, String commission, String irrn) throws Exception {
        //String result = "";

        // Make Stream To TILSIMAT
        //URL url = new URL("http://217.174.225.250:29775/service.asmx");
        URL url = new URL("http://" + bill_ip + ":" + bill_port + "/service.asmx");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type","text/xml");
        con.setDoOutput(true);
        DataOutputStream toBill = new DataOutputStream(con.getOutputStream());
        // Make Stream To TILSIMAT END

        String xml = "";

        if(request.equals("VERIFY")) {
            xml = makeVerifyXML(terminalName, processingProduct, phoneNumber, amount, commission);
        } else if(request.equals("GENERATE")){
            xml = makeGenerateXML();
        } else if(request.equals("PAY")){
            xml = makePayXML(terminalName, processingProduct, phoneNumber, amount, commission, irrn);
        }

        /*
        switch (request){
            case "verify" : {
                xml = makeVerifyXML(terminalName, processingProduct, phoneNumber, amount, commission);
            }
            case "generate" : {
                xml = makeGenerateXML();
            }
            case "pay" : {
                xml = makePayXML(terminalName, processingProduct, phoneNumber, amount, commission, irrn);
            }
        }*/
        //System.out.println(request + " REQUEST : " + xml);
        log.info(ip + " " + request + " REQUEST : " + xml);
        toBill.writeBytes(xml);
        toBill.flush();

        //int responseCode = con.getResponseCode();

        if(con.getResponseCode() == 200) {

            //System.out.println("RESP CODE : " + 200 + "\n");
            BufferedReader fromBill = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String output;
            StringBuilder res = new StringBuilder();
            while ((output = fromBill.readLine()) != null) {
                res.append(output);
            }

            //System.out.println(request + " RESPONSE : " + res.toString());

            String xmlRes = res.toString();
            log.info(ip + " " + request + " RESPONSE : " + xmlRes);
            Element rootTag = getRootTag(xmlRes);


            if((request.equals("VERIFY")) || (request.equals("PAY"))){
                //result = getTagValue("ResultMessage", rootTag);
                fromBill.close();
                //return getTagValue("ResultMessage", rootTag);
                if(getTagValue("ResultMessage", rootTag).equals("OK")){
                    return "OK";
                } else {
                    return getTagValue("ResultMessage", rootTag) + " " + getTagValue("ResultCode", rootTag);
                }

            } else if (request.equals("GENERATE")){
                //result = getTagValue("GenerateIRRNResult", rootTag);
                fromBill.close();
                return getTagValue("GenerateIRRNResult", rootTag);

            }

            fromBill.close();

        } else {

            toBill.close();
            return null;
        }

        toBill.close();
        return null;
    }



}
