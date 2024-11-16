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
public enum TransactionType {

    INCOME(1, "Income"),
    EXPENSE(2, "Expense");

    private static final Map<Integer, TransactionType> lookup = new HashMap<>();

    static {
        // Mengisi lookup map untuk akses cepat berdasarkan ID
        for (TransactionType type : TransactionType.values()) {
            lookup.put(type.id, type);
        }
    }

    private final int id;
    private final String name;

    TransactionType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Mengambil ID tipe transaksi
    public int getId() {
        return id;
    }

    // Mengambil nama tipe transaksi
    public String getName() {
        return name;
    }

    // Method untuk mencari TransactionType berdasarkan ID
    public static TransactionType fromId(int id) {
        return lookup.get(id);
    }

    // Menginisialisasi tabel dan data enum di dalam tabel
    public static void initialize(Connection conn) throws SQLException {
        // Membuat tabel transaction_types jika belum ada
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS transaction_types (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL
            );
        """;
        try (var stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }

        // Menambahkan nilai enum jika belum ada di tabel
        String insertSQL = """
            INSERT OR IGNORE INTO transaction_types (id, name)
            VALUES (?, ?);
        """;

        try (var pstmt = conn.prepareStatement(insertSQL)) {
            for (TransactionType type : TransactionType.values()) {
                pstmt.setInt(1, type.getId());
                pstmt.setString(2, type.getName());
                pstmt.executeUpdate();
            }
        }
    }
}
