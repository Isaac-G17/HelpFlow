package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.PageResult;
import com.helpflow.ticket.domain.model.User;
import com.helpflow.ticket.domain.model.enums.AccountStatus;

public interface ManageUserUseCase {

    User getProfile(Long userId);

    User updateProfile(Long userId, UpdateProfileCommand command);

    PageResult<User> list(PageQuery pageQuery);

    User changeStatus(Long userId, AccountStatus status);

    record UpdateProfileCommand(String name, String sede, String area) {
    }
}
