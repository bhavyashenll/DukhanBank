package com.example.api.account.service;

import com.example.api.account.domain.dto.StatementData;

import java.io.IOException;

public interface StatementPdfGenerationService {
    byte[] generatePdf(String segmentName, StatementData statementData) throws IOException;
}

