package com.example;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DNSResolverBD {
    private static final Map<String, InetAddress> masterFile = new HashMap<>();

    static {
        readMasterFileFromDB();
    }

    private static void readMasterFileFromDB() {
        try (Connection connection = DriverManager.getConnection(ConnectorBase.getDBUrl(), ConnectorBase.getDBUser(), ConnectorBase.getDBPassword())) {
            String sql = "SELECT domain, ip_address FROM master_file";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String domain = resultSet.getString("domain");
                        String ipAddressStr = resultSet.getString("ip_address");
                        InetAddress ipAddress = InetAddress.getByName(ipAddressStr);
                        masterFile.put(domain, ipAddress);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] createDNSResponse(short transactionID, InetAddress ipAddress) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            dos.writeShort(transactionID); // Transaction ID
            dos.writeShort((short) 0x8180); // Standard response flags
            dos.writeShort(1); // QDCOUNT = 1
            dos.writeShort(1); // ANCOUNT = 1
            dos.writeShort(0); // NSCOUNT = 0
            dos.writeShort(0); // ARCOUNT = 0

            // Question Section
            dos.writeByte(0); // Null label for the root
            dos.writeShort(1); // QTYPE = A (Host Address)
            dos.writeShort(1); // QCLASS = IN (Internet)

            // Answer Section
            dos.writeByte(0xc0); // Pointer to the root (0xc0 0x0c)
            dos.writeByte(0x0c);
            dos.writeShort(1); // TYPE = A (Host Address)
            dos.writeShort(1); // CLASS = IN (Internet)
            dos.writeInt(3600); // TTL = 1 hour
            byte[] ipBytes = ipAddress.getAddress();
            dos.writeShort(ipBytes.length); // RDLENGTH
            dos.write(ipBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
