package main;

import java.util.ArrayList;

public class Parser {

//    public String getClientNumber(final byte[] bytes){
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < 11; i++) {
//            sb.append((char) bytes[i]);
//        }
//        //System.out.println("NUMBER: " + sb);
//        return String.valueOf(sb);
//    }
//
//    public String getClientAmount(final byte[] bytes){
//
//        StringBuilder sb = new StringBuilder();
//
//        for (int i = 11; i < 23; i++) {
//            sb.append((char) bytes[i]);
//        }
//        //System.out.println("AMOUNT : " + sb);
//        return String.valueOf(sb);
//    }

    //GET BODY
    public ArrayList<Byte> getBODY_b(final byte[] bytes) {

        ArrayList<Byte> body = new ArrayList<>();
        for (int i = 16; i < bytes.length; i++) {
            body.add(bytes[i]);
        }
        return body;
    }

    // GET LEN
    public ArrayList<Byte> getLEN_b(final byte[] bytes) {

        ArrayList<Byte> al = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            al.add(bytes[i]);

        }
        return al;
    }


    // GET MTI
    public ArrayList<Byte> getMTI_b(final byte[] bytes) {

        ArrayList<Byte> al = new ArrayList<>();
        for (int i = 4; i < 8; i++) {
            al.add(bytes[i]);
        }
        return al;
    }


    // GET MAP
    public ArrayList<Byte> getMAP_b(final byte[] bytes) {

        ArrayList<Byte> bytesForMap = new ArrayList<>();
        ArrayList<Byte> al = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = 8; i < 16; i++) {
            bytesForMap.add(bytes[i]);
        }

        for (byte byte_ : bytesForMap) {
            sb.append(String.format("%8s", Integer.toBinaryString(byte_ & 0xFF)).replace(' ', '0'));
        }
        String str = String.valueOf(sb);
        byte[] bb = str.getBytes();

        for (byte b : bb) {
            al.add(b);
        }

        return al;
    }

    // GET FIELDS
    public ArrayList<Byte> getFIELDS_b(final ArrayList<Byte> messMAP) {

        ArrayList<Byte> messFIELDS = new ArrayList<>();
        for (int i = 0; i < messMAP.size(); i++) {
            if (messMAP.get(i) == 49) {
                messFIELDS.add((byte) (i + 1));
            }
        }

        return messFIELDS;
    }


    public byte[] arrayListToByteArray(ArrayList<Byte> in) {

        byte[] bytes = new byte[in.size()];
        for (int i = 0; i < bytes.length; i++) {
            if (in.get(i) != null) {
                bytes[i] = in.get(i);
            }
        }
        return bytes;
    }

    public String byteArrayToString(byte[] in) {

        StringBuilder sb = new StringBuilder();
        for (byte b : in) {
            sb.append((char) b);
        }
        return String.valueOf(sb);
    }



    // PARSING BODY
    public ArrayList<ArrayList<Byte>> parseBODY_b(final byte[] bytes) {

        ArrayList<ArrayList<Byte>> parsedBodyA = new ArrayList<>();
        for (int i = 0; i < 65; i++) {
            parsedBodyA.add(null);
        }

        parsedBodyA.set(0, getLEN_b(bytes));
        parsedBodyA.set(1, getMTI_b(bytes));
        //System.out.println(getFIELDS_b(getMAP_b(bytes)));

        ArrayList<Integer> fieldsLen = new ArrayList<>();
        for (int i = 0; i < 65; i++) {
            fieldsLen.add(0);
        }

        for (int i = 0; i < 65; i++) {
            switch (i) {
                case 2: {
                    fieldsLen.set(i, 16);
                    break;
                }
                case 3:
                case 6:{
                    fieldsLen.set(i, 12);
                    break;
                }
//                case 4: {
//                    fieldsLen.set(i, 11);
//                    break;
//                }
//                case 5: {
//                    fieldsLen.set(i, 3);
//                    break;
//                }


                //default : {fieldsLen.set(i, 0); break;}
            }
        }

        ArrayList<Byte> messFields = getFIELDS_b(getMAP_b(bytes));
        ArrayList<Byte> messBody = getBODY_b(bytes);
        //System.out.println(messFields);
        for (int field : messFields) {
            switch (field) {
                case 2: {
                    ArrayList<Byte> fieldBytes = new ArrayList<>();
                    for (int i = 0; i < fieldsLen.get(field); i++) {
                        fieldBytes.add(messBody.get(0));
                        messBody.remove(0);
                    }
                    parsedBodyA.set(2, fieldBytes);
                    //System.out.println(fieldBytes);
                    break;
                }
                case 3: {
                    ArrayList<Byte> fieldBytes = new ArrayList<>();
                    for (int i = 0; i < fieldsLen.get(field); i++) {
                        fieldBytes.add(messBody.get(0));
                        messBody.remove(0);
                    }
                    parsedBodyA.set(3, fieldBytes);
                    //System.out.println(fieldBytes);
                    break;
                }
                case 4: {
                    ArrayList<Byte> fLen = new ArrayList<>();
                    for (int i = 0; i < 2; i++) {
                        fLen.add(messBody.get(0));
                        messBody.remove(0);
                    }
                    ArrayList<Byte> fieldBytes = new ArrayList<>();
                    for (int i = 0; i < Integer.parseInt(byteArrayToString(arrayListToByteArray(fLen))); i++) {
                        fieldBytes.add(messBody.get(0));
                        messBody.remove(0);
                    }
                    parsedBodyA.set(4, fieldBytes);
                    //System.out.println(fieldBytes);
                    break;
                }
//                case 5: {
//                    ArrayList<Byte> fLen = new ArrayList<>();
//                    for (int i = 0; i < 2; i++) {
//                        fLen.add(messBody.get(0));
//                        messBody.remove(0);
//                    }
//                    ArrayList<Byte> fieldBytes = new ArrayList<>();
//                    for (int i = 0; i < Integer.parseInt(byteArrayToString(arrayListToByteArray(fLen))); i++) {
//                        fieldBytes.add(messBody.get(0));
//                        messBody.remove(0);
//                    }
//                    parsedBodyA.set(5, fieldBytes);
//                    //System.out.println(fieldBytes);
//                    break;
//                }
                case 6: {
                    ArrayList<Byte> fieldBytes = new ArrayList<>();
                    for (int i = 0; i < fieldsLen.get(field); i++) {
                        fieldBytes.add(messBody.get(0));
                        messBody.remove(0);
                    }
                    parsedBodyA.set(6, fieldBytes);
                    //System.out.println(fieldBytes);
                    break;
                }

            }
        }

        return parsedBodyA;

    }


    public String getFieldsForLog(final ArrayList<ArrayList<Byte>> parsedBODY) {

        String[] fieldsNames = new String[65];
        fieldsNames[2] = " [PAN]              ";
        fieldsNames[3] = " [AMOUNT]           ";
        fieldsNames[4] = " [MOBILE ACCOUNT]   ";
        fieldsNames[6] = " [RRN]              ";

        StringBuilder sb = new StringBuilder("\n");

        sb.append("         [LENGTH]            = ").append(byteArrayToString(arrayListToByteArray(parsedBODY.get(0)))).append("\n");
        sb.append("         [MTI]               = ").append(byteArrayToString(arrayListToByteArray(parsedBODY.get(1)))).append("\n");
        //sb.append("         [FIELDS]            = ").append(getFIELDS_b(getMAP_b(bytes))).append("\n");
        for (int i = 2; i < parsedBODY.size(); i++) {
            if (parsedBODY.get(i) != null) {
                sb.append("field ").append(i).append(" ").append(fieldsNames[i]).append(" = ").append(byteArrayToString(arrayListToByteArray(parsedBODY.get(i)))).append("\n");
            }
        }
        return String.valueOf(sb);

    }


    public String bytesToHex(final byte[] bytes, String block) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        String str = new String(hexChars);
        if (block.equals("all")) {
            str = str.replaceAll("(.{32})", "$1\n");
            str = str.replaceAll("(.{2})", "$1 ");
            str = "\n" + str;
        } else if (block.equals("emv")) {

            //System.out.println("EMV STRING : " + str);
            str = str.replaceAll("(.{2})", "$1 ");
        }

        //str = "\n" + str;
        //return str;
        return str.substring(0, str.length() - 1);
    }


    public String logFormat(final String string) {

        return string.replaceAll("\n", "\n\t\t\t\t\t\t\t\t\t\t\t\t");

    }

    public String logFormatForFields(final String string) {

        return string.replaceAll("\n", "\n\t\t\t\t\t\t\t\t");

    }





}
