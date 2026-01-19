package com.fileconverter.csvtojson.service;

import com.fileconverter.csvtojson.model.CostAllocationModels.*;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.*;

@Service
public class CsvToJsonService {

    public Root convert(MultipartFile file) throws Exception {

        CSVParser parser = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .parse(new InputStreamReader(file.getInputStream()));

        Root root = new Root();
        Map<String, Header> headerMap = new LinkedHashMap<>();

        for (CSVRecord record : parser) {

            String headerId = record.get("CostAllocationIdentifier");

            Header header = headerMap.get(headerId);
            if (header == null) {
                header = new Header();
                header.costAllocationIdentifier = headerId;
                header.costAllocationTypeCode = record.get("CostAllocationTypeCode");
                header.statusCode = record.get("StatusCode");
                header.documentCreationDate = record.get("DocumentCreationDate");
                header.approvalDate = record.get("ApprovalDate");
                header.supplierName = record.get("SupplierName");
                header.accountingDate = record.get("AccountingDate");
                header.grossAmount = record.get("GrossAmount");
                header.vatAmount = record.get("VatAmount");
                header.currencyCode = record.get("CurrencyCode");
                header.dueDate = record.get("DueDate");

                headerMap.put(headerId, header);
            }

            Line line = new Line();
            line.costAllocationLineIdentifier = record.get("CostAllocationLineIdentifier");
            line.originalCostAllocationLineId = record.get("OriginalCostAllocationLineID");
            line.statusCode = record.get("LineStatusCode");
            line.grossAmount = record.get("LineGrossAmount");
            line.vatAmount = record.get("LineVatAmount");
            line.currencyCode = record.get("CurrencyCode");
            line.debtCaseId = record.get("DebtCaseId");
            line.debtorName = record.get("DebtorName");
            line.collateralCity = record.get("CollateralCity");
            line.collateralParcelNumber = record.get("CollateralParcelNumber");
            line.sapDocumentNumber = record.get("SapDocumentNumber");
            line.fulfillmentDate = record.get("FulfillmentDate");

            header.lines.add(line);
        }

        root.headers.addAll(headerMap.values());
        return root;
    }
}
