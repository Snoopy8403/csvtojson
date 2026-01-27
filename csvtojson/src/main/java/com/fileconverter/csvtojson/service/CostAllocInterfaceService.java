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
 * Service a CSV -> JSON konverzióhoz a Cost Allocation Interface DTO-ra.
 * Biztonságosan parse-olja a numerikus mezőket NumberUtils segítségével,
 * String mezőknél pedig a getRaw() null-safe logikáját használja.
 */
@Slf4j
@Service
public class CostAllocInterfaceService {

    // Numerikus String → Long konverzió biztonságos ellenőrzéssel
    private final Function<String, Long> stringToLong = text -> {
        if (!NumberUtils.isCreatable(text)) {
            log.error(
                    "Invalid numeric value encountered during parsing to Long: {}" + text
            );
            throw new IllegalArgumentException("Invalid numeric value: " + text);
        }
        return NumberUtils.createNumber(text).longValue();
    };

    // Numerikus String → Double konverzió biztonságos ellenőrzéssel
    private final Function<String, Double> stringToDouble = text-> {
        if (!NumberUtils.isCreatable(text)) {
            log.error(
                    "Invalid numeric value encountered during parsing to Double: {}" + text
            );
            throw new IllegalArgumentException("Invalid numeric value: " + text);
        }
        return NumberUtils.createNumber(text).doubleValue();
    };

    /**
     * CSV fájl feldolgozása és DTO struktúrába töltése
     * @param file feltöltött CSV fájl
     * @return Root DTO a feldolgozott adatokkal
     * @throws Exception fájl olvasási hiba esetén
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
                        .setCostAllocationIdentifier(stringToLong.apply(getRaw(csvRecord, "CostAllocationIdentifier")))
                        .setCostAllocationTypeCode(stringToLong.apply(getRaw(csvRecord, "CostAllocationTypeCode")))
                        .setStatusCode(stringToLong.apply(getRaw(csvRecord, "StatusCode")))
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

                    .setCostAllocationLineIdentifier(stringToLong.apply(getRaw(csvRecord, "CostAllocationLineIdentifier")))
                    .setOriginalCostAllocationLineId(stringToLong.apply(getRaw(csvRecord, "OriginalCostAllocationLineID")))
                    .setStatusCode(stringToLong.apply(getRaw(csvRecord, "StatusCode")))
                    .setGrossAmount(stringToDouble.apply(getRaw(csvRecord, "GrossAmount")))
                    .setVatAmount(stringToDouble.apply(getRaw(csvRecord, "VatAmount")))
                    .setCurrencyCode(getRaw(csvRecord, "CurrencyCode"))
                    .setDebtCaseId(stringToLong.apply(getRaw(csvRecord, "DebtCaseId")))
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
     * Biztonságosan olvassa ki a CSV mezőt:
     * - header nincs → null
     * - rekord rövidebb → null
     * - üres / "NULL" → null
     * - trim-elés után adja vissza a Stringet
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
