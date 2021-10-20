package main;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Date;
import java.util.Locale;


public class DBWorker {

    //private static final Logger log = Logger.getLogger(DBWorker.class);
    private final Logger log = Logger.getLogger(DBWorker.class);


    private Connection connection;
    private Statement connStatement;


    private void postgreSQLConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ttgate", "postgres", "postgres");
            connStatement = connection.createStatement();
        } catch (SQLException e) {
            log.error("ERROR: UNABLE CONNECT TO DB ! :(" + e);
            System.err.println("ERROR: UNABLE CONNECT TO DB !" + e);
            //e.printStackTrace();
        }
    }

    private void closeConnections(){
        try {
            if(connStatement != null){connStatement.close();}
            if(connection != null){connection.close();}
        } catch (SQLException e) {
            log.error("ERROR: CLOSING DB CONNECTIONS ERROR ! :(");
            System.err.println("ERROR: CLOSING DB CONNECTIONS ERROR !");
        }
    }






    private void insertPaid(String[] fields, String billingRRN){

        Date date = new Date();
        String dateS = String.format("%tF", date);
        String timeS = String.format("%tT", date);

        String queryToPg = String.format(Locale.US, "INSERT INTO aa_operations (op_date, op_time, pan, amount, rrn, account, billing_rrn) VALUES ('%s', '%s', '%s', %.2f, '%s', '%s', '%s')", dateS, timeS, fields[2], Integer.parseInt(fields[3]) / 100.d, fields[6], fields[4], billingRRN);
        //System.out.println(queryToPg);
        //System.out.println(queryToPg2);
        try {
            connStatement.execute(queryToPg);
        } catch (SQLException e) {
            log.error("ERROR: INSERTING PAY RESULT !!!\n" + e);
            System.err.println("ERROR: INSERTING PAY RESULT !!!\n" + e);
            closeConnections();
        }
        closeConnections();

    }

    public void paid(String ip, String[] fields, String billingRRN){

        postgreSQLConnection();

        insertPaid(fields, billingRRN);

        log.info(ip + " OPERATION PAID SAVED TO DATABASE\n");


    }


    private String createTable(){

        return "CREATE TABLE IF NOT EXISTS aa_operations\n" +
                "(\n" +
                "  id bigserial NOT NULL,\n" +
                "  op_date date,\n" +
                "  op_time time without time zone,\n" +
                "  pan text,\n" +
                "  amount numeric,\n" +
                "  rrn text,\n" +
                "  account text,\n" +
                "  billing_rrn text,\n" +
                "  CONSTRAINT aa_operations_pkey PRIMARY KEY (id)\n" +
                ")";

    }


    public void createTablesOnStart() throws SQLException {

        postgreSQLConnection();
        connStatement.execute(createTable());
        closeConnections();

    }



    /*
    private void createTable(){


        String createTable = "CREATE TABLE IF NOT EXISTS aa_operations\n" +
                "(\n" +
                "  id bigserial NOT NULL,\n" +
                "  date date,\n" +
                "  time with out timezone,\n" +
                "  pan text,\n" +
                "  amount numeric,\n" +
                "  rrn text,\n" +
                "  account text,\n" +
                "  billing_rrn text,\n" +
                "  CONSTRAINT aa_operations_pkey PRIMARY KEY (id)\n" +
                ")";
        //System.out.println(createTableQueryToPG);
        try {
            connStatement.execute(createTable);
        } catch (SQLException e) {
            log.error("ERROR: CREATING TABLE !!!\n" + e);
            System.err.println("ERROR: CREATING TABLE !!!\n" + e);
        }
    }

    public void createTableOnStart(){

        postgreSQLConnection();
        createTable();

    }
    */




}
