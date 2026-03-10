package com.tripmoa.community.mate.Exception;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;

public class InvalidBudgetException extends BusinessException {

    public InvalidBudgetException(String message) {
        super(ErrorCode.INVALID_BUDGET, message);
    }

    public InvalidBudgetException() {
        super(ErrorCode.INVALID_BUDGET);
    }
}

