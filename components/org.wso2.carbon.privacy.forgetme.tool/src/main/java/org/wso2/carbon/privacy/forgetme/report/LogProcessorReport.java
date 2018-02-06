package org.wso2.carbon.privacy.forgetme.report;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogProcessorReport {

    private static final String FILE_NAME = "GDPR-ForgetMe-LogProcessor-Report";

    private StringBuilder stringBuilder;
    private String outputFormat;
    private String directoryPath;

    private static Logger log = LoggerFactory.getLogger(LogProcessorReport.class);

    public LogProcessorReport(String directoryPath, String outputFormat) {

        stringBuilder = new StringBuilder();
        this.directoryPath = directoryPath;
        this.outputFormat = outputFormat;
    }

    public void addToReport(String fileName, int lineNumber, boolean isReplaced) {

        String lineContent;
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        if (isReplaced) {
            lineContent = timeStamp + " [INFO] Replaced username in File: '" + fileName + "', Line number: " +
                    lineNumber + '\n';
        } else {
            lineContent = timeStamp + " [WARN] Possible match found in File: '" + fileName + "', Line number: " +
                    lineNumber + "\n";
        }
        stringBuilder.append(lineContent);
    }

    public void printReport() {

        switch (StringUtils.lowerCase(outputFormat)) {
            case "pdf":
                // Output to PDF file.
                outputPDF();
                break;
            case "txt":
                // Output to text file.
                break;
            default:
                outputPDF();
        }
    }

    private void outputPDF() {

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(new File(directoryPath + File.separator + FILE_NAME
                    + ".pdf")));

            // Open document.
            document.open();

            Font font = new Font();
            font.setStyle(Font.BOLD);
            font.setSize(12);

            Paragraph titleParagraph = new Paragraph("[GDPR] Forget Me - Logs - Final Report", font);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(titleParagraph);

            // Add a couple of blank lines.
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            Paragraph contentParagraph = new Paragraph();
            contentParagraph.add(this.stringBuilder.toString());
            document.add(contentParagraph);

            // Close document.
            document.close();
        } catch (DocumentException | IOException e) {
            log.error("Error occurred while generating PDF report.", e);
        }
    }
}
