package com.tripmoa.community.mate.Exception;

public class InvalidBudgetException extends MateException {

    public InvalidBudgetException(String message) {
        super(ErrorCode.INVALID_BUDGET, message);
    }

    public InvalidBudgetException() {
        super(ErrorCode.INVALID_BUDGET);
    }
}

