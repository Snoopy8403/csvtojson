package com.fileconverter.csvtojson.service;

import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
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

    /* ---------- SAFE typed getters ---------- */

    private String getString(CSVRecord record, String column) {
        return getRaw(record, column);
    }

    private Long getLong(CSVRecord record, String column) {
        String value = getRaw(record, column);
        if (value == null) return null;

        try {
            if (value.contains(",")) value = value.replace(",", ".");
            if (value.contains(".")) {
                return (long) Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getInteger(CSVRecord record, String column) {
        String value = getRaw(record, column);
        if (value == null) return null;

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 💣 BULLETPROOF CSV ACCESS
     * - header nincs → null
     * - rekord rövidebb → null
     * - üres / "NULL" → null
     */
    private String getRaw(CSVRecord record, String column) {
        if (!record.isMapped(column)) {
            return null;
        }

        int index = record.getParser().getHeaderMap().get(column);

        if (index >= record.size()) {
            return null;
        }

        String value = record.get(index);
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty() || "NULL".equalsIgnoreCase(trimmed)) {
            return null;
        }

        return trimmed;
    }
}