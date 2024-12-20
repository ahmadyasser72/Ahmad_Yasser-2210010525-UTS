/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ahmad.yasser.uts.aplikasi.keuangan.pribadi;

import ahmad.yasser.uts.aplikasi.keuangan.pribadi.database.Account;
import ahmad.yasser.uts.aplikasi.keuangan.pribadi.database.TransactionType;
import ahmad.yasser.uts.aplikasi.keuangan.pribadi.database.Transactions;
import com.jthemedetecor.OsThemeDetector;
import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author x
 */
public class JFrameAplikasiKeuanganPribadi extends javax.swing.JFrame {

    // Detektor mode tema sistem (gelap/terang).
    // Digunakan jika aplikasi diatur untuk mengikuti tema OS (mode default).
    private static final OsThemeDetector SYSTEM_THEME = OsThemeDetector.getDetector();
    // Nama kelas tema gelap yang digunakan oleh aplikasi.
    private static final String DARK_THEME = com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme.class.getName();
    // Nama kelas tema terang yang digunakan oleh aplikasi.
    private static final String LIGHT_THEME = com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme.class.getName();
    // Flag untuk mencatat apakah tema awal aplikasi adalah tema gelap.
    private static final boolean INITIAL_SYSTEM_THEME_IS_DARK = SYSTEM_THEME.isDark();

    // List untuk menyimpan data transaksi
    private List<Transactions> records;

    // Model tabel untuk mengelola data transaksi yang ditampilkan di tabel UI
    TransactionTableModel tableModel = new TransactionTableModel();

    // Formatter untuk mengubah angka menjadi format mata uang lokal (IDR)
    final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.of("in", "ID"));

    // Formatter untuk mengubah tanggal menjadi format "yyyy-MM-dd"
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    // Utilitas tambahan untuk membantu berbagai operasi
    final Utilities utils;

    // Konstruktor untuk JFrame aplikasi keuangan pribadi
    // - Mengatur tema aplikasi berdasarkan preferensi sistem operasi (gelap atau terang)
    // - Mengatur tema aplikasi agar dapat berubah secara dinamis mengikuti tema OS
    // - Menginisialisasi komponen GUI
    // - Menentukan lokasi jendela agar muncul di tengah layar
    // - Membuat objek utilitas untuk mempermudah beberapa operasi umum
    // - Menginisialisasi koneksi ke database SQLite dan keluar jika terjadi kesalahan
    // - Mengisi data pada ComboBox dari database
    // - Menyinkronkan format tanggal sesuai dengan konfigurasi aplikasi
    // - Mengatur ulang formulir ke kondisi awal
    public JFrameAplikasiKeuanganPribadi() {
        initComponents(); // Menginisialisasi komponen GUI dari form

        this.setLocationRelativeTo(null); // Menentukan lokasi jendela di tengah layar
        this.utils = new Utilities(this); // Membuat instance utilitas untuk berbagai fungsi umum

        // Mengatur tema berdasarkan perubahan tema sistem operasi
        this.updateTheme(SYSTEM_THEME.isDark());
        SYSTEM_THEME.registerListener(isDark -> {
            var isUsingOSTheme = jRadioButtonMenuItemThemeOS.isSelected();
            // Update tema hanya bila tema system dipilih
            if (isUsingOSTheme) {
                updateTheme(isDark); // Memperbarui tema secara dinamis
            }
        });

        try {
            // Menginisialisasi koneksi ke database SQLite
            Transactions.initializeConnection();
        } catch (SQLException ex) {
            // Menampilkan pesan error dan keluar jika koneksi gagal
            utils.showErrorDialog(ex, "koneksi ke sqlite");
            System.exit(1); // Mengakhiri aplikasi jika terjadi kesalahan koneksi
        }

        this.populateComboBoxItems(); // Mengisi data pada ComboBox dari sumber yang relevan
        this.syncDateFormat(); // Menyinkronkan format tanggal
        this.resetForm(); // Mengatur ulang tampilan awal formulir
    }

    // Menyinkronkan format tanggal pada semua komponen pemilih tanggal.
    private void syncDateFormat() {
        var pattern = this.dateFormatter.toPattern();
        jDateChooserDate.setDateFormatString(pattern);
        jDateChooserDateAwalFilter.setDateFormatString(pattern);
        jDateChooserDateAkhirFilter.setDateFormatString(pattern);
    }

    // Mengisi ComboBox dengan data dari enum Account dan TransactionType
    private void populateComboBoxItems() {
        var accountModel = (DefaultComboBoxModel) jComboBoxAccount.getModel();
        var accountFilterModel = (DefaultComboBoxModel) jComboBoxAccountFilter.getModel();
        for (var value : Account.values()) {
            accountModel.addElement(value.getName()); // Tambahkan nama akun ke ComboBox akun
            accountFilterModel.addElement(value.getName()); // Tambahkan nama akun ke ComboBox filter akun
        }

        var transactionTypeModel = (DefaultComboBoxModel) jComboBoxTransactionType.getModel();
        var transactionTypeFilterModel = (DefaultComboBoxModel) jComboBoxTransactionTypeFilter.getModel();
        for (var value : TransactionType.values()) {
            transactionTypeModel.addElement(value.getName()); // Tambahkan jenis transaksi ke ComboBox jenis transaksi
            transactionTypeFilterModel.addElement(value.getName()); // Tambahkan jenis transaksi ke ComboBox filter transaksi
        }
    }

    // Mengambil semua data transaksi dari database dan menyimpannya dalam properti `records`
    private void populateRecords() {
        try {
            this.records = Transactions.getAllTransactions(); // Mengambil semua transaksi dari database
        } catch (SQLException ex) {
            utils.showErrorDialog(ex, "mendapatkan data transaksi"); // Menampilkan dialog error jika terjadi kesalahan
        }
    }

    // Memperbarui tampilan tabel berdasarkan filter akun, tipe transaksi, dan tanggal.
    private void updateTableView() {
        var filterAkun = (String) jComboBoxAccountFilter.getSelectedItem(); // Filter akun
        var filterTipeTransaksi = (String) jComboBoxTransactionTypeFilter.getSelectedItem(); // Filter tipe transaksi

        // Menyaring data berdasarkan filter yang dipilih
        var filteredRecords = this.records.stream()
                // Menyaring berdasarkan akun jika filter tidak "-"
                .filter(entry -> filterAkun.equals("-") || filterAkun.equals(entry.account.getName()))
                // Menyaring berdasarkan tipe transaksi jika filter tidak "-"
                .filter(entry -> filterTipeTransaksi.equals("-") || filterTipeTransaksi.equals(entry.transactionType.getName()))
                // Menyaring berdasarkan rentang tanggal yang dipilih
                .filter(entry -> {
                    try {
                        var date = utils.roundDateToMidnight(dateFormatter.parse(entry.date)); // Mengonversi tanggal ke midnight
                        var from = utils.roundDateToMidnight(jDateChooserDateAwalFilter.getDate()); // Tanggal awal filter
                        var to = utils.roundDateToMidnight(jDateChooserDateAkhirFilter.getDate()); // Tanggal akhir filter

                        // Memeriksa apakah tanggal berada dalam rentang yang valid
                        return (from == null || date.compareTo(from) >= 0)
                                && (to == null || date.compareTo(to) <= 0);
                    } catch (ParseException ex) {
                        return false; // Jika terjadi error parsing tanggal, data ini diabaikan
                    }
                })
                .collect(Collectors.toList()); // Mengumpulkan hasil filter menjadi daftar

        // Hitung total pemasukan dan pengeluaran
        double totalPemasukan = 0d, totalPengeluaran = 0d;
        for (var entry : filteredRecords) {
            switch (entry.transactionType) {
                case INCOME ->
                    totalPemasukan += entry.amount; // Tambahkan ke total pemasukan
                case EXPENSE ->
                    totalPengeluaran += entry.amount; // Tambahkan ke total pengeluaran
            }
        }

        // Tampilkan hasil ke label
        jLabelTotalPemasukan.setText(currencyFormatter.format(totalPemasukan));
        jLabelTotalPengeluaran.setText(currencyFormatter.format(totalPengeluaran));

        this.tableModel.setRecords(filteredRecords); // Update model tabel dengan data yang sudah difilter
        jTable1.getSelectionModel().clearSelection(); // Hapus seleksi di tabel
    }

    // Mengatur ulang formulir ke kondisi awal
    private void resetForm() {
        this.populateRecords(); // Muat ulang data transaksi
        this.updateTableView(); // Perbarui tampilan tabel
        this.resetControls(); // Reset kontrol form
    }

    // Mengatur ulang kontrol UI (tombol dan input)
    private void resetControls() {
        jButtonTambah.setEnabled(true); // Aktifkan tombol Tambah
        jButtonUpdate.setEnabled(false); // Nonaktifkan tombol Update
        jButtonHapus.setEnabled(false); // Nonaktifkan tombol Hapus
        utils.clearInput( // Kosongkan input form
                jDateChooserDate, jSpinnerAmount, jComboBoxAccount, jComboBoxTransactionType, jTextAreaNote
        );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTextFieldId = new javax.swing.JTextField();
        buttonGroupTema = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jDateChooserDate = new com.toedter.calendar.JDateChooser();
        jSpinnerAmount = new javax.swing.JSpinner();
        jComboBoxTransactionType = new javax.swing.JComboBox<>();
        jComboBoxAccount = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaNote = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jButtonTambah = new javax.swing.JButton();
        jButtonUpdate = new javax.swing.JButton();
        jButtonHapus = new javax.swing.JButton();
        jButtonReset = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxTransactionTypeFilter = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxAccountFilter = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jDateChooserDateAwalFilter = new com.toedter.calendar.JDateChooser();
        jLabel9 = new javax.swing.JLabel();
        jDateChooserDateAkhirFilter = new com.toedter.calendar.JDateChooser();
        jButtonGrafikHarian = new javax.swing.JButton();
        jButtonResetFilter = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jComboBoxPresetFilterTanggal = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabelTotalPemasukan = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabelTotalPengeluaran = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItemExportExcel = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItemImportExcel = new javax.swing.JMenuItem();
        jMenuItemKeluar = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        jRadioButtonMenuItemThemeOS = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemThemeLight = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemThemeDark = new javax.swing.JRadioButtonMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplikasi Keuangan Pribadi");
        setMinimumSize(new java.awt.Dimension(750, 375));

        jSplitPane1.setDividerLocation(320);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Form", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));
        jPanel1.setMinimumSize(new java.awt.Dimension(320, 250));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel1.setText("Tanggal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("Nominal (Rp. )");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Tipe Transaksi");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel4.setText("Pilih Akun");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jDateChooserDate, gridBagConstraints);

        jSpinnerAmount.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1000.0d));
        jSpinnerAmount.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jSpinnerAmount, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jComboBoxTransactionType, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jComboBoxAccount, gridBagConstraints);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 88));

        jTextAreaNote.setColumns(20);
        jTextAreaNote.setLineWrap(true);
        jTextAreaNote.setRows(5);
        jTextAreaNote.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextAreaNote);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel5.setText("Keterangan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(jLabel5, gridBagConstraints);

        jButtonTambah.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        jButtonTambah.setText("Tambah");
        jButtonTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTambahActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jButtonTambah, gridBagConstraints);

        jButtonUpdate.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        jButtonUpdate.setText("Update");
        jButtonUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jButtonUpdate, gridBagConstraints);

        jButtonHapus.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        jButtonHapus.setText("Hapus");
        jButtonHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHapusActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jButtonHapus, gridBagConstraints);

        jButtonReset.setText("Reset");
        jButtonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jButtonReset, gridBagConstraints);

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setPreferredSize(new java.awt.Dimension(500, 200));

        jTable1.setModel(tableModel);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.setShowGrid(true);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);
        jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filter", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));
        java.awt.GridBagLayout jPanel3Layout = new java.awt.GridBagLayout();
        jPanel3Layout.columnWidths = new int[] {0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0};
        jPanel3Layout.rowHeights = new int[] {0, 8, 0, 8, 0, 8, 0};
        jPanel3.setLayout(jPanel3Layout);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("Tipe Transaksi");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jLabel6, gridBagConstraints);

        jComboBoxTransactionTypeFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-" }));
        jComboBoxTransactionTypeFilter.setPreferredSize(new java.awt.Dimension(100, 24));
        jComboBoxTransactionTypeFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTransactionTypeFilterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jComboBoxTransactionTypeFilter, gridBagConstraints);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel7.setText("Akun");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jLabel7, gridBagConstraints);

        jComboBoxAccountFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-" }));
        jComboBoxAccountFilter.setPreferredSize(new java.awt.Dimension(100, 24));
        jComboBoxAccountFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxAccountFilterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jComboBoxAccountFilter, gridBagConstraints);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel8.setText("Tanggal Awal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jLabel8, gridBagConstraints);

        jDateChooserDateAwalFilter.setPreferredSize(new java.awt.Dimension(100, 24));
        jDateChooserDateAwalFilter.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jDateChooserDateAwalFilterPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jDateChooserDateAwalFilter, gridBagConstraints);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel9.setText("Tanggal Akhir");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jLabel9, gridBagConstraints);

        jDateChooserDateAkhirFilter.setPreferredSize(new java.awt.Dimension(100, 24));
        jDateChooserDateAkhirFilter.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jDateChooserDateAkhirFilterPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jDateChooserDateAkhirFilter, gridBagConstraints);

        jButtonGrafikHarian.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        jButtonGrafikHarian.setText("Tampilkan Grafik Harian");
        jButtonGrafikHarian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGrafikHarianActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanel3.add(jButtonGrafikHarian, gridBagConstraints);

        jButtonResetFilter.setText("Reset Filter");
        jButtonResetFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetFilterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanel3.add(jButtonResetFilter, gridBagConstraints);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel10.setText("Preset Filter Tanggal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jLabel10, gridBagConstraints);

        jComboBoxPresetFilterTanggal.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Minggu ini", "7 hari terakhir", "Bulan ini", "30 hari terakhir", "90 hari terakhir", "Tahun ini", "6 bulan terakhir", "12 bulan terakhir" }));
        jComboBoxPresetFilterTanggal.setSelectedIndex(-1);
        jComboBoxPresetFilterTanggal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxPresetFilterTanggalActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jComboBoxPresetFilterTanggal, gridBagConstraints);

        jPanel2.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel4.setLayout(new java.awt.GridLayout(1, 0));

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Total Pemasukan", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        jLabelTotalPemasukan.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabelTotalPemasukan.setText("Rp. 0");
        jPanel5.add(jLabelTotalPemasukan);

        jPanel4.add(jPanel5);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Total Pengeluaran", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        jLabelTotalPengeluaran.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabelTotalPengeluaran.setText("Rp. 0");
        jPanel6.add(jLabelTotalPengeluaran);

        jPanel4.add(jPanel6);

        jPanel2.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(jPanel2);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");

        jMenu2.setText("Export Database");

        jMenuItemExportExcel.setText("Excel (.XLSX)");
        jMenuItemExportExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportExcelActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemExportExcel);

        jMenu1.add(jMenu2);

        jMenu3.setText("Import Database");

        jMenuItemImportExcel.setText("Excel (.XLSX)");
        jMenuItemImportExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemImportExcelActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemImportExcel);

        jMenu1.add(jMenu3);

        jMenuItemKeluar.setText("Keluar");
        jMenuItemKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemKeluarActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemKeluar);

        jMenuBar1.add(jMenu1);

        jMenu4.setText("Pengaturan");

        jMenu5.setText("Tema");

        buttonGroupTema.add(jRadioButtonMenuItemThemeOS);
        jRadioButtonMenuItemThemeOS.setSelected(true);
        jRadioButtonMenuItemThemeOS.setText("Sistem");
        jRadioButtonMenuItemThemeOS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemThemeOSActionPerformed(evt);
            }
        });
        jMenu5.add(jRadioButtonMenuItemThemeOS);

        buttonGroupTema.add(jRadioButtonMenuItemThemeLight);
        jRadioButtonMenuItemThemeLight.setText("Light");
        jRadioButtonMenuItemThemeLight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemThemeLightActionPerformed(evt);
            }
        });
        jMenu5.add(jRadioButtonMenuItemThemeLight);

        buttonGroupTema.add(jRadioButtonMenuItemThemeDark);
        jRadioButtonMenuItemThemeDark.setText("Dark");
        jRadioButtonMenuItemThemeDark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemThemeDarkActionPerformed(evt);
            }
        });
        jMenu5.add(jRadioButtonMenuItemThemeDark);

        jMenu4.add(jMenu5);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTambahActionPerformed
        // Ambil transaksi dari input pengguna sebagai Optional
        var transaksiOpt = this.getTransactionFromInput();

        // Jika tidak ada transaksi valid, keluar dari metode
        if (transaksiOpt.isEmpty()) {
            return;
        }

        // Ambil transaksi dari Optional
        var transaksi = transaksiOpt.get();

        try {
            // Tambahkan transaksi ke database
            transaksi.insert();

            // Tampilkan pesan sukses dan reset formulir
            utils.showInformationDialog("data berhasil ditambah!");
            this.resetForm();
        } catch (SQLException ex) {
            // Tangani kesalahan SQL dengan dialog error
            utils.showErrorDialog(ex, "menambah data");
        }
    }//GEN-LAST:event_jButtonTambahActionPerformed

    private void jComboBoxTransactionTypeFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTransactionTypeFilterActionPerformed
        updateTableView();
        resetControls();
    }//GEN-LAST:event_jComboBoxTransactionTypeFilterActionPerformed

    private void jComboBoxAccountFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxAccountFilterActionPerformed
        updateTableView();
        resetControls();
    }//GEN-LAST:event_jComboBoxAccountFilterActionPerformed

    private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetActionPerformed
        resetForm();
    }//GEN-LAST:event_jButtonResetActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // Ambil baris yang diklik pada tabel
        var rowIndex = jTable1.rowAtPoint(evt.getPoint());

        // Jika tidak ada baris yang dipilih, keluar dari metode
        if (rowIndex < 0) {
            return;
        }

        // Ambil transaksi yang sesuai dengan baris yang dipilih
        var transaksi = tableModel.getEntry(rowIndex);

        try {
            // Isi form dengan data transaksi yang dipilih
            jTextFieldId.setText(transaksi.id.toString());  // Set ID transaksi
            jDateChooserDate.setDate(dateFormatter.parse(transaksi.date));  // Set tanggal transaksi
            jSpinnerAmount.setValue(transaksi.amount);  // Set jumlah transaksi
            jComboBoxTransactionType.setSelectedItem(transaksi.transactionType.getName());  // Set tipe transaksi
            jComboBoxAccount.setSelectedItem(transaksi.account.getName());  // Set akun transaksi
            jTextAreaNote.setText(transaksi.note);  // Set catatan transaksi
        } catch (ParseException ex) {
            // Tangani kesalahan format tanggal jika parsing gagal
            utils.showErrorDialog(ex, "memilih entry");
        }

        // Atur tombol sesuai dengan status transaksi yang dipilih
        jButtonTambah.setEnabled(false);  // Nonaktifkan tombol Tambah
        jButtonUpdate.setEnabled(true);   // Aktifkan tombol Update
        jButtonHapus.setEnabled(true);    // Aktifkan tombol Hapus
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButtonUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateActionPerformed
        // Ambil transaksi dari input pengguna sebagai Optional
        var transaksiOpt = this.getTransactionFromInput();

        // Jika tidak ada transaksi valid, keluar dari metode
        if (transaksiOpt.isEmpty()) {
            return;
        }

        // Ambil transaksi dari Optional
        var transaksi = transaksiOpt.get();

        try {
            // Perbarui transaksi
            transaksi.update();

            // Tampilkan pesan sukses dan reset formulir
            utils.showInformationDialog("data berhasil diupdate!");
            this.resetForm();
        } catch (SQLException ex) {
            // Tangani kesalahan SQL dengan dialog error
            utils.showErrorDialog(ex, "update data");
        }
    }//GEN-LAST:event_jButtonUpdateActionPerformed

    private void jButtonHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHapusActionPerformed
        // Ambil transaksi dari input pengguna sebagai Optional
        var transaksiOpt = this.getTransactionFromInput();

        // Jika tidak ada transaksi valid, keluar dari metode
        if (transaksiOpt.isEmpty()) {
            return;
        }

        // Ambil transaksi dari Optional
        var transaksi = transaksiOpt.get();

        try {
            // Tampilkan dialog konfirmasi sebelum menghapus transaksi
            if (utils.showConfirmDialog(
                    "hapus transaksi %s %s tanggal %s?".formatted(
                            transaksi.transactionType.getName(), // Tipe transaksi
                            transaksi.account.getName(), // Akun terkait
                            transaksi.date // Tanggal transaksi
                    )
            )) {
                // Hapus transaksi jika dikonfirmasi
                transaksi.delete();

                // Tampilkan pesan sukses dan reset formulir
                utils.showInformationDialog("data berhasil dihapus!");
                this.resetForm();
            }
        } catch (SQLException ex) {
            // Tangani kesalahan SQL dengan dialog error
            utils.showErrorDialog(ex, "hapus data");
        }

    }//GEN-LAST:event_jButtonHapusActionPerformed

    private void jDateChooserDateAwalFilterPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jDateChooserDateAwalFilterPropertyChange
        if (!evt.getPropertyName().equals("date")) {
            return; // Hanya merespon perubahan properti "date"
        }

        // Filter tanggal akhir tidak boleh kurang dari tanggal awal,
        // jadi set tanggal minimum untuk filter tanggal akhir
        // dengan tanggal yang dipilih di filter tanggal awal
        jDateChooserDateAkhirFilter.setMinSelectableDate(jDateChooserDateAwalFilter.getDate());
        updateTableView();
        resetControls();
    }//GEN-LAST:event_jDateChooserDateAwalFilterPropertyChange

    private void jDateChooserDateAkhirFilterPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jDateChooserDateAkhirFilterPropertyChange
        if (!evt.getPropertyName().equals("date")) {
            return; // Hanya merespon perubahan properti "date"
        }

        // Filter tanggal awal tidak boleh lebih dari tanggal akhir,
        // jadi set tanggal maksimum untuk filter tanggal awal
        // dengan tanggal yang dipilih di filter tanggal akhir
        jDateChooserDateAwalFilter.setMaxSelectableDate(jDateChooserDateAkhirFilter.getDate());
        updateTableView();
        resetControls();
    }//GEN-LAST:event_jDateChooserDateAkhirFilterPropertyChange

    private void jButtonGrafikHarianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGrafikHarianActionPerformed
        // Membuat objek JDialog yang menampilkan grafik transaksi
        var dialog = new JDialogTransactionChart(this);

        // Menentukan ukuran dialog
        dialog.setSize(800, 600);
        dialog.setResizable(false);

        // Menempatkan dialog agar muncul di tengah-tengah frame utama
        dialog.setLocationRelativeTo(this);

        // Menampilkan dialog agar pengguna bisa melihat dan berinteraksi dengan grafik
        dialog.setVisible(true);
    }//GEN-LAST:event_jButtonGrafikHarianActionPerformed

    private void jButtonResetFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetFilterActionPerformed
        jComboBoxTransactionTypeFilter.setSelectedIndex(0);
        jComboBoxAccountFilter.setSelectedIndex(0);
        jDateChooserDateAwalFilter.setDate(null);
        jDateChooserDateAkhirFilter.setDate(null);
        jComboBoxPresetFilterTanggal.setSelectedIndex(-1);
    }//GEN-LAST:event_jButtonResetFilterActionPerformed

    private void jMenuItemExportExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportExcelActionPerformed
        // Membuat JFileChooser untuk memilih lokasi dan nama file untuk ekspor
        var fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export File Excel");

        // Mengatur filter hanya untuk file Excel dengan ekstensi .xlsx
        fileChooser.setFileFilter(new FileNameExtensionFilter("File Excel (*.xlsx)", "xlsx"));

        // Menampilkan dialog untuk memilih lokasi penyimpanan
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            // Jika pengguna membatalkan, tampilkan informasi dan keluar dari metode
            utils.showInformationDialog("Export file excel dibatalkan!");
            return;
        }

        // Mendapatkan file yang dipilih oleh pengguna
        var file = fileChooser.getSelectedFile();

        // Menambahkan ekstensi .xlsx jika belum ada pada nama file
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");  // Menambahkan ekstensi jika perlu
        }

        try (var workbook = tableModel.toXLSX(dateFormatter); // Membuat workbook dari model tabel
                 var excelFile = new FileOutputStream(file)) {  // Membuka output stream ke file yang dipilih
            // Menulis workbook ke file
            workbook.write(excelFile);
            // Menampilkan dialog berhasil setelah file berhasil diekspor
            utils.showInformationDialog("Berhasil export file excel ke '%s' !".formatted(file.getPath()));
        } catch (IOException ex) {
            // Menangani error jika terjadi kesalahan saat menulis file
            utils.showErrorDialog(ex, "export file excel");
        }
    }//GEN-LAST:event_jMenuItemExportExcelActionPerformed

    private void jMenuItemKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemKeluarActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItemKeluarActionPerformed

    private void jMenuItemImportExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemImportExcelActionPerformed
        // Membuka dialog untuk memilih file Excel
        var fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import File Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter("File Excel (*.xlsx)", "xlsx"));

        // Jika pengguna membatalkan, proses dihentikan
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            utils.showInformationDialog("Import file excel dibatalkan!");
            return;
        }

        // Mendapatkan file yang dipilih
        var file = fileChooser.getSelectedFile();

        // Menambahkan ekstensi .xlsx jika belum ada
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        try (var workbook = WorkbookFactory.create(file)) {
            // Mengimpor data dari file Excel ke database
            Transactions.importXLSX(workbook);
            utils.showInformationDialog("Berhasil import file excel '%s'!".formatted(file.getPath()));
            this.resetForm();
        } catch (IOException | SQLException ex) {
            // Menangani error jika ada masalah saat import
            utils.showErrorDialog(ex, "import file excel");
        }
    }//GEN-LAST:event_jMenuItemImportExcelActionPerformed

    private void jRadioButtonMenuItemThemeOSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemThemeOSActionPerformed
        updateTheme(SYSTEM_THEME.isDark());
    }//GEN-LAST:event_jRadioButtonMenuItemThemeOSActionPerformed

    private void jRadioButtonMenuItemThemeDarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemThemeDarkActionPerformed
        updateTheme(true);
    }//GEN-LAST:event_jRadioButtonMenuItemThemeDarkActionPerformed

    private void jRadioButtonMenuItemThemeLightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemThemeLightActionPerformed
        updateTheme(false);
    }//GEN-LAST:event_jRadioButtonMenuItemThemeLightActionPerformed

    private void jComboBoxPresetFilterTanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxPresetFilterTanggalActionPerformed
        // Pastikan item pada ComboBox tidak null, jika null maka langsung keluar dari method
        if (jComboBoxPresetFilterTanggal.getSelectedItem() == null) {
            return;
        }

        // Inisialisasi objek Calendar untuk manipulasi tanggal
        var calendar = Calendar.getInstance();
        var today = calendar.getTime();

        // Set tanggal akhir pada filter default ke hari ini
        jDateChooserDateAkhirFilter.setDate(today);

        // Cek preset filter yang dipilih pada ComboBox
        switch (jComboBoxPresetFilterTanggal.getSelectedItem().toString()) {
            case "Minggu ini" ->
                // Set tanggal awal ke hari pertama dalam minggu ini sesuai dengan pengaturan lokal
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            case "7 hari terakhir" ->
                // Kurangi 6 hari dari hari ini untuk mencakup 7 hari terakhir
                calendar.add(Calendar.DAY_OF_YEAR, -6);
            case "Bulan ini" ->
                // Set tanggal awal ke hari pertama dalam bulan ini
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            case "30 hari terakhir" ->
                // Kurangi 29 hari dari hari ini untuk mencakup 30 hari terakhir
                calendar.add(Calendar.DAY_OF_YEAR, -29);
            case "90 hari terakhir" ->
                // Kurangi 89 hari dari hari ini untuk mencakup 90 hari terakhir
                calendar.add(Calendar.DAY_OF_YEAR, -89);
            case "Tahun ini" ->
                // Set tanggal awal ke hari pertama dalam tahun ini
                calendar.set(Calendar.DAY_OF_YEAR, 1);
            case "6 bulan terakhir" -> {
                // Kurangi 5 bulan dari bulan ini
                calendar.add(Calendar.MONTH, -5);
                // Set tanggal ke hari pertama dari bulan yang dihitung
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            }
            case "12 bulan terakhir" -> {
                // Kurangi 1 tahun dari tahun ini
                calendar.add(Calendar.YEAR, -1);
                // Tambahkan 1 hari untuk menghindari overlap
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        // Set tanggal awal pada filter sesuai hasil manipulasi kalender
        jDateChooserDateAwalFilter.setDate(calendar.getTime());
    }//GEN-LAST:event_jComboBoxPresetFilterTanggalActionPerformed

    private Optional<Transactions> getTransactionFromInput() {
        if (utils.validasiTidakKosong(jDateChooserDate, "transaksi")
                || utils.validasiTidakKosong(jComboBoxTransactionType, "tipe transaksi")
                || utils.validasiTidakKosong(jComboBoxAccount, "tipe akun")
                || utils.validasiTidakKosong(jTextAreaNote, "keterangan")) {
            return Optional.empty();
        }

        // Mendapatkan tanggal dari input dan format jadi string
        var date = jDateChooserDate.getDate();
        var dateString = dateFormatter.format(date);

        // Mendapatkan nominal dari input
        var amount = (Double) jSpinnerAmount.getValue();

        // Mendapatkan tipe transaksi dari combo box
        var transactionTypeId = jComboBoxTransactionType.getSelectedIndex() + 1;
        var transactionType = TransactionType.fromId(transactionTypeId);

        // Mendapatkan akun dari combo box
        var accountId = jComboBoxAccount.getSelectedIndex() + 1;
        var account = Account.fromId(accountId);

        // Mendapatkan keterangan dari text area
        var note = jTextAreaNote.getText().trim();

        // Mendapatkan id dari hidden text field
        var id = jTextFieldId.getText();

        // Membuat dan mengembalikan objek Transactions
        return Optional.of(
                new Transactions(
                        id.isEmpty() ? null : Integer.valueOf(id),
                        dateString,
                        amount,
                        transactionType,
                        account,
                        note)
        );
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // Set tema aplikasi awal sesuai dengan tema sistem saat aplikasi baru mulai
            UIManager.setLookAndFeel(INITIAL_SYSTEM_THEME_IS_DARK ? DARK_THEME : LIGHT_THEME);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            // Tampilkan error jika ada masalah dengan Look and Feel
            ex.printStackTrace();
        }

        /* Membuat dan menampilkan form */
        java.awt.EventQueue.invokeLater(() -> {
            new JFrameAplikasiKeuanganPribadi().setVisible(true);
        });
    }

    // Memperbarui tema aplikasi berdasarkan pilihan tema gelap atau terang.
    private void updateTheme(boolean isDark) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Mengatur tema sesuai dengan pilihan tema (gelap atau terang)
                UIManager.setLookAndFeel(isDark ? DARK_THEME : LIGHT_THEME);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
                // Menampilkan dialog error jika terjadi kesalahan dalam pengaturan tema
                utils.showErrorDialog(ex, "mengatur tema");
                return;
            }

            // Memperbarui tampilan komponen UI setelah perubahan tema
            SwingUtilities.updateComponentTreeUI(this);
            this.pack();

            // Memperbarui tema pada komponen JDateChooser
            // JDateChooser tidak otomatis mengikuti tema default Swing
            updateJDateChooserTheme(jDateChooserDate);
            updateJDateChooserTheme(jDateChooserDateAwalFilter);
            updateJDateChooserTheme(jDateChooserDateAkhirFilter);
        });
    }

    private void updateJDateChooserTheme(JDateChooser jDateChooser) {
        // Mengambil kalender dari JDateChooser untuk mengubah tema
        var jCalendar = jDateChooser.getJCalendar();

        // Jangan perbolehkan edit tanggal secara manual
        // karena bila diperbolehkan warna TextFieldnya akan selalu hitam
        jDateChooser.getDateEditor().setEnabled(false);

        // Jika tema gelap, atur warna kalender menjadi putih dan hari Minggu menjadi merah muda
        if (INITIAL_SYSTEM_THEME_IS_DARK) {
            jCalendar.setForeground(Color.WHITE); // Teks kalender berwarna putih
            jCalendar.setSundayForeground(Color.PINK); // Hari Minggu berwarna merah muda
        } else {
            // Jika tema terang, atur warna kalender menjadi hitam dan hari Minggu menjadi merah
            jCalendar.setForeground(Color.BLACK); // Teks kalender berwarna hitam
            jCalendar.setSundayForeground(Color.RED); // Hari Minggu berwarna merah
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupTema;
    private javax.swing.JButton jButtonGrafikHarian;
    private javax.swing.JButton jButtonHapus;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JButton jButtonResetFilter;
    private javax.swing.JButton jButtonTambah;
    private javax.swing.JButton jButtonUpdate;
    private javax.swing.JComboBox<String> jComboBoxAccount;
    private javax.swing.JComboBox<String> jComboBoxAccountFilter;
    private javax.swing.JComboBox<String> jComboBoxPresetFilterTanggal;
    private javax.swing.JComboBox<String> jComboBoxTransactionType;
    private javax.swing.JComboBox<String> jComboBoxTransactionTypeFilter;
    private com.toedter.calendar.JDateChooser jDateChooserDate;
    private com.toedter.calendar.JDateChooser jDateChooserDateAkhirFilter;
    private com.toedter.calendar.JDateChooser jDateChooserDateAwalFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelTotalPemasukan;
    private javax.swing.JLabel jLabelTotalPengeluaran;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemExportExcel;
    private javax.swing.JMenuItem jMenuItemImportExcel;
    private javax.swing.JMenuItem jMenuItemKeluar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemThemeDark;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemThemeLight;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemThemeOS;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinnerAmount;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextAreaNote;
    private javax.swing.JTextField jTextFieldId;
    // End of variables declaration//GEN-END:variables
}
