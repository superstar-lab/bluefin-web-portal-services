package com.mcmcg.ico.bluefin.persistent.jpa;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.BatchUpload;

public interface BatchUploadRepository extends JpaRepository<BatchUpload, Long> {

    public Page<BatchUpload> findByDateUploadedAfter(DateTime dateBeforeNoofdays, Pageable pageRequest);

    public List<BatchUpload> findByDateUploadedAfter(DateTime dateBeforeNoofdays);

}
