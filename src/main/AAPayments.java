package main;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AAPayments {

    private static final Logger log = Logger.getLogger(AAPayments.class);
    private static HashMap PROP;


    public static void main(String[] args) {
        //System.out.println("STARTED !");


        DBWorker DBWorker = new DBWorker();
        try {
            DBWorker.createTablesOnStart();
        } catch (SQLException e) {
            log.error("ERROR. CAN`T CREATE TABLES ON START", e);
            System.exit(0);
        }

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");



        Properties Properties = new Properties();
        try{
            PROP = Properties.getProp(date, dateFormat);
        } catch (Exception err){
            log.error("ERROR. CAN`T GET PROPERTIES", err);
            System.exit(0);
        }


        try (
                ServerSocket AAPaymentGateway = new ServerSocket((int) PROP.get("MODULE_PORT"), 0, (InetAddress) PROP.get("MODULE_IP"))
        ){

            log.info("\n\n" +
                    "======================================================\n" +
                    "AA.PaymentsGateway v 1.0 (XML) TO TilsimatBill.HOST\n" +
                    "STARTED AT " + dateFormat.format(date) + "\n" +
                    "MODULE_IP        :   " + String.valueOf(PROP.get("MODULE_IP")).substring(1) + "\n" +
                    "MODULE_PORT      :   " + PROP.get("MODULE_PORT") + "\n" +
                    "BILLING_IP       :   " + PROP.get("BILLING_IP") + "\n" +
                    "BILLING_PORT     :   " + PROP.get("BILLING_PORT") + "\n" +
                    "BILLING_PORT     :   " + PROP.get("BILLING_TERM") + "\n" +
                    "======================================================\n\n");

            //ExecutorService es = Executors.newFixedThreadPool(4);
            ExecutorService es = Executors.newCachedThreadPool();



            //while (!STOP) {
            while (true) {

                try{

                    Socket ClientConnected = AAPaymentGateway.accept();
                    String ip = String.valueOf(ClientConnected.getRemoteSocketAddress()).substring(1);

                    log.info("NEW CONNECTION FROM: " + ip);
                    String tName = "A " + ip;

                    try{
                        es.submit(new ClientHandleThread(ClientConnected,
                                (String) PROP.get("BILLING_IP"),
                                (int) PROP.get("BILLING_PORT"),
                                (String) PROP.get("BILLING_TERM"),
                                (int) PROP.get("BILLING_READ_TIMEOUT"),
                                tName));

                    } catch (Exception err){
                        log.error(ip + " THREAD START ERROR ", err);
                    }

                } catch (Exception err){
                    err.printStackTrace();
                }

            }


        } catch (IOException err) {
            log.error("CAN`T BIND ADDRESS OR PORT !!!", err);
        }

    }


}
