package com.kdongdexample.norunnolifeexample.exception;

public class NicknameAlreadyExistsException extends RuntimeException {
    public static final String MESSAGE = "이미 사용 중인 닉네임입니다";

    public NicknameAlreadyExistsException() {
        super(MESSAGE);
    }
}
