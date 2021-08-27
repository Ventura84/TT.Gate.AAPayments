package main;

import org.apache.log4j.Logger;

import java.io.OutputStream;

public class Sender {

    //private static final Logger log = Logger.getLogger(Sender.class);
    private final Logger log = Logger.getLogger(Sender.class);


    public boolean sendTo(OutputStream messReceiver, byte[] mess, String ip, String mti, String to) {

        Parser Parser = new Parser();

        try {
            //messReceiver.write(mess, 0, mess.length);
            messReceiver.write(mess);
            messReceiver.flush();

            log.info(ip + " " + mti + " = " + mess.length + " BYTES >>>\n" + Parser.logFormat(Parser.bytesToHex(mess, "all")) + "\r\t\t\t\t\t\t\t\t\t\t\t\tTO " + to + " OK !\n");
            return true;
        } catch (Exception err){
            log.error(ip + " " + mti + " = " + mess.length + " BYTES >>>\n" + Parser.logFormat(Parser.bytesToHex(mess, "all")) + "\r\t\t\t\t\t\t\t\t\t\t\t\tCAN`T SEND TO " + to + " !", err);
            return false;

        }
    }
}
