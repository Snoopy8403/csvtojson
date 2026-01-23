package com.fileconverter.csvtojson.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class CostAllocInterfaceDto {

    public static class Root {
        public List<Header> headers = new ArrayList<>();
    }

    @JsonPropertyOrder({
            "costAllocationIdentifier",
            "costAllocationTypeCode",
            "statusCode",
            "documentCreationDate",
            "approvalDate",
            "supplierName",
            "accountingDate",
            "grossAmount",
            "vatAmount",
            "currencyCode",
            "dueDate",
            "lines"
    })
    public static class Header {
        public Long costAllocationIdentifier;
        public Integer costAllocationTypeCode;
        public Integer statusCode;
        public String documentCreationDate;
        public String approvalDate;
        public String supplierName;
        public String accountingDate;
        public Long grossAmount;
        public Long vatAmount;
        public String currencyCode;
        public String dueDate;
        public List<Line> lines = new ArrayList<>();
    }

    @JsonPropertyOrder({
            "costAllocationLineIdentifier",
            "originalCostAllocationLineId",
            "statusCode",
            "grossAmount",
            "vatAmount",
            "currencyCode",
            "debtCaseId",
            "debtorName",
            "collateralCity",
            "collateralParcelNumber",
            "sapDocumentNumber",
            "fulfillmentDate"
    })
    public static class Line {
        public Long costAllocationLineIdentifier;
        public Long originalCostAllocationLineId;
        public Integer statusCode;
        public Long grossAmount;
        public Long vatAmount;
        public String currencyCode;
        public Long debtCaseId;
        public String debtorName;
        public String collateralCity;
        public String collateralParcelNumber;
        public String sapDocumentNumber;
        public String fulfillmentDate;
    }
}