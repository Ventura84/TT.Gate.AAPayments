package main;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandleThread extends Thread{

    private static final Logger log = Logger.getLogger(ClientHandleThread.class);

    private String tName;
    private Socket ClientConnect;
    private String ip;
    private String billingIp;
    private int billingPort;
    private String billingTerminal;
    private int billingReadTimeout;
    //private String DB_IP;
//    private String verifyResult;
//    private String genResult;
//    private String payResult;
    private String answerFromPC;

    private InputStream fromClient;
    private OutputStream toClient;

    private byte[] bytesFromClient;
    private String[] parsedBodyS = new String[65];


    public ClientHandleThread(Socket clientConnected, String billingIp, int billingPort, String billingTerminal, int billingReadTimeout, String tName) {
        this.ip = String.valueOf(clientConnected.getRemoteSocketAddress()).substring(1);
        this.ClientConnect = clientConnected;
        this.billingIp = billingIp;
        this.billingPort = billingPort;
        this.billingTerminal = billingTerminal;
        this.billingReadTimeout = billingReadTimeout;
        //this.DB_IP = db_ip;
        this.tName = tName;

    }

    private boolean threadRun = true;
    private void threadStop(){
        threadRun = false;
    }

    private void close_conn(String ip){

        try {

            this.fromClient.close();
            this.toClient.close();
            this.ClientConnect.close();

        } catch (IOException err) {
            //e.printStackTrace();
            log.error(ip + " ERROR: CAN`T CLOSE SOCKETS OR STREAMS", err);
        }

    }


    private boolean getConnectionStreamsIsFail() {

        try {
            ClientConnect.setSoTimeout(2000);
            fromClient = ClientConnect.getInputStream();
            toClient = ClientConnect.getOutputStream();
        } catch (Exception err) {
            log.error(ip + " ERROR: CAN`T GET STREAMS FROM CLIENT !\n", err);
            close_conn(ip);
            return true;
        }

        return false;
    }

    private boolean getBytesFromIsFail (Reader Reader) {

        try {
            bytesFromClient = Reader.ReadFrom(fromClient, ip);
            //System.out.println(Arrays.toString(bytesFromCLIENT));
        } catch (Exception err) {
            log.error(ip + " ERROR: CAN`T GET BYTES FROM CLIENT !\n", err);
            close_conn(ip);
            return true;
        }

        return false;
    }

    private boolean getParsedBodyFromIsFail(Parser Parser) {

        try {
            ArrayList<ArrayList<Byte>> parsedBody = Parser.parseBODY_b(bytesFromClient);
            for (int i = 0; i < parsedBody.size(); i++) {
                if (parsedBody.get(i) != null) {
                    parsedBodyS[i] = Parser.byteArrayToString(Parser.arrayListToByteArray(parsedBody.get(i)));
                }
            }
            log.info(ip + " DATA FROM CLIENT PARSED");
            log.info(Parser.logFormatForFields(Parser.getFieldsForLog(parsedBody)));
        } catch (Exception err) {
            log.error(ip + " ERROR: CAN`T PARSE BYTES FROM CLIENT !\n", err);
            close_conn(ip);
            return true;
        }

        return false;
    }


    private void sendTo (Sender Sender, String mess) {

        if (!Sender.sendTo(toClient, mess.getBytes(), ip, "VERF", "CLIENT")) {
            close_conn(ip);
        }
    }


    @Override
    public void run() {


        Thread.currentThread().setName(tName);
        //String ip = String.valueOf(ClientConnect.getRemoteSocketAddress()).substring(1);

        Reader Reader = new Reader();
        Parser Parser = new Parser();
        Sender Sender = new Sender();
        BillingSender BillingSender = new BillingSender();



        while (threadRun) {


            if (getConnectionStreamsIsFail()) {
                break;
            }

            if (getBytesFromIsFail(Reader)) {
                break;
            }

            if (getParsedBodyFromIsFail(Parser)) {
                break;
            }

            if (parsedBodyS[1].equals("VERF")) {
                //System.out.println("VERIFICATION REQUEST...");

                try {
                    String verifyResult = BillingSender.sendRequest(ip, billingIp, billingPort, "VERIFY", billingTerminal, "2", parsedBodyS[4], String.valueOf(Integer.parseInt(parsedBodyS[3])), "0", "");
                    log.info(ip + " BILLING SAYS : " + parsedBodyS[4] + " VERIFY: " + verifyResult);
                    sendTo(Sender, verifyResult);
                    break;
                } catch (Exception e) {
                    sendTo(Sender, "BILLING VERIFY ERROR !");
                    log.error(ip + " ERROR : BILLING VERIFY ERROR ! ", e);
                    break;
                }


            }

            if (parsedBodyS[1].equals("GPAY")) {

                String genResult;
                String payResult;
                try {
                    genResult = BillingSender.sendRequest(ip, billingIp, billingPort, "GENERATE", "", "", "", "", "", "");
                    log.info(ip + " BILLING SAYS : " + parsedBodyS[4] + " GENERATE: " + genResult);
                } catch (Exception e) {
                    log.error(ip + " ERROR : BILLING GENERATE ERROR ! ", e);
                    break;
                }

                try {
                    payResult = BillingSender.sendRequest(ip, billingIp, billingPort, "PAY", billingTerminal, "2", parsedBodyS[4], String.valueOf(Integer.parseInt(parsedBodyS[3])), "0", genResult);
                    log.info(ip + " BILLING SAYS : " + parsedBodyS[4] + " +" + Integer.parseInt(parsedBodyS[3]) / 100.d + " PAY: " + genResult + " " + payResult + " ****************");
                    DBWorker DBWorker = new DBWorker();
                    DBWorker.paid(ip, parsedBodyS, genResult);
                } catch (Exception e) {
                    log.error(ip + " ERROR : BILLING PAY ERROR ! ", e);
                    break;
                }


            }


            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            close_conn(ip);
            threadStop();


        } // WHILE

    } // RUN



}
