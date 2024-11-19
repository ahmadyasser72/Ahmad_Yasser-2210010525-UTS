package ahmad.yasser.uts.aplikasi.keuangan.pribadi;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import java.awt.Color;
import java.text.ParseException;

// Kelas untuk menampilkan grafik transaksi dalam dialog JDialog
public class JDialogTransactionChart extends javax.swing.JDialog {

    // Referensi ke JFrame utama
    private final JFrameAplikasiKeuanganPribadi parent;

    // Konstruktor untuk inisialisasi dialog dan elemen grafik
    public JDialogTransactionChart(JFrameAplikasiKeuanganPribadi parent) {
        // Memanggil konstruktor superclass dengan judul dialog dan properti modal
        super(parent, "Chart Aplikasi Keuangan Pribadi", true);

        // Menyimpan referensi ke JFrame parent
        this.parent = parent;

        // Membuat dataset untuk grafik
        var dataset = createDataset();
        // Membuat grafik berdasarkan dataset
        var chart = createChart(dataset);

        // Membuat panel grafik dan menetapkannya sebagai konten dialog
        var chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

    // Metode untuk membuat grafik menggunakan JFreeChart
    private JFreeChart createChart(TimeSeriesCollection dataset) {
        // Membuat grafik garis waktu (time series chart)
        var chart = ChartFactory.createTimeSeriesChart(
                "Pemasukan dan Pengeluaran Harian", // Judul grafik
                "Tanggal", // Label sumbu X
                "Nominal (Rp. )", // Label sumbu Y
                dataset, // Dataset untuk grafik
                true, // Tampilkan legenda
                true, // Tampilkan tooltips
                false // URLs tidak diperlukan
        );

        // Mendapatkan plot (bagian visual) dari grafik
        var plot = (XYPlot) chart.getPlot();

        // Menyetel warna latar belakang dan garis kisi
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Menyesuaikan renderer untuk menggambar grafik
        var renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        // Menampilkan titik data pada grafik
        renderer.setDefaultShapesVisible(true);
        // Menampilkan garis penghubung antar titik
        renderer.setDefaultLinesVisible(true);

        // Menyetel warna seri data
        renderer.setSeriesPaint(0, Color.BLUE); // Seri 0 (Pemasukan) dengan warna biru
        renderer.setSeriesPaint(1, Color.RED); // Seri 1 (Pengeluaran) dengan warna merah

        return chart; // Mengembalikan grafik yang telah dikonfigurasi
    }

    // Metode untuk membuat dataset grafik berdasarkan data transaksi
    private TimeSeriesCollection createDataset() {
        // Membuat time series untuk pemasukan dan pengeluaran
        var income = new TimeSeries("Pemasukan");
        var expense = new TimeSeries("Pengeluaran");

        // Iterasi melalui semua baris dalam tabel model
        for (var idx = 0; idx < parent.tableModel.getRowCount(); idx += 1) {
            try {
                // Mendapatkan entri data dari tabel model
                var entry = parent.tableModel.getEntry(idx);
                // Mengubah tanggal dari string ke format yang dapat digunakan grafik
                var date = new Day(parent.dateFormatter.parse(entry.date));
                // Menambahkan data ke seri berdasarkan jenis transaksi
                switch (entry.transactionType) {
                    case INCOME -> // Jika tipe transaksi pemasukan
                        income.add(date, entry.amount);
                    case EXPENSE -> // Jika tipe transaksi pengeluaran
                        expense.add(date, entry.amount);
                }
            } catch (ParseException ex) {
                // Menangani kesalahan parsing tanggal
                parent.utils.showErrorDialog(ex, "mengisi dataset chart");
                this.dispose(); // Menutup dialog secara langsung, tidak bisa digunakan lagi
            }
        }

        // Membuat koleksi time series dan menambahkan seri pemasukan dan pengeluaran
        var dataset = new TimeSeriesCollection();
        dataset.addSeries(income);
        dataset.addSeries(expense);

        return dataset; // Mengembalikan dataset untuk grafik
    }
}
