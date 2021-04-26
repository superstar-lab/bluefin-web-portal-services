package com.mcmcg.ico.bluefin.service;

import com.mcmcg.ico.bluefin.dto.DeclinedTranSummaryDTO;
import com.mcmcg.ico.bluefin.model.ApprovedTranSummary;
import com.mcmcg.ico.bluefin.dto.TopTranSummaryDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TransactionSummaryService {
    List<DeclinedTranSummaryDTO> declinedSummary(String fromDate, String toDate);
    Map<String, List<ApprovedTranSummary>> approvedSummary(String fromDate, String toDate);
    Map<String, List<TopTranSummaryDTO>> topSummary(String top,String statusCode, String fromDate, String toDate);
    File tranSummaryReport(String top,String statusCode, String fromDate, String toDate) throws IOException;
}
