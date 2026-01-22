package com.fileconverter.csvtojson;

import com.fileconverter.csvtojson.model.CostAllocInterface.Header;
import com.fileconverter.csvtojson.model.CostAllocInterface.Root;
import com.fileconverter.csvtojson.service.CostAllocInterfaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CostAllocInterfaceServiceTest {

    @InjectMocks
    private CostAllocInterfaceService service;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void convert_shouldGroupLinesUnderSameHeader() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "HDR1;TYPE1;1;20240101;20240102;Supplier A;20240103;1000,50;270,50;HUF;20240110;" +
                        "LINE1;10;100;John Doe;Budapest;123;SAP1;20240105\n" +
                        "HDR1;TYPE1;1;20240101;20240102;Supplier A;20240103;1000,50;270,50;HUF;20240110;" +
                        "LINE2;11;101;Jane Doe;Budapest;124;SAP2;20240106";

        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(multipartFile.getOriginalFilename()).thenReturn("test.csv");

        Root root = service.convert(multipartFile);

        assertEquals(1, root.headers.size());

        Header header = root.headers.get(0);
        assertEquals("HDR1", header.costAllocationIdentifier);
        assertEquals(new BigDecimal("1000.50"), header.grossAmount);
        assertEquals(2, header.lines.size());
    }

    @Test
    void convert_shouldCreateNewHeader_whenIdentifierChanges() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "HDR1;TYPE1;1;20240101;20240102;Supplier A;20240103;100;27;HUF;20240110;" +
                        "LINE1;1;100;John Doe;Bp;1;SAP1;20240105\n" +
                        "HDR2;TYPE2;2;20240111;20240112;Supplier B;20240113;200;54;EUR;20240120;" +
                        "LINE2;2;200;Jane Doe;Vienna;2;SAP2;20240115";

        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(multipartFile.getOriginalFilename()).thenReturn("test.csv");

        Root root = service.convert(multipartFile);

        assertEquals(2, root.headers.size());
        assertEquals("HDR1", root.headers.get(0).costAllocationIdentifier);
        assertEquals("HDR2", root.headers.get(1).costAllocationIdentifier);
    }

    @Test
    void convert_shouldHandleEmptyAndNullValuesAsNull() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "HDR1;;;;;;;NULL;;;;" +
                        "LINE1;NULL;;;;;;;";

        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(multipartFile.getOriginalFilename()).thenReturn("empty.csv");

        Root root = service.convert(multipartFile);

        Header header = root.headers.get(0);

        assertNull(header.costAllocationTypeCode);
        assertNull(header.grossAmount);
        assertEquals(1, header.lines.size());
        assertNull(header.lines.get(0).originalCostAllocationLineId);
    }

    @Test
    void parseValue_shouldInferCorrectTypesAndHandleNullLiteral() throws Exception {
        var method = CostAllocInterfaceService.class
                .getDeclaredMethod("parseValue", String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(service, "true"));
        assertEquals(123L, method.invoke(service, "123"));
        assertEquals(new BigDecimal("12.34"), method.invoke(service, "12,34"));
        assertNull(method.invoke(service, " "));
        assertNull(method.invoke(service, "NULL"));
        assertNull(method.invoke(service, "null"));
        assertEquals("ABC", method.invoke(service, "ABC"));
    }
}