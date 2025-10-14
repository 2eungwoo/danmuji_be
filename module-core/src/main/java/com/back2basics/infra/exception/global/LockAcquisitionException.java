package com.back2basics.infra.exception.global;

import com.back2basics.global.response.error.CustomException;

public class LockAcquisitionException extends CustomException {
    public LockAcquisitionException() {
        super(GlobalErrorCode.LOCK_ACQUISITION_FAILED);
    }
}
