package ahmad.yasser.uts.aplikasi.keuangan.pribadi;

import com.toedter.calendar.JDateChooser;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Utilities {

    final private Component parent;  // Komponen induk untuk dialog (misalnya window utama)
    final private boolean silent;    // Menentukan apakah dialog tampil atau tidak

    // Constructor dengan parameter parent dan silent
    public Utilities(Component parent, boolean silent) {
        this.parent = parent;
        this.silent = silent;
    }

    // Constructor dengan parameter parent, silent default false
    public Utilities(Component parent) {
        this(parent, false);
    }

    // Mengosongkan teks pada JTextField saat mendapatkan fokus
    public static void clearSaatFocus(FocusEvent evt) {
        var source = evt.getSource();
        if (source instanceof JTextField jTextField) {
            jTextField.setText("");  // Kosongkan teks JTextField
        }
    }

    // Mengosongkan input pada komponen-komponen form (text field, combo box, dll.)
    public void clearInput(Component... components) {
        for (var component : components) {
            switch (component) {
                case JTextField jTextField ->
                    jTextField.setText("");  // Kosongkan JTextField
                case JComboBox jComboBox ->
                    jComboBox.setSelectedIndex(-1);  // Setel JComboBox ke pilihan kosong
                case JSpinner jSpinner ->
                    jSpinner.setValue(0);  // Setel nilai JSpinner ke 0
                case JTextArea jTextArea ->
                    jTextArea.setText("");  // Kosongkan JTextArea
                case JDateChooser jDateChooser ->
                    jDateChooser.setDate(null);  // Kosongkan tanggal JDateChooser
                default -> {
                    // Jika komponen tidak dikenali, tampilkan pesan error
                    this.showErrorDialog("tidak bisa clear input component %s!".formatted(component.getClass().getName()));
                }
            }
        }
    }

    // Memvalidasi apakah input pada komponen tidak kosong
    public boolean validasiTidakKosong(Component component, String nama) {
        switch (component) {
            case JTextField jTextField -> {
                if (jTextField.getText().isBlank()) {
                    this.showErrorDialog("inputan nilai %s tidak boleh kosong!".formatted(nama));
                    component.requestFocusInWindow();  // Fokuskan ke komponen
                    return true;
                }
            }
            case JComboBox jComboBox -> {
                var item = jComboBox.getSelectedItem();
                if (item == null) {
                    this.showErrorDialog("inputan pilihan %s belum dipilih!".formatted(nama));
                    component.requestFocusInWindow();
                    return true;
                }
            }
            case JTextArea jTextArea -> {
                if (jTextArea.getText().isBlank()) {
                    this.showErrorDialog("inputan nilai %s tidak boleh kosong!".formatted(nama));
                    component.requestFocusInWindow();
                    return true;
                }
            }
            case JDateChooser jDateChooser -> {
                var date = jDateChooser.getDate();
                if (date == null) {
                    this.showErrorDialog("inputan tanggal %s tidak boleh kosong!".formatted(nama));
                    component.requestFocusInWindow();
                    return true;
                }
            }
            default -> {
                // Jika komponen tidak dikenali untuk validasi
                this.showErrorDialog("tidak bisa validasi tidak kosong component '%s' !".formatted(component.getClass().getName()));
                return true;
            }
        }

        return false;  // Jika tidak ada yang kosong, kembalikan false (valid)
    }

    // Validasi untuk memastikan input hanya angka
    public boolean validasiInputHanyaAngka(KeyEvent event) {
        char c = event.getKeyChar();
        if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
            this.showErrorDialog("inputan ini hanya boleh input angka!");  // Tampilkan pesan error
            event.consume();  // Batalkan input yang tidak valid
            return true;
        }

        return false;  // Input valid jika hanya angka
    }

    // Menampilkan dialog error
    public void showErrorDialog(String pesan) {
        if (!this.silent) {
            JOptionPane.showMessageDialog(parent, pesan, "error", JOptionPane.ERROR_MESSAGE);  // Tampilkan dialog error
        }
    }

    // Menampilkan dialog error dengan exception sebagai pesan
    public void showErrorDialog(Exception e, String nama) {
        this.showErrorDialog("gagal %s! %s".formatted(nama, e.getMessage()));
    }

    // Menampilkan dialog informasi
    public void showInformationDialog(String pesan) {
        if (!this.silent) {
            JOptionPane.showMessageDialog(parent, pesan, "info", JOptionPane.INFORMATION_MESSAGE);  // Tampilkan pesan informasi
        }
    }

    // Menampilkan input dialog dan mengembalikan input pengguna
    public String showInputDialog(String pesan) {
        if (!this.silent) {
            return JOptionPane.showInputDialog(parent, pesan);  // Tampilkan input dialog
        }

        return "";  // Jika silent, kembalikan string kosong
    }

    // Menampilkan dialog konfirmasi (Yes/No)
    public boolean showConfirmDialog(String pesan) {
        if (!this.silent) {
            return JOptionPane.showConfirmDialog(parent, pesan, "confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;  // Tampilkan konfirmasi
        }

        return true;  // Jika silent, anggap Ya
    }
}
