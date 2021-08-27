package main;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.Arrays;

public class Reader {

    private final Logger log = Logger.getLogger(Reader.class);

    public byte[] ReadFrom(InputStream fromStream, String ip) throws Exception {

        int bytesCount;
        byte[] bytesFromStream = new byte[128];
        log.info(ip + " RECEIVING FROM CLIENT... ");

        try {
            bytesCount = fromStream.read(bytesFromStream);

            if (bytesCount > 0) {
                byte[] bytesFromStream_trimmed = new byte[bytesCount];
                System.arraycopy(bytesFromStream, 0, bytesFromStream_trimmed, 0, bytesCount);
                log.info(ip + " " + bytesFromStream_trimmed.length + " BYTES FROM CLIENT RECEIVED");
                return bytesFromStream_trimmed;
            } else {
                log.error(ip + " ERROR: READ SOURCE ( CLIENT ) HAS LINK DROP DOWN OR READ ERROR !");
                throw new Exception("RETURNED " + bytesCount);
            }

        } catch (Exception err){
            throw new Exception(err.getMessage() + "\n" + Arrays.toString(err.getStackTrace()).replaceAll(",", "\n") + "\n");
        }

    }

}
