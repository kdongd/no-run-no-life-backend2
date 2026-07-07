package com.kdongdexample.norunnolifeexample.exception;

public class InvalidPageSizeException extends RuntimeException {
    public InvalidPageSizeException(int requestedSize, int maxSize) {
        super("페이지 크기는 최대 " + maxSize + "까지 가능합니다. 요청값: " + requestedSize);
    }
}