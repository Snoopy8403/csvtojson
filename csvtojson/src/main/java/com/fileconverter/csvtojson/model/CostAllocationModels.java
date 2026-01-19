package com.fileconverter.csvtojson.model;

import java.util.ArrayList;
import java.util.List;

public class CostAllocationModels {

    public static class Root {
        public List<Header> headers = new ArrayList<>();
    }

    public static class Header {
        public String costAllocationIdentifier;
        public String costAllocationTypeCode;
        public String statusCode;
        public String documentCreationDate;
        public String approvalDate;
        public String supplierName;
        public String accountingDate;
        public String grossAmount;
        public String vatAmount;
        public String currencyCode;
        public String dueDate;
        public List<Line> lines = new ArrayList<>();
    }

    public static class Line {
        public String costAllocationLineIdentifier;
        public String originalCostAllocationLineId;
        public String statusCode;
        public String grossAmount;
        public String vatAmount;
        public String currencyCode;
        public String debtCaseId;
        public String debtorName;
        public String collateralCity;
        public String collateralParcelNumber;
        public String sapDocumentNumber;
        public String fulfillmentDate;
    }
}