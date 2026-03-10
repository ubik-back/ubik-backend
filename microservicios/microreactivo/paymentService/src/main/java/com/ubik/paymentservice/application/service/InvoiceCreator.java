package com.ubik.paymentservice.application.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
<<<<<<< HEAD
import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement.dto.ReservationDto;
import com.ubik.paymentservice.infrastructure.adapter.out.usermanagement.dto.UserProfileDto;
=======
>>>>>>> Stripe-payment
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceCreator {

<<<<<<< HEAD
    public byte[] generateInvoice(Payment payment, ReservationDto reservation, UserProfileDto userProfile) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
=======
    public byte[] generateInvoice(
            String invoiceNumber,
            String customerName,
            String customerEmail,
            String customerPhone,
            String servicesDetail,
            double totalAmount
    ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
>>>>>>> Stripe-payment
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

<<<<<<< HEAD
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            Paragraph header = new Paragraph("Factura de Pago")
                    .setBold()
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            document.add(new Paragraph(""));
            document.add(new Paragraph("ID Factura: INV-" + payment.id() + "-" + System.currentTimeMillis()));
            document.add(new Paragraph("Fecha: " + LocalDateTime.now().format(formatter)));
            document.add(new Paragraph(""));

            document.add(new Paragraph("Datos del Cliente:").setBold().setFontSize(14));
            String fullName = (userProfile.firstName() != null ? userProfile.firstName() : "") + " " +
                              (userProfile.lastName() != null ? userProfile.lastName() : "");
            if (fullName.trim().isEmpty()) {
                fullName = userProfile.username();
            }
            document.add(new Paragraph("Nombre: " + fullName.trim()));
            document.add(new Paragraph("Email: " + userProfile.email()));
            if (userProfile.documentNumber() != null && !userProfile.documentNumber().isEmpty()) {
                document.add(new Paragraph("Documento: " + userProfile.documentNumber()));
            }
            if (userProfile.phone() != null && !userProfile.phone().isEmpty()) {
                document.add(new Paragraph("Teléfono: " + userProfile.phone()));
            }

            document.add(new Paragraph(""));
            document.add(new Paragraph("Detalle de Servicios:").setBold().setFontSize(14));
            document.add(new Paragraph("Reserva ID: " + reservation.id()));
            document.add(new Paragraph("Habitación ID: " + reservation.roomId()));
            document.add(new Paragraph("Entrada: " + reservation.checkInDate().format(formatter)));
            document.add(new Paragraph("Salida: " + reservation.checkOutDate().format(formatter)));

            document.add(new Paragraph(""));
            document.add(new Paragraph("Total: $" + (payment.amountCents() / 100.0) + " " + payment.currency().toUpperCase()).setBold().setFontSize(16));

            document.add(new Paragraph(""));
            document.add(new Paragraph("Gracias por su pago.").setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF invoice", e);
        }
=======
            // Título
            Paragraph title = new Paragraph("FACTURA DE COMPRA - UBIK")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18);
            document.add(title);

            // Fecha
            String dateFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            document.add(new Paragraph("Fecha: " + dateFormatted));
            
            // Número de Factura
            document.add(new Paragraph("Factura #: " + invoiceNumber).setBold());

            document.add(new Paragraph("\n"));

            // Datos del cliente
            document.add(new Paragraph("DATOS DEL CLIENTE").setBold());
            document.add(new Paragraph("Nombre: " + (customerName != null ? customerName : "N/A")));
            document.add(new Paragraph("Email: " + (customerEmail != null ? customerEmail : "N/A")));
            document.add(new Paragraph("Teléfono: " + (customerPhone != null ? customerPhone : "N/A")));

            document.add(new Paragraph("\n"));

            // Detalle de servicios
            document.add(new Paragraph("DETALLE DE SERVICIOS").setBold());
            document.add(new Paragraph(servicesDetail));

            document.add(new Paragraph("\n"));

            // Total
            Paragraph total = new Paragraph("TOTAL PAGADO: $" + String.format("%.2f", totalAmount) + " COP")
                    .setBold()
                    .setFontSize(14);
            document.add(total);

            // Agradecimiento
            document.add(new Paragraph("\n¡Gracias por utilizar UBIK!").setTextAlignment(TextAlignment.CENTER).setItalic());

            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generando la factura PDF", e);
        }

        return baos.toByteArray();
>>>>>>> Stripe-payment
    }
}
