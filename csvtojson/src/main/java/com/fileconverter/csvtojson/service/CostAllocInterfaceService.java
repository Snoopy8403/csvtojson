package com.fileconverter.csvtojson.service;

import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class CostAllocInterfaceService {

    public Root convert(MultipartFile file) throws Exception {
        log.info("Fileprocessing started: {}", file.getOriginalFilename());

        CSVParser parser = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .withTrim()
                .parse(new InputStreamReader(file.getInputStream()));

        Root root = new Root();
        Header currentHeader = null;
        String previousHeaderId = null;

        for (CSVRecord csvRecord : parser) {
            String headerIdRaw = getString(csvRecord, "CostAllocationIdentifier");

            // null-safe összehasonlítás
            if (currentHeader == null || !Objects.equals(headerIdRaw, previousHeaderId)) {
                currentHeader = new Header()
                        .setCostAllocationIdentifier(getLong(csvRecord, "CostAllocationIdentifier"))
                        .setCostAllocationTypeCode(getInteger(csvRecord, "CostAllocationTypeCode"))
                        .setStatusCode(getInteger(csvRecord, "StatusCode"))
                        .setDocumentCreationDate(getString(csvRecord, "DocumentCreationDate"))
                        .setApprovalDate(getString(csvRecord, "ApprovalDate"))
                        .setSupplierName(getString(csvRecord, "SupplierName"))
                        .setAccountingDate(getString(csvRecord, "AccountingDate"))
                        .setGrossAmount(getLong(csvRecord, "GrossAmount"))
                        .setVatAmount(getLong(csvRecord, "VatAmount"))
                        .setCurrencyCode(getString(csvRecord, "CurrencyCode"))
                        .setDueDate(getString(csvRecord, "DueDate"));

                root.getHeaders().add(currentHeader);
            }

            Line line = new Line()
                    .setCostAllocationLineIdentifier(getLong(csvRecord, "CostAllocationLineIdentifier"))
                    .setOriginalCostAllocationLineId(getLong(csvRecord, "OriginalCostAllocationLineID"))
                    .setStatusCode(getInteger(csvRecord, "StatusCode"))
                    .setGrossAmount(getLong(csvRecord, "GrossAmount"))
                    .setVatAmount(getLong(csvRecord, "VatAmount"))
                    .setCurrencyCode(getString(csvRecord, "CurrencyCode"))
                    .setDebtCaseId(getLong(csvRecord, "DebtCaseId"))
                    .setDebtorName(getString(csvRecord, "DebtorName"))
                    .setCollateralCity(getString(csvRecord, "CollateralCity"))
                    .setCollateralParcelNumber(getString(csvRecord, "CollateralParcelNumber"))
                    .setSapDocumentNumber(getString(csvRecord, "SapDocumentNumber"))
                    .setFulfillmentDate(getString(csvRecord, "FulfillmentDate"));

            currentHeader.getLines().add(line);
            previousHeaderId = headerIdRaw;
        }

        return root;
    }

    /* ---------- Typed helper methods ---------- */

    private String getString(CSVRecord csvRecord, String column) {
        String value = getRaw(csvRecord, column);
        return value;
    }

    private Long getLong(CSVRecord csvRecord, String column) {
        String value = getRaw(csvRecord, column);
        if (value == null) return null;

        try {
            // számoknál vesszőt ponttá alakítunk
            if (value.contains(",")) value = value.replace(",", ".");
            if (value.contains(".")) {
                double d = Double.parseDouble(value);
                return (long) d; // egész számra castoljuk
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getInteger(CSVRecord csvRecord, String column) {
        String value = getRaw(csvRecord, column);
        if (value == null) return null;

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getRaw(CSVRecord csvRecord, String column) {
        Map<String, Integer> headers = csvRecord.getParser().getHeaderMap();

        if (!headers.containsKey(column)) return null;

        String value = csvRecord.get(column);
        if (value == null) return null;

        String trimmed = value.trim();
        if (trimmed.isEmpty() || "NULL".equalsIgnoreCase(trimmed)) return null;

        return trimmed;
    }
}
