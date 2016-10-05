package com.mcmcg.ico.bluefin.service;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.BatchUpload;
import com.mcmcg.ico.bluefin.persistent.jpa.BatchUploadRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

@Service
@Transactional
public class BatchUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchUploadService.class);

    @Autowired
    private BatchUploadRepository batchUploadRepository;

    public BatchUpload getBatchUploadById(Long id) {
        BatchUpload batchUpload = batchUploadRepository.findOne(id);

        if (batchUpload == null) {
            LOGGER.error("Unable to find batch upload with id = {}", id);
            throw new CustomNotFoundException(String.format("Unable to find batch upload with id = [%s]", id));
        }

        return batchUpload;
    }

    public Iterable<BatchUpload> getAllBatchUploads(Integer page, Integer size, String sort) {
        Page<BatchUpload> result = batchUploadRepository.findAll(QueryDSLUtil.getPageRequest(page, size, sort));
        if (page > result.getTotalPages() && page != 0) {
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
    }
  
}
