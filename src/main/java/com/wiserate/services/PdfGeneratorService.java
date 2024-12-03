package com.wiserate.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.wiserate.dto.loan.AmortizationPaymentDTO;
import com.wiserate.dto.loan.LoanResponseData;
import com.wiserate.enums.Alignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class PdfGeneratorService {

    private void addTableHeader(PdfPTable table) {
        Stream.of("Year", "Total Paid", "Principal Paid", "Interest Paid", "Remaining Balance")
                .forEach((title) -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(title));
                    header.setNoWrap(true);
                    header.setPadding(5);
                    table.addCell(header);
                });
    }

    public void configureTable(PdfPTable table) {
        table.setWidthPercentage(100); // Adjust the table to occupy the full width of the page
        table.setHorizontalAlignment(Element.ALIGN_CENTER); // Center-align the table
        try {
            table.setWidths(new float[]{1.5f, 2f, 2f, 2f, 2.5f});
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private PdfPCell createCustomCell(String text){
        return createCustomCell(text, 1, Alignment.LEFT);
    }

    private PdfPCell createCustomCell(String text, int borderWidth, Alignment alignment){
        int align = getAlignment(alignment);
        PdfPCell cell = new PdfPCell();
        cell.setPhrase(new Phrase(text));
        cell.setPaddingTop(5);
        cell.setPaddingBottom(5);
        cell.setPaddingLeft(4);
        cell.setPaddingRight(4);
        // BORDER
        cell.setBorderWidth(borderWidth);
        cell.setHorizontalAlignment(align);
        cell.setNoWrap(true);
        return cell;
    }

    private void addRows(PdfPTable table, List<AmortizationPaymentDTO> schedule) {
        schedule.forEach((payment) -> {
            table.addCell(createCustomCell(payment.getYear().toString()));
            table.addCell(createCustomCell(payment.getTotalPaid().toString()));
            table.addCell(createCustomCell(payment.getPrincipalPaid().toString()));
            table.addCell(createCustomCell(payment.getInterestPaid().toString()));
            table.addCell(createCustomCell(payment.getRemainingBalance().toString()));
        });
    }

    private void addEmptyRow(PdfPTable table) {
        // Create an empty cell that spans all columns
        PdfPCell emptyCell = new PdfPCell(new Phrase("")); // Empty content
        emptyCell.setColspan(table.getNumberOfColumns()); // Span across all columns
        emptyCell.setBorder(PdfPCell.NO_BORDER); // No border for the empty row
        emptyCell.setFixedHeight(20);
        table.addCell(emptyCell);
    }

    private void addFinalRow(PdfPTable table, int totalYears, double totalPaid, double totalPrincipalPaid, double totalInterestPaid) {
        // ADD EMPTY FULL ROW
        addEmptyRow(table);

        table.addCell(createCustomCell(totalYears + " Years", 2, Alignment.CENTER));
        table.addCell(createCustomCell(String.valueOf(totalPaid), 2, Alignment.CENTER));
        table.addCell(createCustomCell(String.valueOf(totalPrincipalPaid), 2, Alignment.CENTER));
        table.addCell(createCustomCell(String.valueOf(totalInterestPaid), 2, Alignment.CENTER));
        table.addCell(createCustomCell("0", 2, Alignment.CENTER));
    }

    private Paragraph createHeadingH1(String text, int fontSize, Alignment alignment) {
        return createHeadingH1(text, fontSize, alignment, 10);
    }

    private int getAlignment(Alignment alignment){
        return switch (alignment) {
            case LEFT -> Element.ALIGN_LEFT;
            case RIGHT -> Element.ALIGN_RIGHT;
            default -> Element.ALIGN_CENTER;
        };
    }

    private Paragraph createHeadingH1(String text, int fontSize, Alignment alignment, int spacingAfter) {
        int align = getAlignment(alignment);
        Font headerFont = new Font();
        headerFont.setSize(fontSize);
        Paragraph header = new Paragraph(text, headerFont);
        header.setAlignment(align);
        header.setSpacingAfter(spacingAfter);
        return header;
    }

    public byte[] generateAmortizationSchedulePdf(String siteName, LoanResponseData loanResponseData) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // TODO: TESTING FILE -> AFTER WE WILL CHANGE IT TO ByteArrayOutputStream for InMemory
            // FileOutputStream file = new FileOutputStream("output.pdf");
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();

            // SiteHeading
            Paragraph siteHeading = createHeadingH1(siteName, 20, Alignment.LEFT, 20);
            doc.add(siteHeading);


            // Heading 1
            Paragraph heading1 = createHeadingH1("Loan Amortization Schedule", 20, Alignment.CENTER, 10);
            doc.add(heading1);

            // List of Amortization Schedule
            List<AmortizationPaymentDTO> schedule = loanResponseData.getAmortizationSchedule();

            // Table with 7 columns HEADING ROW -> year, totalPaid, principalPaid, interestPaid, remainingBalance
            PdfPTable table = new PdfPTable(5);
            configureTable(table);

            // Add Table Header
            addTableHeader(table);

            // Add Data Rows
            addRows(table, schedule);

            // Add Final Row
            int totalYears = schedule.size();
            double totalPaid = Math.round(loanResponseData.getTotalPayment().doubleValue() * 100.0) / 100.0;
            double totalInterestPaid = Math.round(loanResponseData.getTotalInterest().doubleValue() * 100.0) / 100.0;
            double totalPrincipalPaid = totalPaid - totalInterestPaid;
            addFinalRow(table, totalYears, totalPaid, totalPrincipalPaid, totalInterestPaid);
            doc.add(table);

            doc.close();
            // file.close();
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Error occurred while generating PDF under \"ByteArrayOutputStream\": ", e);
        } catch (DocumentException e) {
            log.error("Error occurred while generating PDF under \"Document\": ", e);
        }
        throw new RuntimeException("Error occurred while generating PDF");
    }
}
