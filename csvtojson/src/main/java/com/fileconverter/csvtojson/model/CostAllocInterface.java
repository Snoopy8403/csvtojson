package com.fileconverter.csvtojson.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.List;

public class CostAllocInterface {

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
        public Object costAllocationIdentifier;
        public Object costAllocationTypeCode;
        public Object statusCode;
        public Object documentCreationDate;
        public Object approvalDate;
        public Object supplierName;
        public Object accountingDate;
        public Object grossAmount;
        public Object vatAmount;
        public Object currencyCode;
        public Object dueDate;
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
        public Object costAllocationLineIdentifier;
        public Object originalCostAllocationLineId;
        public Object statusCode;
        public Object grossAmount;
        public Object vatAmount;
        public Object currencyCode;
        public Object debtCaseId;
        public Object debtorName;
        public Object collateralCity;
        public Object collateralParcelNumber;
        public Object sapDocumentNumber;
        public Object fulfillmentDate;
    }
}