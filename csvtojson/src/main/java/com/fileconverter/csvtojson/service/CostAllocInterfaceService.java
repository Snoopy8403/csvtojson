package com.fileconverter.csvtojson.service;

import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;

/**
 * Ideiglenesen használt service, addig amíg a végleges INCASSO -> ORCA interface nem készül el.
 * A költségterhelés CSV fájlt JSON struktúrává alakító szolgáltatás.
 */
@Slf4j
@Service
public class CostAllocInterfaceService {

    public Root convert(MultipartFile file) throws Exception {
        log.info("Fileprocessing started: {}", file.getOriginalFilename());

        CSVParser parser = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .withTrim()
                .withIgnoreSurroundingSpaces()
                .parse(new InputStreamReader(file.getInputStream()));

        Root root = new Root();

        Header currentHeader = null;
        String previousHeaderId = null;

        for (CSVRecord csvRecord : parser) {

            String headerIdRaw = getSafe(csvRecord, "CostAllocationIdentifier");

            if (headerIdRaw == null) {
                log.warn("Missing CostAllocationIdentifier at record {}", csvRecord.getRecordNumber());
                continue;
            }

            if (currentHeader == null || !headerIdRaw.equals(previousHeaderId)) {

                currentHeader = new Header();
                currentHeader.costAllocationIdentifier = parseValue(headerIdRaw);
                currentHeader.costAllocationTypeCode = parseValue(getSafe(csvRecord, "CostAllocationTypeCode"));
                currentHeader.statusCode = parseValue(getSafe(csvRecord, "StatusCode"));
                currentHeader.documentCreationDate = parseValue(getSafe(csvRecord, "DocumentCreationDate"));
                currentHeader.approvalDate = parseValue(getSafe(csvRecord, "ApprovalDate"));
                currentHeader.supplierName = parseValue(getSafe(csvRecord, "SupplierName"));
                currentHeader.accountingDate = parseValue(getSafe(csvRecord, "AccountingDate"));
                currentHeader.grossAmount = parseValue(getSafe(csvRecord, "GrossAmount"));
                currentHeader.vatAmount = parseValue(getSafe(csvRecord, "VatAmount"));
                currentHeader.currencyCode = parseValue(getSafe(csvRecord, "CurrencyCode"));
                currentHeader.dueDate = parseValue(getSafe(csvRecord, "DueDate"));

                root.headers.add(currentHeader);
            }

            Line line = new Line();
            line.costAllocationLineIdentifier = parseValue(getSafe(csvRecord, "CostAllocationLineIdentifier"));
            line.originalCostAllocationLineId = parseValue(getSafe(csvRecord, "OriginalCostAllocationLineID"));
            line.statusCode = parseValue(getSafe(csvRecord, "StatusCode"));
            line.grossAmount = parseValue(getSafe(csvRecord, "GrossAmount"));
            line.vatAmount = parseValue(getSafe(csvRecord, "VatAmount"));
            line.currencyCode = parseValue(getSafe(csvRecord, "CurrencyCode"));
            line.debtCaseId = parseValue(getSafe(csvRecord, "DebtCaseId"));
            line.debtorName = parseValue(getSafe(csvRecord, "DebtorName"));
            line.collateralCity = parseValue(getSafe(csvRecord, "CollateralCity"));
            line.collateralParcelNumber = parseValue(getSafe(csvRecord, "CollateralParcelNumber"));
            line.sapDocumentNumber = parseValue(getSafe(csvRecord, "SapDocumentNumber"));
            line.fulfillmentDate = parseValue(getSafe(csvRecord, "FulfillmentDate"));

            currentHeader.lines.add(line);
            previousHeaderId = headerIdRaw;
        }

        return root;
    }

    /**
     * “If the field is empty, it does not throw an exception.
     */
    private String getSafe(CSVRecord record, String column) {
        if (record.isMapped(column) && record.isSet(column)) {
            return record.get(column);
        }
        return null;
    }

    /**
     * Type inference for CSV values: Boolean, Long, BigDecimal, String
     */
    private Object parseValue(String value) {

        if (value == null) {
            return null;
        }

        String v = value.trim();

        // NULL, null, empty -> null
        if (v.isEmpty() || "null".equalsIgnoreCase(v)) {
            return null;
        }

        // Boolean
        if ("true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
            return Boolean.valueOf(v);
        }

        // Integer (Long)
        if (v.matches("[-+]?\\d+")) {
            try {
                return Long.valueOf(v);
            } catch (NumberFormatException e) {
                log.error("Number format exception (Not Integer): {}", e.toString());
                throw new RuntimeException("Failed to parse integer value: " + v);
            }
        }

        // Decimal
        if (v.matches("[-+]?\\d+[\\.,]\\d+")) {
            try {
                return new BigDecimal(v.replace(",", "."));
            } catch (NumberFormatException e) {
                log.error("Number format exception (Not Decimal): {}", e.toString());
                throw new RuntimeException("Failed to parse decimal value: " + v);
            }
        }

        return v;
    }
}
