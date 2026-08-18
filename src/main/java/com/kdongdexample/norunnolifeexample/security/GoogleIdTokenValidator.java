package com.kdongdexample.norunnolifeexample.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.kdongdexample.norunnolifeexample.exception.InvalidGoogleTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleIdTokenValidator {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenValidator(@Value("${google.client-id}") String googleClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidGoogleTokenException();
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidGoogleTokenException();
        }
    }
}
