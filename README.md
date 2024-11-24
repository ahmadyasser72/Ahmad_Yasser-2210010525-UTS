# Aplikasi Keuangan Pribadi

> [!NOTE]
> Video demo: https://youtu.be/nTN96uBromk

Aplikasi desktop sederhana untuk mengelola keuangan pribadi, dikembangkan dengan Java Swing dan SQLite sebagai basis data. 🌟

## Fitur Utama
- [x] Menambah, mengubah, dan menghapus transaksi.
- [x] Kategori transaksi: **Pemasukan** dan **Pengeluaran**.
- [x] Filter transaksi berdasarkan:
  - Tanggal
  - Jenis transaksi
  - Akun keuangan.
- [x] Tampilan total pemasukan dan pengeluaran.
- [x] Ekspor dan impor data ke/dari file Excel (.xlsx).
- [x] Grafik transaksi harian.
- [x] Tema terang dan gelap yang otomatis menyesuaikan dengan sistem operasi.

## Cara Menggunakan
1. Clone repository ini ke perangkatmu.
2. Buka project ini di IDE yang mendukung Maven (NetBeans, IntelliJ IDEA, dsb.).
3. Jalankan perintah `mvn clean install` untuk memastikan semua dependency terunduh.
4. Pastikan perangkatmu memiliki Java Development Kit (JDK) 23 atau lebih baru.
5. Jalankan aplikasi melalui file `JFrameAplikasiKeuanganPribadi.java`.

## Teknologi yang Digunakan
- **Java Swing** untuk antarmuka pengguna.
- **SQLite** untuk penyimpanan data.
- **Apache POI** untuk ekspor dan impor file Excel.
- **FlatLaf** untuk tema gelap dan terang.
- **Maven** untuk manajemen dependensi dan build.

## Dependensi Maven
Dependensi utama yang digunakan:
- **SQLite JDBC**: Untuk koneksi ke SQLite.
- **JCalendar**: Untuk komponen pemilih tanggal.
- **JFreeChart**: Untuk menampilkan grafik transaksi harian.
- **Apache POI**: Untuk manipulasi file Excel.
- **FlatLaf**: Untuk tampilan tema modern.
- **jSystemThemeDetector**: Untuk mendeteksi tema sistem operasi secara otomatis.
