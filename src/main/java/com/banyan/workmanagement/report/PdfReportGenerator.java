package com.banyan.workmanagement.report;

import com.banyan.workmanagement.model.WorkDetails;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;

public class PdfReportGenerator {

    public static byte[] generateReport(WorkDetails workDetails) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 50, 50, 70, 50); // margins
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            writer.setPageEvent(new FooterPageEvent());

            document.open();

            // --- Add Title ---
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Work Detail Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30f);
            document.add(title);

            // --- Create Table ---
            float[] columnWidths = {2, 5};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // --- Add Table Content ---
            table.addCell(createHeaderCell("Engineer Name"));
            table.addCell(createValueCell(workDetails.getEngineerName()));

            table.addCell(createHeaderCell("Customer Name"));
            table.addCell(createValueCell(workDetails.getCustomerName()));

            table.addCell(createHeaderCell("Location"));
            table.addCell(createValueCell(workDetails.getLocation()));

            table.addCell(createHeaderCell("Date"));
            table.addCell(createValueCell(workDetails.getDate().toString()));

            table.addCell(createHeaderCell("Work Type"));
            table.addCell(createValueCell(workDetails.getTypeOfWork()));

            table.addCell(createHeaderCell("Status"));
            table.addCell(createValueCell(workDetails.getStatus()));

            table.addCell(createHeaderCell("Bill Amount"));
            table.addCell(createValueCell(String.valueOf(workDetails.getBillAmount())));

            table.addCell(createHeaderCell("Payment Mode"));
            table.addCell(createHeaderCell(workDetails.getPaymentMode()));

            table.addCell(createHeaderCell("Description"));
            table.addCell(createValueCell(workDetails.getWorkDescription()));

            document.add(table);

            document.close();
            writer.close();

            System.out.println("PDF report created successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    // === Helper method to create a header cell ===
    private static PdfPCell createHeaderCell(String text) {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(BaseColor.GRAY);
        cell.setPadding(8);
        return cell;
    }

    // === Helper method to create a normal value cell ===
    private static PdfPCell createValueCell(String text) {
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
        cell.setPadding(8);
        return cell;
    }

    // === Footer Event for Page Number ===
    static class FooterPageEvent extends PdfPageEventHelper {
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase("Page " + document.getPageNumber(), footerFont);

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }
}
