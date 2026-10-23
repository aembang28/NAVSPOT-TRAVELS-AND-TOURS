package com.example.travelandtour;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class PaymentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Button backButton = findViewById(R.id.backButton);
        TextView destinationSummary = findViewById(R.id.destinationSummary);
        TextView customerSummary = findViewById(R.id.customerSummary);
        TextView priceSummary = findViewById(R.id.priceSummary);
        EditText ticketQuantityInput = findViewById(R.id.ticketQuantityInput);
        Spinner paymentMethodSpinner = findViewById(R.id.paymentMethodSpinner);
        EditText accountNameInput = findViewById(R.id.accountNameInput);
        EditText accountNumberInput = findViewById(R.id.accountNumberInput);
        Button payButton = findViewById(R.id.payButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        String destination = getIntent().getStringExtra("destination");
        String location = getIntent().getStringExtra("location");
        String priceStr = getIntent().getStringExtra("price");

        if (name == null || name.trim().isEmpty()) name = "Traveler";
        if (email == null || email.trim().isEmpty()) email = "traveler@app.com";
        if (destination == null) destination = "Boracay Island";
        if (location == null) location = "Aklan";

        destinationSummary.setText("Destination: " + destination + " (" + location + ")");
        customerSummary.setText("Customer: " + name + " (" + email + ")");
        priceSummary.setText("Price per Ticket: ₱5,000.00 (Max 100 Limit)");

        String[] paymentMethods = {
                "BDO Unibank Online",
                "BPI (Bank of the Philippine Islands)",
                "Metrobank Online",
                "UnionBank Online",
                "Landbank Online",
                "RCBC Digital",
                "PNB Digital",
                "GCash E-Wallet",
                "Maya E-Wallet",
                "Credit / Debit Card (Visa / Mastercard)",
                "PayPal",
                "Cash Payment"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, paymentMethods);
        paymentMethodSpinner.setAdapter(adapter);

        paymentMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String method = paymentMethods[position];
                if (method.contains("Bank") || method.contains("BDO") || method.contains("BPI")
                        || method.contains("Metrobank") || method.contains("Landbank")
                        || method.contains("UnionBank") || method.contains("RCBC") || method.contains("PNB")) {
                    accountNameInput.setHint("Bank Account Holder Name");
                    accountNumberInput.setHint("Bank Account / Ref Number");
                } else if (method.contains("GCash") || method.contains("Maya")) {
                    accountNameInput.setHint("E-Wallet Account Name");
                    accountNumberInput.setHint("Mobile / Reference Number");
                } else if (method.contains("Card")) {
                    accountNameInput.setHint("Cardholder Name");
                    accountNumberInput.setHint("Card Number (16 Digits)");
                } else if (method.contains("PayPal")) {
                    accountNameInput.setHint("PayPal Account Name");
                    accountNumberInput.setHint("PayPal Email / Ref Number");
                } else {
                    accountNameInput.setHint("Payer Full Name");
                    accountNumberInput.setHint("Payment Reference Number");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final String finalName = name;
        final String finalEmail = email;
        final String finalDestination = destination;
        final String finalLocation = location;

        payButton.setOnClickListener(v -> {
            String quantityStr = ticketQuantityInput.getText().toString().trim();
            int ticketQuantity = 1;
            try {
                ticketQuantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                ticketQuantity = 1;
            }

            // Enforce 100 limit per booking
            if (ticketQuantity < 1 || ticketQuantity > 100) {
                Toast.makeText(this, "Booking limit is up to 100 tickets/guests per booking!", Toast.LENGTH_LONG).show();
                ticketQuantityInput.setError("Limit: 1 to 100 tickets");
                return;
            }

            String accountName = accountNameInput.getText().toString().trim();
            String accountNumber = accountNumberInput.getText().toString().trim();

            if (accountName.isEmpty() || accountNumber.isEmpty()) {
                Toast.makeText(this, "Please enter online bank/account payment details", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalPrice = ticketQuantity * 5000;
            String calculatedPrice = "₱" + String.format(Locale.US, "%,d", totalPrice) + ".00";

            String selectedMethod = paymentMethodSpinner.getSelectedItem().toString();
            String receiptId = "REC-" + (100000 + new Random().nextInt(900000));
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Save booking + payment record into SharedPreferences
            SharedPreferences prefs = getSharedPreferences("TouristApp", MODE_PRIVATE);
            String bookingKey = "booking_" + System.currentTimeMillis();
            String bookingValue = finalEmail + "|" + finalName + "|" + finalDestination + "|" + finalLocation + "|" + calculatedPrice + "|" + selectedMethod + "|" + receiptId + "|" + timeStamp + "|" + ticketQuantity;
            prefs.edit().putString(bookingKey, bookingValue).apply();

            // Display Receipt Dialog
            showReceiptDialog(receiptId, finalName, finalEmail, finalDestination, finalLocation, calculatedPrice, selectedMethod, timeStamp, ticketQuantity);
        });
    }

    private void showReceiptDialog(String receiptId, String name, String email, String destination, String location, String price, String paymentMethod, String timeStamp, int quantity) {
        StringBuilder receiptText = new StringBuilder();
        receiptText.append("🧾 OFFICIAL PAYMENT RECEIPT\n");
        receiptText.append("------------------------------------\n");
        receiptText.append("Receipt ID: ").append(receiptId).append("\n");
        receiptText.append("Date/Time: ").append(timeStamp).append("\n");
        receiptText.append("------------------------------------\n");
        receiptText.append("Customer: ").append(name).append("\n");
        receiptText.append("Email: ").append(email).append("\n");
        receiptText.append("Destination: ").append(destination).append(" (").append(location).append(")\n");
        receiptText.append("Tickets/Guests: ").append(quantity).append(" Person(s) [Max 100 Limit]\n");
        receiptText.append("Total Paid: ").append(price).append("\n");
        receiptText.append("Payment Channel: ").append(paymentMethod).append("\n");
        receiptText.append("------------------------------------\n");
        receiptText.append("STATUS: PAID & CONFIRMED ✅\n");
        receiptText.append("\nThank you for booking with NAVSPOT Travels and Tours!");

        new AlertDialog.Builder(this)
                .setTitle("Online Payment Confirmed!")
                .setMessage(receiptText.toString())
                .setCancelable(false)
                .setPositiveButton("DONE / RETURN TO DASHBOARD", (dialog, which) -> {
                    Intent intent = new Intent(PaymentActivity.this, DashboardActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("newBookingConfirmed", true);
                    intent.putExtra("receiptId", receiptId);
                    intent.putExtra("destination", destination);
                    intent.putExtra("price", price);
                    intent.putExtra("quantity", quantity);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}
