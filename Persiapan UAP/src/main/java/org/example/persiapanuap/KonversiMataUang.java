package org.example.persiapanuap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.*;
import org.json.JSONObject;

public class KonversiMataUang {
    private static Map<String, Double> currencyRates = new HashMap<>();
    private static final String API_KEY = "2a6b5074694a79662a731f5588e2381d";
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/";

    public static void main(String[] args) {
        initializeRates();

        JFrame frame = new JFrame("Konversi Mata Uang");
        frame.setSize(700, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        ImageIcon backgroundImage = new ImageIcon("C:\\Users\\thegr\\Desktop\\istockphoto-1389168857-612x612.jpg");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setLayout(new BorderLayout());
        frame.setContentPane(backgroundLabel);

        JPanel panelInput = new JPanel(new GridBagLayout());
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelInput.setOpaque(false);
        panelBottom.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblAmount = new JLabel("Masukkan jumlah uang:");
        lblAmount.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField txtAmount = new JTextField();
        txtAmount.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel lblFrom = new JLabel("Dari mata uang:");
        lblFrom.setFont(new Font("Arial", Font.BOLD, 14));
        JComboBox<String> cbFrom = new JComboBox<>(new String[]{"USD", "EUR", "IDR", "JPY"});
        cbFrom.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel lblTo = new JLabel("Ke mata uang:");
        lblTo.setFont(new Font("Arial", Font.BOLD, 14));
        JComboBox<String> cbTo = new JComboBox<>(new String[]{"USD", "EUR", "IDR", "JPY"});
        cbTo.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton btnConvert = new JButton("Konversi");
        btnConvert.setFont(new Font("Arial", Font.BOLD, 14));
        btnConvert.setBackground(new Color(72, 118, 255));
        btnConvert.setForeground(Color.WHITE);
        btnConvert.setFocusPainted(false);
        btnConvert.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnClearTable = new JButton("Hapus Tabel");
        btnClearTable.setFont(new Font("Arial", Font.BOLD, 14));
        btnClearTable.setBackground(new Color(255, 69, 0));
        btnClearTable.setForeground(Color.WHITE);
        btnClearTable.setFocusPainted(false);
        btnClearTable.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JTable resultTable = new JTable(new String[][]{}, new String[]{"Jumlah Uang", "Dari Mata Uang", "Ke Mata Uang", "Hasil"});
        resultTable.setFont(new Font("Arial", Font.PLAIN, 12));
        resultTable.setRowHeight(25);
        resultTable.setShowGrid(true);
        JScrollPane tableScrollPane = new JScrollPane(resultTable);
        tableScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelInput.add(lblAmount, gbc);
        gbc.gridx = 1;
        panelInput.add(txtAmount, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelInput.add(lblFrom, gbc);
        gbc.gridx = 1;
        panelInput.add(cbFrom, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelInput.add(lblTo, gbc);
        gbc.gridx = 1;
        panelInput.add(cbTo, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panelInput.add(btnConvert, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        panelInput.add(btnClearTable, gbc);

        panelBottom.add(tableScrollPane, BorderLayout.CENTER);

        frame.add(panelInput, BorderLayout.NORTH);
        frame.add(panelBottom, BorderLayout.CENTER);

        btnConvert.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(txtAmount.getText());
                String fromCurrency = (String) cbFrom.getSelectedItem();
                String toCurrency = (String) cbTo.getSelectedItem();

                if (fromCurrency.equals(toCurrency)) {
                    JOptionPane.showMessageDialog(frame, "Mata uang asal dan tujuan sama.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Double rate = getConversionRate(fromCurrency, toCurrency);
                if (rate == null) {
                    JOptionPane.showMessageDialog(frame, "Kurs tidak tersedia.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double result = amount * rate;
                String[][] tableData = {
                        {String.valueOf(amount), fromCurrency, toCurrency, String.format("%.2f", result)}
                };
                resultTable.setModel(new javax.swing.table.DefaultTableModel(
                        tableData,
                        new String[]{"Jumlah Uang", "Dari Mata Uang", "Ke Mata Uang", "Hasil"}
                ));

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Masukkan jumlah uang yang valid.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnClearTable.addActionListener(e -> {
            resultTable.setModel(new javax.swing.table.DefaultTableModel(
                    new Object[][]{},
                    new String[]{"Jumlah Uang", "Dari Mata Uang", "Ke Mata Uang", "Hasil"}
            ));
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Kurs");
        menu.setFont(new Font("Arial", Font.BOLD, 14));

        JMenuItem addRate = new JMenuItem("Tambah Kurs Baru");
        addRate.setFont(new Font("Arial", Font.PLAIN, 14));
        addRate.addActionListener(e -> {
            String from = JOptionPane.showInputDialog(frame, "Masukkan mata uang asal:");
            String to = JOptionPane.showInputDialog(frame, "Masukkan mata uang tujuan:");
            String rateStr = JOptionPane.showInputDialog(frame, "Masukkan nilai kurs:");

            try {
                double rate = Double.parseDouble(rateStr);
                currencyRates.put(from + "-" + to, rate);
                JOptionPane.showMessageDialog(frame, "Kurs berhasil ditambahkan.", "Info", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Nilai kurs harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JMenuItem viewRates = new JMenuItem("Lihat Semua Kurs");
        viewRates.setFont(new Font("Arial", Font.PLAIN, 14));
        viewRates.addActionListener(e -> {
            StringBuilder rates = new StringBuilder("Daftar Kurs:\n");
            for (Map.Entry<String, Double> entry : currencyRates.entrySet()) {
                rates.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
            }
            JOptionPane.showMessageDialog(frame, rates.toString(), "Kurs", JOptionPane.INFORMATION_MESSAGE);
        });

        menu.add(addRate);
        menu.add(viewRates);
        menuBar.add(menu);

        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    private static void initializeRates() {
        currencyRates.put("USD-IDR", 15000.0);
        currencyRates.put("EUR-IDR", 16500.0);
        currencyRates.put("IDR-USD", 0.000067);
        currencyRates.put("IDR-EUR", 0.000061);
        currencyRates.put("USD-EUR", 0.91);
        currencyRates.put("EUR-USD", 1.10);
    }

    private static Double getConversionRate(String from, String to) {
        try {
            return fetchExchangeRate(from, to);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal mengakses API: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private static Double fetchExchangeRate(String fromCurrency, String toCurrency) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(API_URL + fromCurrency + "?apikey=" + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseData = response.body().string();
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject rates = jsonObject.getJSONObject("rates");

            return rates.getDouble(toCurrency);
        }
    }
}
