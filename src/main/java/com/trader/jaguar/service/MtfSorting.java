package com.trader.jaguar.service;

import com.trader.jaguar.model.data.Leverage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MtfSorting {

    List<Leverage> csvToMtf(MultipartFile file, short percent) throws IOException;

    List<Map<String, Object>> getEtfMargin(MultipartFile file) throws IOException;
}
