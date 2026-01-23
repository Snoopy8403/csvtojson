package com.fileconverter.csvtojson;

import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.*;
import com.fileconverter.csvtojson.service.CostAllocInterfaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
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
    void convert_groupsLinesUnderSameHeader() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "1;10;1;20240101;20240102;Supplier A;20240103;1000;270;HUF;20240110;" +
                        "100;NULL;200;John Doe;Budapest;123;SAP1;20240105\n" +
                        "1;10;1;20240101;20240102;Supplier A;20240103;1000;270;HUF;20240110;" +
                        "101;11;201;Jane Doe;Budapest;124;SAP2;20240106";

        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(multipartFile.getOriginalFilename()).thenReturn("test.csv");

        Root root = service.convert(multipartFile);

        assertEquals(1, root.headers.size());

        Header header = root.headers.get(0);
        assertEquals(2, header.lines.size());

        Line firstLine = header.lines.get(0);
        assertNull(firstLine.originalCostAllocationLineId);
    }

    @Test
    void convert_createsNewHeader_whenIdentifierChanges() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "1;10;1;;;;;;;;100;1;;;;;\n" +
                        "2;20;2;;;;;;;;200;2;;;;;";

        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(multipartFile.getOriginalFilename()).thenReturn("test.csv");

        Root root = service.convert(multipartFile);

        assertEquals(2, root.headers.size());
    }
}
