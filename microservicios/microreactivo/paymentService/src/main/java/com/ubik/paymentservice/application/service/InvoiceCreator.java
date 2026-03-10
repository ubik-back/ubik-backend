package com.ubik.paymentservice.application.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement.dto.ReservationDto;
import com.ubik.paymentservice.infrastructure.adapter.out.usermanagement.dto.UserProfileDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceCreator {

    public byte[] generateInvoice(Payment payment, ReservationDto reservation, UserProfileDto userProfile) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

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
    }
}
