package com.fileconverter.csvtojson.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CostAllocInterfaceDto {

    @Data
    @NoArgsConstructor
    public static class Root {
        private List<Header> headers = new ArrayList<>();
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

    @Data
    @NoArgsConstructor
    public static class Header {
        private Long costAllocationIdentifier;
        private Integer costAllocationTypeCode;
        private Integer statusCode;
        private String documentCreationDate;
        private String approvalDate;
        private String supplierName;
        private String accountingDate;
        private Long grossAmount;
        private Long vatAmount;
        private String currencyCode;
        private String dueDate;
        private List<Line> lines = new ArrayList<>();
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

    @Data
    @NoArgsConstructor
    public static class Line {
        private Long costAllocationLineIdentifier;
        private Long originalCostAllocationLineId;
        private Integer statusCode;
        private Long grossAmount;
        private Long vatAmount;
        private String currencyCode;
        private Long debtCaseId;
        private String debtorName;
        private String collateralCity;
        private String collateralParcelNumber;
        private String sapDocumentNumber;
        private String fulfillmentDate;
    }
}
