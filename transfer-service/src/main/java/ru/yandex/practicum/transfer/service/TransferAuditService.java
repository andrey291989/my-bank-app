package ru.yandex.practicum.transfer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.transfer.dto.TransferRequestDto;
import ru.yandex.practicum.transfer.model.TransferTransaction;
import ru.yandex.practicum.transfer.repository.TransferTransactionRepository;

/**
 * Аудит переводов. Запись о НЕуспешном переводе сохраняется в отдельной
 * транзакции ({@link Propagation#REQUIRES_NEW}), чтобы она не откатилась вместе
 * с основной транзакцией перевода при пробросе исключения.
 */
@Service
public class TransferAuditService {

    private final TransferTransactionRepository transactionRepository;

    public TransferAuditService(TransferTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedTransfer(TransferRequestDto request, String errorMessage) {
        TransferTransaction transaction = new TransferTransaction(
                request.fromLogin(),
                request.toLogin(),
                request.amount(),
                null, null, null, null,
                "FAILED"
        );
        transaction.setErrorMessage(errorMessage);
        transactionRepository.save(transaction);
    }
}
