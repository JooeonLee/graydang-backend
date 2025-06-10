package com.graydang.app.domain.bill.service;

import com.graydang.app.domain.bill.repository.BillReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BillReactionService {

    private final BillReactionRepository billReactionRepository;

    public long getBillReactionCountByUserId(Long userId) {
        return billReactionRepository.countByUserId(userId);
    }
}
