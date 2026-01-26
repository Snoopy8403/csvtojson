package com.fileconverter.csvtojson;

import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.Root;
import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.Header;
import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.Line;
import com.fileconverter.csvtojson.service.CostAllocInterfaceService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CostAllocInterfaceServiceTest {

    private final CostAllocInterfaceService service = new CostAllocInterfaceService();

    @Test
    void shouldHandleMissingColumnsAndShortRecordsGracefully() throws Exception {
        // GIVEN: FulfillmentDate header létezik, de az adat sor rövidebb
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;" +
                        "ApprovalDate;SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;" +
                        "DueDate;CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;" +
                        "DebtorName;CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "1001;1;10;2024-01-01;2024-01-02;Supplier Kft;2024-01-03;10000;2700;HUF;" +
                        "2024-02-01;2001;NULL;3001;John Doe;Budapest;12345;SAP123\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        // WHEN
        Root result = service.convert(file);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.getHeaders().size());

        Header header = result.getHeaders().get(0);
        assertEquals(1001L, header.getCostAllocationIdentifier());
        assertEquals("Supplier Kft", header.getSupplierName());
        assertEquals(1, header.getLines().size());

        Line line = header.getLines().get(0);

        assertEquals(2001L, line.getCostAllocationLineIdentifier());

        // "NULL" → null
        assertNull(line.getOriginalCostAllocationLineId());

        // HIÁNYZÓ oszlop → null (ÉS nincs exception)
        assertNull(line.getFulfillmentDate());

        assertEquals("Budapest", line.getCollateralCity());
        assertEquals("SAP123", line.getSapDocumentNumber());
    }

    @Test
    void shouldGroupLinesUnderSameHeader() throws Exception {
        // GIVEN: két sor ugyanazzal a header ID-val
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;GrossAmount;" +
                        "CostAllocationLineIdentifier\n" +
                        "5000;2;20;1000;1\n" +
                        "5000;2;20;1000;2\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "grouping.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        // WHEN
        Root result = service.convert(file);

        // THEN
        assertEquals(1, result.getHeaders().size());
        assertEquals(2, result.getHeaders().get(0).getLines().size());
    }

    @Test
    void shouldCreateNewHeaderWhenIdentifierChanges() throws Exception {
        String csv =
                "CostAllocationIdentifier;CostAllocationLineIdentifier\n" +
                        "1;10\n" +
                        "2;20\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "multiheader.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        Root result = service.convert(file);

        assertEquals(2, result.getHeaders().size());
        assertEquals(1, result.getHeaders().get(0).getLines().size());
        assertEquals(1, result.getHeaders().get(1).getLines().size());
    }
}
