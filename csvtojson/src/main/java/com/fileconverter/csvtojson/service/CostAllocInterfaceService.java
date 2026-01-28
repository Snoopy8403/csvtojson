package com.fileconverter.csvtojson.service;

import com.fileconverter.csvtojson.model.CostAllocInterfaceDto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.Objects;
import java.util.function.Function;

/**
 * Service réteg a Cost Allocation Interface CSV → JSON konverziójához.
 *
 * <p>
 *     Ez a szolgáltatás csak IDEIGELENES! TÖRLÉSRE FOG KERÜLNI! A későbbiek során az INCASSO szinkron fogja kiváltani!!!
 * </p>
 *
 * <p>
 * A szolgáltatás egy feltöltött CSV fájlt dolgoz fel, és azt
 * {@link com.fileconverter.csvtojson.model.CostAllocInterfaceDto.Root}
 * DTO struktúrába tölti.
 * </p>
 *
 * <p>
 * Feldolgozás során:
 * <ul>
 *   <li>a CSV sorokat {@link Header} és {@link Line} DTO-kba rendezi,</li>
 *   <li>a header rekordok a {@code CostAllocationIdentifier} mező alapján
 *       kerülnek csoportosításra,</li>
 *   <li>a numerikus mezők biztonságosan kerülnek parse-olásra
 *       az Apache Commons {@link org.apache.commons.lang3.math.NumberUtils}
 *       segítségével,</li>
 *   <li>a String mezők null-safe módon kerülnek kiolvasásra a
 *       {@link #getRaw(CSVRecord, String)} metóduson keresztül.</li>
 * </ul>
 * </p>
 *
 * <p>
 * A szolgáltatás {@link org.springframework.stereotype.Service} annotációval
 * van ellátva, így Spring bean-ként használható.
 * </p>
 */

@Slf4j
@Service
public class CostAllocInterfaceService {

    /**
     * Numerikus String → {@link Double} konverzió biztonságos ellenőrzéssel.
     *
     * <p>
     * A konverzió menete:
     * <ul>
     *   <li>{@code null} bemenet esetén {@code null}-t ad vissza,</li>
     *   <li>ellenőrzi, hogy az érték számmá alakítható-e
     *       ({@link NumberUtils#isCreatable(String)}),</li>
     *   <li>érvénytelen érték esetén {@link IllegalArgumentException}-t dob,</li>
     *   <li>érvényes érték esetén {@link Double}-re konvertálja.</li>
     * </ul>
     * </p>
     */

    private final Function<String, Double> stringToDouble = text-> {
        if (text == null) {
            return null;
        }
        if (!NumberUtils.isCreatable(text)) {
            throw new IllegalArgumentException("Invalid numeric value: " + text);
        }
        return NumberUtils.createNumber(text).doubleValue();
    };

    /**
     * CSV fájl feldolgozása és Cost Allocation Interface DTO struktúrába töltése.
     *
     * <p>
     * A metódus beolvassa a feltöltött CSV fájlt, majd a rekordokat
     * {@link Header} és {@link Line} DTO-kba szervezi.
     * Az azonos {@code CostAllocationIdentifier} értékkel rendelkező sorok
     * egy közös header alá kerülnek.
     * </p>
     *
     * <p>
     * CSV feldolgozási szabályok:
     * <ul>
     *   <li>a mezőelválasztó karakter {@code ;},</li>
     *   <li>a CSV első sora header-ként kerül értelmezésre,</li>
     *   <li>a header sor nem kerül rekordként feldolgozásra,</li>
     *   <li>a mezők automatikusan trim-elésre kerülnek.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Numerikus mezők parse-olása a {@code stringToDouble} konverterrel történik,
     * amely hibás adat esetén kivételt dob.
     * </p>
     *
     * @param file a feltöltött CSV fájl
     * @return a feldolgozott adatokat tartalmazó Root DTO
     * @throws Exception fájlolvasási vagy feldolgozási hiba esetén
     */

    public Root convert(MultipartFile file) throws Exception {
        log.info("Fileprocessing started: {}", file.getOriginalFilename());

        // CSV formátum konfiguráció
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';') // mezőelválasztó
                .setHeader() // header-t olvasson
                .setSkipHeaderRecord(true) // header sor ne kerüljön rekordként
                .setTrim(true) // szóközök eltávolítása
                .build();

        // CSVParser létrehozása InputStreamReader-rel
        CSVParser parser = format.parse(new InputStreamReader(file.getInputStream()));

        Root root = new Root(); // DTO Root objektum
        Header currentHeader = null; // aktuális header DTO
        String previousHeaderId = null; // előző header azonosító összehasonlításhoz

        // minden CSV rekord feldolgozása
        for (CSVRecord csvRecord : parser) {

            // header azonosító olvasása
            String headerIdRaw = getRaw(csvRecord, "CostAllocationIdentifier");

            // új header kezdete, ha még nincs vagy változott az azonosító
            // String esetén a getRaw-ból egy trim után visszadjuk az értéket
            // Numerikus esetén a getRaw visszadja a Stringet, amit később parse-olunk, ha nem szám exceptiont dobunk
            if (currentHeader == null || !Objects.equals(headerIdRaw, previousHeaderId)) {
                currentHeader = new Header()
                        .setCostAllocationIdentifier(getRaw(csvRecord, "CostAllocationIdentifier"))
                        .setCostAllocationTypeCode(getRaw(csvRecord, "CostAllocationTypeCode"))
                        .setStatusCode(getRaw(csvRecord, "StatusCode"))
                        .setDocumentCreationDate(getRaw(csvRecord, "DocumentCreationDate"))
                        .setApprovalDate(getRaw(csvRecord, "ApprovalDate"))
                        .setSupplierName(getRaw(csvRecord, "SupplierName"))
                        .setAccountingDate(getRaw(csvRecord, "AccountingDate"))
                        .setGrossAmount(stringToDouble.apply(getRaw(csvRecord, "GrossAmount")))
                        .setVatAmount(stringToDouble.apply(getRaw(csvRecord, "VatAmount")))
                        .setCurrencyCode(getRaw(csvRecord, "CurrencyCode"))
                        .setDueDate(getRaw(csvRecord, "DueDate"));

                root.getHeaders().add(currentHeader);
            }

            // Line DTO létrehozása a rekordhoz
            Line line = new Line()

                    .setCostAllocationLineIdentifier(getRaw(csvRecord, "CostAllocationLineIdentifier"))
                    .setOriginalCostAllocationLineId(getRaw(csvRecord, "OriginalCostAllocationLineID"))
                    .setStatusCode(getRaw(csvRecord, "StatusCode"))
                    .setGrossAmount(stringToDouble.apply(getRaw(csvRecord, "GrossAmount")))
                    .setVatAmount(stringToDouble.apply(getRaw(csvRecord, "VatAmount")))
                    .setCurrencyCode(getRaw(csvRecord, "CurrencyCode"))
                    .setDebtCaseId(getRaw(csvRecord, "DebtCaseId"))
                    .setDebtorName(getRaw(csvRecord, "DebtorName"))
                    .setCollateralCity(getRaw(csvRecord, "CollateralCity"))
                    .setCollateralParcelNumber(getRaw(csvRecord, "CollateralParcelNumber"))
                    .setSapDocumentNumber(getRaw(csvRecord, "SapDocumentNumber"))
                    .setFulfillmentDate(getRaw(csvRecord, "FulfillmentDate"));

            // Line hozzáadása az aktuális headerhez
            currentHeader.getLines().add(line);

            // előző header ID frissítése
            previousHeaderId = headerIdRaw;
        }

        // visszatérés a teljes DTO-val
        return root;
    }

    /**
     * Biztonságosan olvassa ki egy CSV rekord adott mezőjének értékét.
     *
     * <p>
     * A metódus az alábbi esetekben {@code null}-t ad vissza:
     * <ul>
     *   <li>a megadott oszlop nem létezik a CSV header-ben,</li>
     *   <li>a rekord rövidebb, mint az elvárt oszlopindex,</li>
     *   <li>a mező értéke {@code null},</li>
     *   <li>a mező üres string vagy {@code "NULL"} (kis- és nagybetű független).</li>
     * </ul>
     * </p>
     *
     * <p>
     * Érvényes érték esetén a visszatérési érték trim-elt {@link String}.
     * </p>
     *
     * @param csvRecord az aktuális CSV rekord
     * @param column a kiolvasandó oszlop neve
     * @return a trim-elt mezőérték vagy {@code null}
     */

    private String getRaw(CSVRecord csvRecord, String column) {
        if (!csvRecord.isMapped(column)) {
            return null;
        }

        int index = csvRecord.getParser().getHeaderMap().get(column);

        if (index >= csvRecord.size()) {
            return null;
        }

        String value = csvRecord.get(index);
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty() || "NULL".equalsIgnoreCase(trimmed)) {
            return null;
        }

        return trimmed;
    }
}
