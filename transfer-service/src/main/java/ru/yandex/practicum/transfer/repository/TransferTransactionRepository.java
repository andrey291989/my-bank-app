package ru.yandex.practicum.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.transfer.model.TransferTransaction;

import java.util.List;

@Repository
public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long> {
    List<TransferTransaction> findByFromLoginOrderByCreatedAtDesc(String fromLogin);
    List<TransferTransaction> findByToLoginOrderByCreatedAtDesc(String toLogin);
}