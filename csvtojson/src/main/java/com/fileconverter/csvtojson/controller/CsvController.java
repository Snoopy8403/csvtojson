package com.fileconverter.csvtojson.controller;

import com.fileconverter.csvtojson.model.CostAllocationModels.Root;
import com.fileconverter.csvtojson.service.CsvToJsonService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class CsvController {

    private final CsvToJsonService service;

    public CsvController(CsvToJsonService service) {
        this.service = service;
    }

    @PostMapping(
            value = "/convert",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Root convertCsv(@RequestParam("file") MultipartFile file) throws Exception {
        return service.convert(file);
    }
}
