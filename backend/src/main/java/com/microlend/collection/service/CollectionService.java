package com.microlend.collection.service;

import com.microlend.collection.dto.CollectionRecordResponse;
import com.microlend.collection.dto.CollectionRequest;
import com.microlend.collection.dto.ReceiptResponse;

import java.util.List;


public interface CollectionService {

    CollectionRecordResponse record(Long officerId, CollectionRequest req);

    List<CollectionRecordResponse> listForOfficer(Long officerId);

    List<ReceiptResponse> borrowerReceipts(Long borrowerUserId, boolean pendingOnly);

    ReceiptResponse approve(Long borrowerUserId, Long receiptId);

    ReceiptResponse dispute(Long borrowerUserId, Long receiptId, String remarks);

    List<ReceiptResponse> branchDisputes(Long managerUserId);

    ReceiptResponse coSign(Long actorUserId, Long receiptId, String justification);
}
