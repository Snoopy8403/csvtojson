package com.fileconverter.csvtojson.service;

import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;

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
        Long previousHeaderId = null;

        for (CSVRecord record : parser) {

            Long headerId = getValue(record, "CostAllocationIdentifier", Long.class);
            if (headerId == null) {
                log.warn("Missing CostAllocationIdentifier at record {}", record.getRecordNumber());
                continue;
            }

            if (currentHeader == null || !headerId.equals(previousHeaderId)) {

                currentHeader = new Header();
                currentHeader.costAllocationIdentifier =
                        headerId;
                currentHeader.costAllocationTypeCode =
                        getValue(record, "CostAllocationTypeCode", Integer.class);
                currentHeader.statusCode =
                        getValue(record, "StatusCode", Integer.class);
                currentHeader.documentCreationDate =
                        getValue(record, "DocumentCreationDate", String.class);
                currentHeader.approvalDate =
                        getValue(record, "ApprovalDate", String.class);
                currentHeader.supplierName =
                        getValue(record, "SupplierName", String.class);
                currentHeader.accountingDate =
                        getValue(record, "AccountingDate", String.class);
                currentHeader.grossAmount =
                        getValue(record, "GrossAmount", Long.class);
                currentHeader.vatAmount =
                        getValue(record, "VatAmount", Long.class);
                currentHeader.currencyCode =
                        getValue(record, "CurrencyCode", String.class);
                currentHeader.dueDate =
                        getValue(record, "DueDate", String.class);

                root.headers.add(currentHeader);
            }

            Line line = new Line();
            line.costAllocationLineIdentifier =
                    getValue(record, "CostAllocationLineIdentifier", Long.class);
            line.originalCostAllocationLineId =
                    getValue(record, "OriginalCostAllocationLineID", Long.class);
            line.statusCode =
                    getValue(record, "StatusCode", Integer.class);
            line.grossAmount =
                    getValue(record, "GrossAmount", Long.class);
            line.vatAmount =
                    getValue(record, "VatAmount", Long.class);
            line.currencyCode =
                    getValue(record, "CurrencyCode", String.class);
            line.debtCaseId =
                    getValue(record, "DebtCaseId", Long.class);
            line.debtorName =
                    getValue(record, "DebtorName", String.class);
            line.collateralCity =
                    getValue(record, "CollateralCity", String.class);
            line.collateralParcelNumber =
                    getValue(record, "CollateralParcelNumber", String.class);
            line.sapDocumentNumber =
                    getValue(record, "SapDocumentNumber", String.class);
            line.fulfillmentDate =
                    getValue(record, "FulfillmentDate", String.class);

            currentHeader.lines.add(line);
            previousHeaderId = headerId;
        }

        return root;
    }

    /**
     * Generic, type-safe CSV value extractor.
     * Handles missing columns, empty values and "NULL" literals.
     */
    private <T> T getValue(CSVRecord record, String column, Class<T> type) {

        if (!record.isMapped(column) || !record.isSet(column)) {
            return null;
        }

        String raw = record.get(column);
        if (raw == null) {
            return null;
        }

        String v = raw.trim();
        if (v.isEmpty() || "null".equalsIgnoreCase(v)) {
            return null;
        }

        try {
            if (type == String.class) {
                return type.cast(v);
            }

            if (type == Integer.class) {
                return type.cast(Integer.valueOf(v));
            }

            if (type == Long.class) {
                return type.cast(
                        new BigDecimal(v.replace(",", ".")).longValue()
                );
            }

        } catch (Exception e) {
            log.warn(
                    "Failed to parse column '{}' with value '{}' as {}",
                    column, v, type.getSimpleName()
            );
        }

        return null;
    }
}
