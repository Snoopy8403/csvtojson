package com.fileconverter.csvtojson.service;

import com.fileconverter.csvtojson.model.CostAllocationModels.*;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;

@Service
public class CsvToJsonService {

    public Root convert(MultipartFile file) throws Exception {

        CSVParser parser = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .withTrim()
                .parse(new InputStreamReader(file.getInputStream()));

        Root root = new Root();

        Header currentHeader = null;
        String previousHeaderId = null;

        for (CSVRecord record : parser) {

            String headerIdRaw = record.get("CostAllocationIdentifier");

            if (currentHeader == null || !headerIdRaw.equals(previousHeaderId)) {

                currentHeader = new Header();
                currentHeader.costAllocationIdentifier = parseValue(headerIdRaw);
                currentHeader.costAllocationTypeCode = parseValue(record.get("CostAllocationTypeCode"));
                currentHeader.statusCode = parseValue(record.get("StatusCode"));
                currentHeader.documentCreationDate = parseValue(record.get("DocumentCreationDate"));
                currentHeader.approvalDate = parseValue(record.get("ApprovalDate"));
                currentHeader.supplierName = parseValue(record.get("SupplierName"));
                currentHeader.accountingDate = parseValue(record.get("AccountingDate"));
                currentHeader.grossAmount = parseValue(record.get("GrossAmount"));
                currentHeader.vatAmount = parseValue(record.get("VatAmount"));
                currentHeader.currencyCode = parseValue(record.get("CurrencyCode"));
                currentHeader.dueDate = parseValue(record.get("DueDate"));

                root.headers.add(currentHeader);
            }

            Line line = new Line();
            line.costAllocationLineIdentifier = parseValue(record.get("CostAllocationLineIdentifier"));
            line.originalCostAllocationLineId = parseValue(record.get("OriginalCostAllocationLineID"));
            line.statusCode = parseValue(record.get("StatusCode"));
            line.grossAmount = parseValue(record.get("GrossAmount"));
            line.vatAmount = parseValue(record.get("VatAmount"));
            line.currencyCode = parseValue(record.get("CurrencyCode"));
            line.debtCaseId = parseValue(record.get("DebtCaseId"));
            line.debtorName = parseValue(record.get("DebtorName"));
            line.collateralCity = parseValue(record.get("CollateralCity"));
            line.collateralParcelNumber = parseValue(record.get("CollateralParcelNumber"));
            line.sapDocumentNumber = parseValue(record.get("SapDocumentNumber"));
            line.fulfillmentDate = parseValue(record.get("FulfillmentDate"));

            currentHeader.lines.add(line);

            previousHeaderId = headerIdRaw;
        }

        return root;
    }

    /**
     * 🔍 Dinamikus típusfelismerés
     */
    private Object parseValue(String value) {

        if (value == null) {
            return null;
        }

        String v = value.trim();
        if (v.isEmpty()) {
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
            } catch (NumberFormatException ignored) {
            }
        }

        // Decimal
        if (v.matches("[-+]?\\d+[\\.,]\\d+")) {
            try {
                return new BigDecimal(v.replace(",", "."));
            } catch (NumberFormatException ignored) {
            }
        }

        // Fallback: String
        return v;
    }
}
