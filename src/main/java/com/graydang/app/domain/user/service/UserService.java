package com.graydang.app.domain.user.service;

import com.graydang.app.domain.bill.exception.BillException;
import com.graydang.app.domain.user.exception.UserException;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.repository.UserRepository;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public User findByIdOrThrow(Long id) {
        return  userRepository.findById(id).orElseThrow(() -> new UserException(BaseResponseStatus.NONE_USER));
    }
}
