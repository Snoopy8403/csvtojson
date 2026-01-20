package com.kesmarki.interfacecatalog.service.costallocinterface;

import com.kesmarki.interfacecatalog.domain.costallocinterface.CostAllocInterface.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CostAllocInterfaceServiceTest {

    private CostAllocInterfaceService service;

    @BeforeEach
    void setUp() {
        service = new CostAllocInterfaceService();
    }

    @Test
    void convert_shouldCreateSingleHeaderWithMultipleLines() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "HDR1;TYPE1;1;20240101;20240102;Supplier A;20240103;1000,50;270,50;HUF;20240110;" +
                        "LINE1;10;100;John Doe;Budapest;123;SAP1;20240105\n" +
                        "HDR1;TYPE1;1;20240101;20240102;Supplier A;20240103;1000,50;270,50;HUF;20240110;" +
                        "LINE2;11;101;Jane Doe;Budapest;124;SAP2;20240106";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        Root root = service.convert(file);

        assertNotNull(root);
        assertEquals(1, root.headers.size());

        Header header = root.headers.get(0);
        assertEquals("HDR1", header.costAllocationIdentifier);
        assertEquals("TYPE1", header.costAllocationTypeCode);
        assertEquals(new BigDecimal("1000.50"), header.grossAmount);

        assertEquals(2, header.lines.size());
        assertEquals("LINE1", header.lines.get(0).costAllocationLineIdentifier);
        assertEquals("LINE2", header.lines.get(1).costAllocationLineIdentifier);
    }

    @Test
    void convert_shouldCreateMultipleHeaders_whenIdentifierChanges() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "HDR1;TYPE1;1;20240101;20240102;Supplier A;20240103;100;27;HUF;20240110;" +
                        "LINE1;1;100;John Doe;Bp;1;SAP1;20240105\n" +
                        "HDR2;TYPE2;2;20240111;20240112;Supplier B;20240113;200;54;EUR;20240120;" +
                        "LINE2;2;200;Jane Doe;Vienna;2;SAP2;20240115";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        Root root = service.convert(file);

        assertEquals(2, root.headers.size());
        assertEquals("HDR1", root.headers.get(0).costAllocationIdentifier);
        assertEquals("HDR2", root.headers.get(1).costAllocationIdentifier);
    }

    @Test
    void convert_shouldHandleEmptyAndNullValues() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "HDR1;;;;;;; ;;;" +
                        "LINE1;;;;;;;;";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        Root root = service.convert(file);

        Header header = root.headers.get(0);
        assertNull(header.costAllocationTypeCode);
        assertNull(header.grossAmount);
        assertEquals(1, header.lines.size());
    }

    @Test
    void parseValue_shouldInferCorrectTypes() throws Exception {
        var method = CostAllocInterfaceService.class
                .getDeclaredMethod("parseValue", String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(service, "true"));
        assertEquals(123L, method.invoke(service, "123"));
        assertEquals(new BigDecimal("12.34"), method.invoke(service, "12,34"));
        assertNull(method.invoke(service, "   "));
        assertEquals("ABC", method.invoke(service, "ABC"));
    }
}
