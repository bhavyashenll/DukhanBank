package com.example.api.account.service;

import com.example.api.account.domain.dto.StatementData;

import java.io.IOException;

public interface StatementExcelGenerationService {
    byte[] generateExcel(String segmentName, StatementData statementData) throws IOException;
}

