package com.example;
import java.io.*;
import java.net.*;

public class DNSResolver {
    public static void main(String[] args){
        // Dirección del servidor DNS
        String dnsServerAddress = "10.2.1.10"; // Ejemplo: Servidor DNS de Google
        int dnsServerPort = 53; // Puerto DNS estándar
        String domain = "google.com"; // Dominio a resolver
        // Crear un buffer para almacenar los datos de la consulta DNS
        byte[] requestData = Requester.createDNSRequest(domain);

        try {
            // Crear un socket UDP
            DatagramSocket socket = new DatagramSocket();
            // Crear un paquete que contenga la solicitud DNS y su longitud
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(dnsServerAddress), dnsServerPort);
            // Enviar la solicitud al servidor DNS
            socket.send(requestPacket);
            System.out.println(new String(requestPacket.getData()));
            // Crear un buffer para almacenar la respuesta del servidor DNS
            byte[] responseData = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);

            // Esperar y recibir la respuesta del servidor DNS
            socket.receive(responsePacket);

            // Imprimir la respuesta
            System.out.println("\n\nReceived: " + responsePacket.getLength() + " bytes");
            for (int i = 0; i < responsePacket.getLength(); i++) {
                System.out.print(String.format("%s", responseData[i]) + " ");
            }

            // Cerrar el socket
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
