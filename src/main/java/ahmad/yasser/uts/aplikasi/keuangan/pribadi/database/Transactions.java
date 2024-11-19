/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ahmad.yasser.uts.aplikasi.keuangan.pribadi.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author x
 */
public class Transactions {

    // Variabel statis untuk nama database
    private static final String DATABASE_URL = "jdbc:sqlite:aplikasi-keuangan.db";  // Ganti path sesuai kebutuhan
    private static Connection conn;

    // Field untuk data transaksi
    public final Integer id;
    public final String date;
    public final double amount;
    public final TransactionType transactionType;
    public final Account account;
    public final String note;

    // Konstruktor untuk transaksi baru
    public Transactions(Integer id, String date, double amount, TransactionType transactionType, Account account, String note) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.transactionType = transactionType;
        this.account = account;
        this.note = note;
    }

    // Method untuk menginisialisasi koneksi dan pembuatan tabel
    public static void initializeConnection() throws SQLException {
        // Cek apakah koneksi sudah ada dan masih aktif
        if (conn != null && !conn.isClosed()) {
            return;  // Jika sudah aktif, langsung return tanpa menjalankan query SQL
        }

        // Jika belum ada koneksi atau koneksi sudah ditutup, buat koneksi baru
        conn = DriverManager.getConnection(DATABASE_URL);

        // Inisialisasi data enum untuk transaction_types dan accounts
        TransactionType.initialize(conn);
        Account.initialize(conn);

        // Membuat tabel transactions jika belum ada
        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                amount REAL NOT NULL,
                transaction_type_id INTEGER,
                account_id INTEGER,
                note TEXT,
                FOREIGN KEY (transaction_type_id) REFERENCES transaction_types(id),
                FOREIGN KEY (account_id) REFERENCES accounts(id)
            );
        """;

        // Eksekusi query untuk membuat tabel transactions setelah tabel referensi ada
        try (var stmt = conn.createStatement()) {
            stmt.execute(createTransactionsTable);
        }
    }

    // Method untuk menambah transaksi
    public void insert() throws SQLException {
        String insertTransactionSQL = """
            INSERT INTO transactions (date, amount, transaction_type_id, account_id, note)
            VALUES (?, ?, ?, ?, ?);
        """;

        try (var pstmt = conn.prepareStatement(insertTransactionSQL)) {
            pstmt.setString(1, this.date);
            pstmt.setDouble(2, this.amount);
            pstmt.setInt(3, this.transactionType.getId());
            pstmt.setInt(4, this.account.getId());
            pstmt.setString(5, this.note);
            pstmt.executeUpdate();
        }
    }

    // Method untuk mengupdate transaksi
    public void update() throws SQLException {
        String updateTransactionSQL = """
            UPDATE transactions
            SET date = ?, amount = ?, transaction_type_id = ?, account_id = ?, note = ?
            WHERE id = ?;
        """;

        try (var pstmt = conn.prepareStatement(updateTransactionSQL)) {
            pstmt.setString(1, this.date);
            pstmt.setDouble(2, this.amount);
            pstmt.setInt(3, this.transactionType.getId());
            pstmt.setInt(4, this.account.getId());
            pstmt.setString(5, this.note);
            pstmt.setInt(6, this.id);
            pstmt.executeUpdate();
        }
    }

    // Method untuk melakukan insert atau update data transaksi
    public void upsert() throws SQLException {
        String upsertTransactionSQL = """
            INSERT OR REPLACE INTO transactions (id, date, amount, transaction_type_id, account_id, note)
            VALUES (?, ?, ?, ?, ?, ?);
        """;

        try (var pstmt = conn.prepareStatement(upsertTransactionSQL)) {
            pstmt.setInt(1, this.id);
            pstmt.setString(2, this.date);
            pstmt.setDouble(3, this.amount);
            pstmt.setInt(4, this.transactionType.getId());
            pstmt.setInt(5, this.account.getId());
            pstmt.setString(6, this.note);
            pstmt.executeUpdate();
        }
    }

    // Method untuk menghapus transaksi
    public void delete() throws SQLException {
        String deleteTransactionSQL = "DELETE FROM transactions WHERE id = ?;";

        try (var pstmt = conn.prepareStatement(deleteTransactionSQL)) {
            pstmt.setInt(1, this.id);
            pstmt.executeUpdate();
        }
    }

    // Method untuk mendapatkan semua transaksi
    public static List<Transactions> getAllTransactions() throws SQLException {
        String getAllTransactionsSQL = "SELECT * FROM transactions;";
        List<Transactions> transactionsList = new ArrayList<>();

        try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(getAllTransactionsSQL)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                double amount = rs.getDouble("amount");
                int transactionTypeId = rs.getInt("transaction_type_id");
                int accountId = rs.getInt("account_id");
                String note = rs.getString("note");

                // Mendapatkan TransactionType dan Account dari ID
                TransactionType transactionType = TransactionType.fromId(transactionTypeId);
                Account account = Account.fromId(accountId);

                // Menambahkan transaksi ke dalam list
                transactionsList.add(new Transactions(id, date, amount, transactionType, account, note));
            }
        }

        return transactionsList;
    }

    // Mengimpor data transaksi dari file XLSX, lalu upsert ke database
    public static void importXLSX(Workbook workbook) throws SQLException {
        // Mengambil sheet "Pemasukan" dan "Pengeluaran" dari workbook
        var pemasukanSheet = workbook.getSheet("Pemasukan");
        var pengeluaranSheet = workbook.getSheet("Pengeluaran");

        // Jika salah satu sheet tidak ada, langsung keluar dari method
        if (pemasukanSheet == null || pengeluaranSheet == null) {
            return;
        }

        for (var entry : Stream.of(
                // Menggabungkan transaksi dari kedua sheet "Pemasukan" dan "Pengeluaran" dalam satu stream,
                getTransactionsFromSheet(pemasukanSheet, TransactionType.INCOME),
                getTransactionsFromSheet(pengeluaranSheet, TransactionType.EXPENSE))
                .flatMap(List::stream)
                .collect(Collectors.toList())) {
            // kemudian melakukan upsert pada setiap transaksi secara langsung.
            entry.upsert();
        }
    }

    // Mengambil data transaksi dari sheet dan mengubahnya menjadi objek transaksi
    private static List<Transactions> getTransactionsFromSheet(Sheet sheet, TransactionType transactionType) {
        // Daftar untuk menyimpan objek transaksi
        List<Transactions> transactionsList = new ArrayList<>();

        // Membaca baris pertama untuk memulai iterasi (baris pertama biasanya header, jadi diabaikan)
        var iterator = sheet.iterator();
        var row = iterator.next(); // Lewati header

        // Iterasi untuk setiap baris di sheet
        while (iterator.hasNext()) {
            row = iterator.next();

            // Ambil data dari setiap kolom dalam baris
            var id = (int) row.getCell(0).getNumericCellValue(); // Ambil ID transaksi dari kolom pertama
            var account = Account.fromName(row.getCell(1).getStringCellValue()); // Ambil akun transaksi dari kolom kedua
            var date = row.getCell(2).getStringCellValue(); // Ambil tanggal transaksi dari kolom ketiga
            var amount = row.getCell(3).getNumericCellValue(); // Ambil jumlah uang transaksi dari kolom keempat
            var note = row.getCell(4).getStringCellValue(); // Ambil catatan transaksi dari kolom kelima

            // Membuat objek transaksi dan menambahkannya ke dalam list
            transactionsList.add(new Transactions(id, date, amount, transactionType, account, note));
        }

        // Mengembalikan daftar transaksi yang sudah diproses
        return transactionsList;
    }
}
