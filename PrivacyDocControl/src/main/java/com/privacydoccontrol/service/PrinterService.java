package com.privacydoccontrol.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Service
public class PrinterService {

    /**
     * Prints a file using the default printer.
     *
     * @param filePath The path to the file to print
     * @return true if printing was successful, false otherwise
     */
    public boolean printFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("File not found for printing: {}", filePath);
                return false;
            }

            // Create a print request attribute set
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
            pras.add(OrientationRequested.PORTRAIT);
            pras.add(MediaSizeName.ISO_A4);

            // Get the default print service
            PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
            if (printService == null) {
                log.warn("No default printer found");
                return false;
            }

            // Create a DocPrintJob
            DocPrintJob printJob = printService.createPrintJob();

            // Create a FileInputStream for the file
            FileInputStream fis = new FileInputStream(file);

            // Create a SimpleDoc
            Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

            // Print the document
            printJob.print(doc, pras);
            
            // Close the FileInputStream
            fis.close();
            
            log.info("Successfully printed file: {}", filePath);
            return true;
        } catch (PrintException e) {
            log.error("Print exception while printing file {}: {}", filePath, e.getMessage(), e);
            return false;
        } catch (IOException e) {
            log.error("IO exception while printing file {}: {}", filePath, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while printing file {}: {}", filePath, e.getMessage(), e);
            return false;
        }
    }
}