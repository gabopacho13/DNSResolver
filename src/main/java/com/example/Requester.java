package com.example;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Requester {
    static byte[] createDNSRequest(String domain) {
        //Salida de datos
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        //Creación Header
        Random random = new Random();
        short ID = (short)random.nextInt(32767);
        //Flags
        String flags = "";
        flags += "0"; //QR (Query)
        flags += "0000"; //OpCode (Standard Query)
        flags += "0"; //AA (No Autoritative Answer)
        flags += "0"; //TC (Not truncated)
        flags += "1"; //RC (Recursive Query)
        flags += "0"; //RA (Recursion available decided by responding server)
        flags += "000"; //Z (Reserved for future use, must be 0 in all queries
        flags += "0000"; //RCode (For responses)
        short requestFlags = Short.parseShort(flags, 2);
        ByteBuffer flagsByteBuffer = ByteBuffer.allocate(2).putShort(requestFlags);
        byte[] flagsByteArray= flagsByteBuffer.array();
        //Counts
        short QDCount = 1; //Number of queries
        short ANCount = 0; //RR in response
        short NSCount = 0; //Name Server RR in AA response
        short ARCount = 0; //RR in additional records in response
        //Juntar Datos
        try {
            dataOutputStream.writeShort(ID);
            dataOutputStream.write(flagsByteArray);
            dataOutputStream.writeShort(QDCount);
            dataOutputStream.writeShort(ANCount);
            dataOutputStream.writeShort(NSCount);
            dataOutputStream.writeShort(ARCount);
            //Consulta transformada a bytes
            String[] domainParts = domain.split("\\.");
            for (String domainPart : domainParts) {
                byte[] domainBytes = domainPart.getBytes(StandardCharsets.UTF_8);
                dataOutputStream.writeByte(domainBytes.length);
                dataOutputStream.write(domainBytes);
            }
            dataOutputStream.writeByte(0); //Fin de consulta
            dataOutputStream.writeShort(1); //Tipo A
            dataOutputStream.writeShort(1); //Clase IN
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return byteArrayOutputStream.toByteArray();
    }
}
