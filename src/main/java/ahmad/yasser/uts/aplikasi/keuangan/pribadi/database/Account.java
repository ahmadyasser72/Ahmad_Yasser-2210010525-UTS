/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ahmad.yasser.uts.aplikasi.keuangan.pribadi.database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author x
 */
public enum Account {

    CASH(1, "Tunai"),
    BANK(2, "Bank"),
    E_WALLET(3, "E-Wallet");

    private static final Map<Integer, Account> lookup = new HashMap<>();
    private static final Map<String, Account> nameLookup = new HashMap<>();

    static {
        // Mengisi lookup map untuk akses cepat berdasarkan ID
        for (Account account : Account.values()) {
            lookup.put(account.id, account);
            nameLookup.put(account.name.toLowerCase(), account);
        }
    }

    private final int id;
    private final String name;

    Account(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Mengambil ID akun
    public int getId() {
        return id;
    }

    // Mengambil nama akun
    public String getName() {
        return name;
    }

    // Method untuk mencari Account berdasarkan ID
    public static Account fromId(int id) {
        return lookup.get(id);
    }

    // Method untuk mencari Account berdasarkan nama
    public static Account fromName(String name) {
        return nameLookup.get(name.toLowerCase()); // Mengabaikan huruf kapital
    }

    // Menginisialisasi tabel dan data enum di dalam tabel
    public static void initialize(Connection conn) throws SQLException {
        // Membuat tabel accounts jika belum ada
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS accounts (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL
            );
        """;
        try (var stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }

        // Menambahkan nilai enum jika belum ada di tabel
        String insertSQL = """
            INSERT OR IGNORE INTO accounts (id, name)
            VALUES (?, ?);
        """;

        try (var pstmt = conn.prepareStatement(insertSQL)) {
            for (Account account : Account.values()) {
                pstmt.setInt(1, account.getId());
                pstmt.setString(2, account.getName());
                pstmt.executeUpdate();
            }
        }
    }
}
