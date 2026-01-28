package com.fileconverter.csvtojson;

import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.*;
import com.fileconverter.csvtojson.service.CostAllocInterfaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CostAllocInterfaceServiceTest {

    private CostAllocInterfaceService service;

    @BeforeEach
    void setUp() {
        service = new CostAllocInterfaceService();
    }

    @Test
    void shouldConvertCsvToDtoWithSingleHeaderAndMultipleLines() throws Exception {
        // given
        String csv =
                "CostAllocationIdentifier;CostAllocationTypeCode;StatusCode;DocumentCreationDate;ApprovalDate;" +
                        "SupplierName;AccountingDate;GrossAmount;VatAmount;CurrencyCode;DueDate;" +
                        "CostAllocationLineIdentifier;OriginalCostAllocationLineID;DebtCaseId;DebtorName;" +
                        "CollateralCity;CollateralParcelNumber;SapDocumentNumber;FulfillmentDate\n" +
                        "HDR1;TYPE1;ACTIVE;2024-01-01;2024-01-02;Supplier A;2024-01-03;100.50;27.00;HUF;2024-01-10;" +
                        "LINE1;ORIG1;DC1;John Doe;Budapest;123;SAP1;2024-01-05\n" +
                        "HDR1;TYPE1;ACTIVE;2024-01-01;2024-01-02;Supplier A;2024-01-03;200.00;54.00;HUF;2024-01-10;" +
                        "LINE2;ORIG2;DC2;Jane Doe;Debrecen;456;SAP2;2024-01-06";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        // when
        Root result = service.convert(file);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHeaders()).hasSize(1);

        Header header = result.getHeaders().get(0);
        assertThat(header.getCostAllocationIdentifier()).isEqualTo("HDR1");
        assertThat(header.getCostAllocationTypeCode()).isEqualTo("TYPE1");
        assertThat(header.getSupplierName()).isEqualTo("Supplier A");
        assertThat(header.getGrossAmount()).isEqualTo(100.50);
        assertThat(header.getVatAmount()).isEqualTo(27.00);
        assertThat(header.getCurrencyCode()).isEqualTo("HUF");

        assertThat(header.getLines()).hasSize(2);

        Line firstLine = header.getLines().get(0);
        assertThat(firstLine.getCostAllocationLineIdentifier()).isEqualTo("LINE1");
        assertThat(firstLine.getGrossAmount()).isEqualTo(100.50);
        assertThat(firstLine.getVatAmount()).isEqualTo(27.00);
        assertThat(firstLine.getDebtorName()).isEqualTo("John Doe");

        Line secondLine = header.getLines().get(1);
        assertThat(secondLine.getCostAllocationLineIdentifier()).isEqualTo("LINE2");
        assertThat(secondLine.getGrossAmount()).isEqualTo(200.00);
        assertThat(secondLine.getVatAmount()).isEqualTo(54.00);
        assertThat(secondLine.getDebtorName()).isEqualTo("Jane Doe");
    }

    @Test
    void shouldThrowExceptionWhenNumericFieldIsInvalid() {
        // given
        String csv =
                "CostAllocationIdentifier;GrossAmount;VatAmount\n" +
                        "HDR1;NOT_A_NUMBER;27.00";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        // when + then
        assertThrows(IllegalArgumentException.class, () -> service.convert(file));
    }
}
