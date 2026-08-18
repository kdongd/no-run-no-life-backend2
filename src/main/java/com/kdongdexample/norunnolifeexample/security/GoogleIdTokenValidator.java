package com.kdongdexample.norunnolifeexample.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.kdongdexample.norunnolifeexample.exception.InvalidGoogleTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleIdTokenValidator {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenValidator(@Value("${google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            // 구글 라이브러리는 토큰 형식이 잘못됐을 때 IllegalArgumentException(unchecked)을 던지기도 하고,
            // 서명/네트워크 문제일 땐 GeneralSecurityException, IOException(checked)을 던지기도 함.
            // 원인이 뭐가 됐든 "유효하지 않은 구글 토큰"으로 취급해서 401로 응답해야 하므로 넓게 catch.
            throw new InvalidGoogleTokenException();
        }

        if (idToken == null) {
            throw new InvalidGoogleTokenException();
        }

        return idToken.getPayload();
    }
}
