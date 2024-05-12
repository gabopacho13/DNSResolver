package com.example;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Interpreter {
    static String interpretDNSAnswer(byte[] responseData){
        try{
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(responseData));
            short ID = dataInputStream.readShort();
            short flags = dataInputStream.readByte();//Primeros 8 bits de flags
            int QR = (flags & 0b10000000) >>> 7; //Tomar primer bit de flags
            int OPCode = (flags & 0b01111000) >>> 3; //Tomar bits 2-5 de flags
            int AA = (flags & 0b00000100) >>> 2; //Tomar bit 6 de flags
            int TC = (flags & 0b00000010) >>> 1; //Tomar bit 7 de flags
            int RD = (flags & 0b00000001); //Tomar bit 8 de flags
            System.out.println("QR "+QR);
            System.out.println("Opcode "+OPCode);
            System.out.println("AA "+AA);
            System.out.println("TC "+TC);
            System.out.println("RD "+RD);
            flags = dataInputStream.readByte();//Segundos 8 bits de flags
            int RA = (flags & 0b10000000) >>> 7; //Tomar primer bit de flags
            int Z = (flags & 0b01110000) >>> 4; //Tomar bits 2-4 de flags
            int RCode = (flags & 0b00001111); //Tomar bits 5-8 de flags
            System.out.println("RA "+RA);
            System.out.println("Z "+Z);
            System.out.println("RCode "+RCode);
            short QDCount = dataInputStream.readShort();
            short ANCount = dataInputStream.readShort();
            short NSCount = dataInputStream.readShort();
            short ARCount = dataInputStream.readShort();
            System.out.println("QDCount "+QDCount);
            System.out.println("ANCount "+ANCount);
            System.out.println("NSCount "+NSCount);
            System.out.println("ARCount "+ARCount);
            //Leer consulta
            StringBuilder QNameBuilder = new StringBuilder(); //Nombre de dominio
            byte length = dataInputStream.readByte();
            while(length != 0){
                byte[] domainBytes = new byte[length];
                dataInputStream.read(domainBytes);//Leer parte del nombre de dominio
                QNameBuilder.append(new String(domainBytes)).append(".");//Agregar al nombre de dominio
                length = dataInputStream.readByte();//Leer longitud de siguiente parte del nombre
            }
            QNameBuilder.deleteCharAt(QNameBuilder.length()-1);//Eliminar último punto
            String QName = QNameBuilder.toString();
            short QType = dataInputStream.readShort(); //Tipo de consulta
            short QClass = dataInputStream.readShort(); //Clase de consulta
            System.out.println("QName "+QName);
            System.out.println("QType "+QType);
            System.out.println("QClass "+QClass);
            //Leer respuesta
            byte firstByte = dataInputStream.readByte();
            int firstTwoBits = (firstByte & 0b11000000) >>> 6; //Tomar primeros 2 bits del byte (11 si es puntero, 00 si es label)
            ByteArrayOutputStream label = new ByteArrayOutputStream();
            Map<String, String> domainToIp = new HashMap<>();
            for(int i = 0; i < ANCount; i++) {
                if(firstTwoBits == 3) {
                    byte currentByte = dataInputStream.readByte();
                    boolean stop = false;
                    byte[] newArray = Arrays.copyOfRange(responseData, currentByte, responseData.length);
                    DataInputStream sectionDataInputStream = new DataInputStream(new ByteArrayInputStream(newArray));
                    ArrayList<Integer> RData = new ArrayList<>();
                    ArrayList<String> domains = new ArrayList<>();
                    while (!stop) {
                        byte nextByte = sectionDataInputStream.readByte();
                        if(nextByte != 0){
                            byte[] currentLabel = new byte[nextByte];
                            for (int j = 0; j < nextByte; j++) {
                                currentLabel[j] = sectionDataInputStream.readByte();
                            }
                            label.write(currentLabel);
                        }
                        else{
                            stop = true;
                            short type = dataInputStream.readShort();
                            short class_ = dataInputStream.readShort();
                            int TTL = dataInputStream.readInt();
                            short RDLength = dataInputStream.readShort();
                            for (int j = 0; j < RDLength; j++) {
                                int nx = dataInputStream.readByte() & 255;
                                RData.add(nx);
                            }
                            System.out.println("Type: "+type);
                            System.out.println("Class: "+class_);
                            System.out.println("TTL: "+TTL);
                            System.out.println("RDLength: "+RDLength);
                        }
                        domains.add(label.toString(StandardCharsets.UTF_8));
                        label.reset();
                    }

                    StringBuilder ip = new StringBuilder();
                    StringBuilder domainSb = new StringBuilder();
                    for(Integer ipPart : RData){
                        ip.append(ipPart).append(".");
                    }
                    ip.deleteCharAt(ip.length()-1);//Eliminar último punto
                    for(String domainPart : domains){
                        if(!domainPart.isEmpty()) {
                            domainSb.append(domainPart).append(".");
                        }
                    }
                    domainSb.deleteCharAt(domainSb.length()-1);//Eliminar último punto
                    System.out.println("Domain: "+domainSb.toString());
                    String domainString = domainSb.toString();
                    String ipString = ip.toString();
                    domainToIp.put(ipString, domainString);
                }
                else if(firstTwoBits == 0){
                    System.out.println("It's a label");
                }
                firstByte = dataInputStream.readByte();
                firstTwoBits = (firstByte & 0b11000000) >>> 6;
            }
            domainToIp.forEach((key, value) -> System.out.println(key + " : " + value));
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return "";
    }
}
