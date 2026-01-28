package com.fileconverter.csvtojson.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Cost Allocation Interface adatstruktúra CSV → JSON konverzióhoz.
 * <p>
 *     Ez a DTO csak IDEIGELENES! TÖRLÉSRE FOG KERÜLNI! A későbbiek során az INCASSO szinkron fogja kiváltani!!!
 * </p>
 *
 * <p>
 * A DTO hierarchikus felépítésű:
 * <ul>
 *   <li>{@link Root} – a gyökérelem, amely a teljes feldolgozott állományt reprezentálja</li>
 *   <li>{@link Header} – egy költségallokációs fejléc rekord</li>
 *   <li>{@link Line} – a fejléc alá tartozó tételsor</li>
 * </ul>
 * </p>
 *
 * <p>
 * A belső osztályok Jackson annotációkkal vannak ellátva a JSON mezők
 * sorrendjének determinisztikus meghatározásához.
 * </p>
 *
 * <p>
 * Lombok annotációk kerülnek használatra az egyszerű DTO jelleg biztosítására:
 * <ul>
 *   <li>{@link lombok.Data} – getterek, setterek, {@code toString}, {@code equals}, {@code hashCode}</li>
 *   <li>{@link lombok.experimental.Accessors} – láncolható setterek</li>
 *   <li>{@link lombok.NoArgsConstructor} – alapértelmezett konstruktor</li>
 * </ul>
 * </p>
 */


@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class CostAllocInterfaceDto {

    /**
     * Gyökér DTO objektum a Cost Allocation Interface struktúrához.
     *
     * <p>
     * A {@link Header} rekordok gyűjteményét tartalmazza,
     * amelyek a CSV feldolgozás során kerülnek feltöltésre.
     * </p>
     */
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

    /**
     * Cost Allocation fejléc DTO.
     *
     * <p>
     * Egy költségallokációs egységet reprezentál, amelyhez
     * egy vagy több {@link Line} tételsor tartozhat.
     * </p>
     *
     * <p>
     * A {@link JsonPropertyOrder} annotáció biztosítja,
     * hogy a JSON kimenet mezőinek sorrendje determinisztikus legyen,
     * ami integrációs és audit célokra fontos lehet.
     * </p>
     */
    @Data
    @NoArgsConstructor
    public static class Header {
        private String costAllocationIdentifier;
        private String costAllocationTypeCode;
        private String statusCode;
        private String documentCreationDate;
        private String approvalDate;
        private String supplierName;
        private String accountingDate;
        private Double grossAmount;
        private Double vatAmount;
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

    /**
     * Cost Allocation tételsor DTO.
     *
     * <p>
     * Egy {@link Header} alá tartozó egyedi költségallokációs sort reprezentál,
     * amely pénzügyi és üzleti azonosító adatokat tartalmaz.
     * </p>
     *
     * <p>
     * A JSON mezők sorrendjét a {@link JsonPropertyOrder} annotáció határozza meg.
     * </p>
     */
    @Data
    @NoArgsConstructor
    public static class Line {
        private String costAllocationLineIdentifier;
        private String originalCostAllocationLineId;
        private String statusCode;
        private Double grossAmount;
        private Double vatAmount;
        private String currencyCode;
        private String debtCaseId;
        private String debtorName;
        private String collateralCity;
        private String collateralParcelNumber;
        private String sapDocumentNumber;
        private String fulfillmentDate;
    }
}
