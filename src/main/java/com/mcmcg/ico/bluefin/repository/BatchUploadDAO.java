package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.mcmcg.ico.bluefin.model.BatchUpload;

public interface BatchUploadDAO {
    BatchUpload saveBasicBatchUpload(BatchUpload batchUpload);

    List<BatchUpload> findAll();

    BatchUpload findOne(Long id);

    List<BatchUpload> findByDateUploadedAfter(DateTime dateBeforeNoofdays);

    Page<BatchUpload> findByDateUploadedAfterOrderByDateUploadedDesc(DateTime dateBeforeNoofdays, Pageable pageRequest);

    Page<BatchUpload> findAllByOrderByDateUploadedDesc(PageRequest pageRequest);
}
