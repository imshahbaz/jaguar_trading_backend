package com.trader.jaguar.service.impl;

import com.trader.jaguar.constants.Constants;
import com.trader.jaguar.model.data.Leverage;
import com.trader.jaguar.model.request.FilteredStocks;
import com.trader.jaguar.service.MtfSorting;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
public class MtfSortingImpl implements MtfSorting {

    @Override
    public Map<String, Double> angelOneMtfSort(MultipartFile data) {
        try {
            Workbook workbook = new XSSFWorkbook(data.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            List<FilteredStocks> list = new ArrayList<>();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber <= 1) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();
                FilteredStocks stock = FilteredStocks.builder().build();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 1 -> stock.setName(currentCell.getStringCellValue());
                        case 2 -> stock.setSymbol(currentCell.getStringCellValue());
                        case 4 -> stock.setChange(currentCell.getNumericCellValue());
                        case 5 -> stock.setPrice(currentCell.getNumericCellValue());
                        case 6 -> stock.setVolume(currentCell.getNumericCellValue());
                    }
                    cellIdx++;
                }
                list.add(stock);
            }
            workbook.close();
            Map<String, Double> response = new HashMap<>();

            list.parallelStream().forEach(com->{
                if (Constants.angelOne.getMtf().containsKey(com.getSymbol())){
                    Leverage leverage = Constants.angelOne.getMtf().get(com.getSymbol());
                    response.put(leverage.getSymbol(), leverage.getPercent());
                }
            });
            return response;

        } catch (Exception e) {
            log.error("{}", e.toString());
            return null;
        }

    }
}
