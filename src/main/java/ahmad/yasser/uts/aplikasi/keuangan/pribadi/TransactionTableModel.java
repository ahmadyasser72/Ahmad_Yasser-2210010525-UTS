/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ahmad.yasser.uts.aplikasi.keuangan.pribadi;

import ahmad.yasser.uts.aplikasi.keuangan.pribadi.database.Transactions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

    public Workbook toXLSX(SimpleDateFormat dateFormatter) {
        // Membuat workbook baru dengan format XLSX
        var workbook = new XSSFWorkbook();

        // Membuat dua sheet: Pemasukan dan Pengeluaran
        var pemasukanSheet = workbook.createSheet("Pemasukan");
        var pengeluaranSheet = workbook.createSheet("Pengeluaran");

        // Menyiapkan format untuk kolom tanggal
        var dateCellStyle = workbook.createCellStyle();
        var creationHelper = workbook.getCreationHelper();
        dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(dateFormatter.toPattern()));

        // Variabel untuk melacak baris terakhir di masing-masing sheet
        var pemasukanLastRow = 0;
        var pengeluaranLastRow = 0;

        // Looping melalui setiap transaksi dalam daftar records
        for (var entry : this.records) {
            XSSFRow row;

            // Menentukan sheet yang digunakan berdasarkan jenis transaksi
            switch (entry.transactionType) {
                case INCOME ->
                    row = pemasukanSheet.createRow(++pemasukanLastRow);  // Untuk transaksi pemasukan
                case EXPENSE ->
                    row = pengeluaranSheet.createRow(++pengeluaranLastRow);  // Untuk transaksi pengeluaran
                default -> {
                    continue;  // Jika jenis transaksi tidak dikenal, lanjutkan ke iterasi berikutnya
                }
            }

            // Menambahkan data ke dalam baris: ID, Akun, Tanggal, Nominal, dan Keterangan
            row.createCell(0).setCellValue(entry.id);
            row.createCell(1).setCellValue(entry.account.getName());

            var dateCell = row.createCell(2);
            dateCell.setCellValue(entry.date);
            dateCell.setCellStyle(dateCellStyle);  // Mengatur format tanggal

            row.createCell(3).setCellValue(entry.amount);
            row.createCell(4).setCellValue(entry.note);
        }

        // Menyiapkan style untuk header
        var headerStyle = workbook.createCellStyle();
        var font = workbook.createFont();
        font.setBold(true);  // Membuat font header menjadi tebal
        headerStyle.setFont(font);

        // Menambahkan header ke masing-masing sheet
        var headers = new String[]{"ID", "Akun", "Tanggal", "Nominal (Rp. )", "Keterangan"};
        for (var sheet : new XSSFSheet[]{pemasukanSheet, pengeluaranSheet}) {
            var headerRow = sheet.createRow(0);
            for (var i = 0; i < headers.length; i += 1) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);  // Mengaplikasikan style pada header
                sheet.autoSizeColumn(i);  // Menyesuaikan lebar kolom otomatis
            }
        }

        // Mengembalikan workbook yang telah terisi data transaksi
        return workbook;
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
