package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.dto.DeclinedTranSummaryDTO;
import com.mcmcg.ico.bluefin.model.ApprovedTranSummary;
import com.mcmcg.ico.bluefin.dto.TopTranSummaryDTO;

import java.util.List;

public interface TransactionSummaryDAO {
    List<DeclinedTranSummaryDTO> declinedSummary(String fromDate, String toDate, String statusCode);
    List<ApprovedTranSummary> approvedSummary(String fromDate, String toDate);
    List<TopTranSummaryDTO> topSummary(String top,String statusCode, String fromDate, String toDate);
}
