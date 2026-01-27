package com.fileconverter.csvtojson.service;

import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CostAllocInterfaceServiceTest {

    @InjectMocks
    private CostAllocInterfaceService service;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void convert_shouldParseCsvIntoRootDto() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "1;100;1;20240101;20240102;Supplier A;20240103;1000,50;270,50;HUF;20240110;" +
                        "10;1000;100;John Doe;Budapest;123;SAP1;20240105\n" +
                        "1;100;1;20240101;20240102;Supplier A;20240103;1000,50;270,50;HUF;20240110;" +
                        "11;1001;101;Jane Doe;Budapest;124;SAP2;20240106\n" +
                        "2;200;2;20240111;20240112;Supplier B;20240113;2000,00;540,00;EUR;20240120;" +
                        "20;2000;200;Alice;Vienna;10;SAP3;20240115";

        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(multipartFile.getOriginalFilename()).thenReturn("test.csv");

        Root root = service.convert(multipartFile);

        // Header-ok száma
        List<Header> headers = root.getHeaders();
        assertEquals(2, headers.size());

        // Első header
        Header header1 = headers.get(0);
        assertEquals(1L, header1.getCostAllocationIdentifier());
        assertEquals(100L, header1.getCostAllocationTypeCode());
        assertEquals(1L, header1.getStatusCode());
        assertEquals("20240101", header1.getDocumentCreationDate());
        assertEquals("20240102", header1.getApprovalDate());
        assertEquals("Supplier A", header1.getSupplierName());
        assertEquals(1000.50, header1.getGrossAmount());
        assertEquals(270.50, header1.getVatAmount());
        assertEquals(2, header1.getLines().size());

        Line line1 = header1.getLines().get(0);
        assertEquals(10L, line1.getCostAllocationLineIdentifier());
        assertEquals(1000L, line1.getOriginalCostAllocationLineId());
        assertEquals(100L, line1.getDebtCaseId());
        assertEquals("John Doe", line1.getDebtorName());

        Line line2 = header1.getLines().get(1);
        assertEquals(11L, line2.getCostAllocationLineIdentifier());
        assertEquals("Jane Doe", line2.getDebtorName());

        // Második header
        Header header2 = headers.get(1);
        assertEquals(2L, header2.getCostAllocationIdentifier());
        assertEquals(1, header2.getLines().size());
        assertEquals("Alice", header2.getLines().get(0).getDebtorName());
    }

    @Test
    void convert_shouldHandleEmptyAndNullValues() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "NULL;;1;;;;;;;NULL;;NULL;;;;;;";

        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(multipartFile.getOriginalFilename()).thenReturn("empty.csv");

        Root root = service.convert(multipartFile);
        Header header = root.getHeaders().get(0);

        assertNull(header.getCostAllocationIdentifier());
        assertNull(header.getCostAllocationTypeCode());
        assertNull(header.getDocumentCreationDate());
        assertNull(header.getSupplierName());

        Line line = header.getLines().get(0);
        assertNull(line.getCostAllocationLineIdentifier());
        assertNull(line.getOriginalCostAllocationLineId());
        assertNull(line.getDebtorName());
    }

    @Test
    void convert_shouldThrowExceptionForInvalidNumeric() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "abc;100;1;20240101;20240102;Supplier A;20240103;1000,50;270,50;HUF;20240110;" +
                        "10;1000;100;John Doe;Budapest;123;SAP1;20240105";

        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(multipartFile.getOriginalFilename()).thenReturn("invalid.csv");

        assertThrows(IllegalArgumentException.class, () -> service.convert(multipartFile));
    }
}
