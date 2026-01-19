package com.fileconverter.csvtojson.service;

import com.fileconverter.csvtojson.model.CostAllocationModels.*;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;

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

            String currentHeaderId = record.get("CostAllocationIdentifier");

            // ÚJ HEADER KEZDŐDIK
            if (currentHeader == null || !currentHeaderId.equals(previousHeaderId)) {

                currentHeader = new Header();
                currentHeader.costAllocationIdentifier = currentHeaderId;
                currentHeader.costAllocationTypeCode = record.get("CostAllocationTypeCode");
                currentHeader.statusCode = record.get("StatusCode");
                currentHeader.documentCreationDate = record.get("DocumentCreationDate");
                currentHeader.approvalDate = record.get("ApprovalDate");
                currentHeader.supplierName = record.get("SupplierName");
                currentHeader.accountingDate = record.get("AccountingDate");
                currentHeader.grossAmount = record.get("GrossAmount");
                currentHeader.vatAmount = record.get("VatAmount");
                currentHeader.currencyCode = record.get("CurrencyCode");
                currentHeader.dueDate = record.get("DueDate");

                root.headers.add(currentHeader);
            }

            // LINE HOZZÁADÁSA
            Line line = new Line();
            line.costAllocationLineIdentifier = record.get("CostAllocationLineIdentifier");
            line.originalCostAllocationLineId = record.get("OriginalCostAllocationLineID");
            line.statusCode = record.get("LineStatusCode");
            line.grossAmount = record.get("LineGrossAmount");
            line.vatAmount = record.get("LineVatAmount");
            line.currencyCode = record.get("LineCurrencyCode");
            line.debtCaseId = record.get("DebtCaseId");
            line.debtorName = record.get("DebtorName");
            line.collateralCity = record.get("CollateralCity");
            line.collateralParcelNumber = record.get("CollateralParcelNumber");
            line.sapDocumentNumber = record.get("SapDocumentNumber");
            line.fulfillmentDate = record.get("FulfillmentDate");

            currentHeader.lines.add(line);

            previousHeaderId = currentHeaderId;
        }

        return root;
    }
}