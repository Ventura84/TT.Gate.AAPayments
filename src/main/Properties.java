package main;

import org.apache.log4j.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
//import java.net.UnknownHostException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Properties {

    private static final Logger log = Logger.getLogger(Properties.class);

    public HashMap getProp(Date date, SimpleDateFormat dateFormat) throws UnknownHostException {

        HashMap properties = new HashMap();
        ////////////////////////////


        java.util.Properties config = new java.util.Properties();
        try {
            config.load(new FileInputStream(new File("config.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        properties.put("MODULE_IP",            InetAddress.getByName(config.getProperty("MODULE_IP", "")));
        properties.put("MODULE_PORT",          Integer.parseInt(config.getProperty("MODULE_PORT", "")));
        properties.put("BILLING_IP",           config.getProperty("BILLING_IP", ""));
        properties.put("BILLING_PORT",         Integer.parseInt(config.getProperty("BILLING_PORT", "")));
        properties.put("BILLING_TERM",         config.getProperty("BILLING_TERM", ""));
        properties.put("BILLING_READ_TIMEOUT", Integer.parseInt(config.getProperty("BILLING_READ_TIMEOUT", "")));
        properties.put("DB_IP",                config.getProperty("DB_IP", ""));

        String procName = ManagementFactory.getRuntimeMXBean().getName();
        int index = procName.indexOf('@');
        String pid = procName.substring(0, index);

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("stop.bat"), StandardCharsets.UTF_8))) {
                writer.write("taskkill /PID " + pid + " /F");
            } catch (UnsupportedEncodingException err) {
                //e.printStackTrace();
                log.error("FILE CREATION ERROR", err);
            } catch (IOException err) {
                //e.printStackTrace();
                log.error("PID WRITING ERROR.", err);
            }
        } else {

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("stop.sh"), StandardCharsets.UTF_8))) {
                writer.write("kill " + pid);
                //writer.write("screen -p 0 -S TTGate -X stuff 'stop^m'");
            } catch (UnsupportedEncodingException err) {
                //e.printStackTrace();
                log.error("FILE CREATION ERROR", err);
            } catch (IOException err) {
                //e.printStackTrace();
                log.error("PID WRITING ERROR.", err);
            }
        }

        File std_err = new File("std_err");
        if (!std_err.exists()) {
            std_err.mkdir();
        }

        File std_out = new File("std_out");
        if (!std_out.exists()) {
            std_out.mkdir();
        }

        // OUT
        /*
        try {
            System.setErr(new PrintStream(new FileOutputStream("std_err/std_err_" + dateFormat.format(date) + ".log")));
            System.setOut(new PrintStream(new FileOutputStream("std_out/std_out_" + dateFormat.format(date) + ".log")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/


        ////////////////////////////
        return properties;
    }


}
