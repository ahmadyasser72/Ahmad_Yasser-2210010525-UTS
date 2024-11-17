/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ahmad.yasser.uts.aplikasi.keuangan.pribadi;

import ahmad.yasser.uts.aplikasi.keuangan.pribadi.database.Transactions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.AbstractTableModel;

// Kelas ini digunakan untuk model tabel transaksi
public class TransactionTableModel extends AbstractTableModel {

    // Menyimpan data transaksi
    private List<Transactions> records;

    // Nama kolom yang akan ditampilkan pada tabel
    private final String[] columnNames = new String[]{"Transaksi", "Akun", "Tanggal", "Nominal (Rp. )", "Keterangan"};

    // Konstruktor untuk inisialisasi records sebagai ArrayList kosong
    public TransactionTableModel() {
        this.records = new ArrayList<>();
    }

    // Set data transaksi dan urutkan berdasarkan tanggal dan tipe transaksi
    public void setRecords(List<Transactions> records) {
        this.records = records;

        // Urutkan berdasarkan tanggal, kemudian tipe transaksi
        Collections.sort(this.records,
                Comparator.comparing((Transactions t) -> t.date)
                        .thenComparing(t -> t.transactionType.getName())
        );

        // Method dibawah harus dipanggil agar table di GUI terupdate
        this.fireTableDataChanged();
    }

    // Mengambil entri transaksi berdasarkan baris
    public Transactions getEntry(int row) {
        return this.records.get(row);
    }

    // Mengembalikan jumlah baris pada tabel (jumlah transaksi)
    @Override
    public int getRowCount() {
        return this.records.size();
    }

    // Mengembalikan nama kolom berdasarkan indeks kolom
    @Override
    public String getColumnName(int col) {
        return this.columnNames[col];
    }

    // Mengembalikan tipe data untuk kolom tertentu
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    // Mengembalikan jumlah kolom pada tabel (jumlah nama kolom)
    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    // Mengambil nilai untuk cell tertentu berdasarkan baris dan kolom
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var entry = this.getEntry(rowIndex);

        // Mengembalikan nilai berdasarkan kolom yang diminta
        switch (columnIndex) {
            case 0 -> {
                return entry.transactionType.getName(); // Tipe transaksi
            }
            case 1 -> {
                return entry.account.getName(); // Nama akun
            }
            case 2 -> {
                return entry.date; // Tanggal transaksi
            }
            case 3 -> {
                return entry.amount; // Nominal transaksi
            }
            case 4 -> {
                return entry.note; // Keterangan transaksi
            }
            default -> {
                throw new AssertionError(); // Menangani kolom yang tidak dikenali
            }
        }
    }
}
