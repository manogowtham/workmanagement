package com.banyan.workmanagement.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    @Value("${google.drive.credentials.file}")
    private String credentialsFilePath;

    @Value("${google.drive.application.name}")
    private String applicationName;

    private Credential getCredentials() throws IOException, GeneralSecurityException {
        InputStream in = GoogleDriveService.class.getResourceAsStream(credentialsFilePath);
        if (in == null) {
            throw new IOException("Resource not found: " + credentialsFilePath);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public String uploadFile(java.io.File filePath, String mimeType) throws IOException, GeneralSecurityException {
        Drive service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials())
                .setApplicationName(applicationName)
                .build();

        File fileMetadata = new File();
        fileMetadata.setName(filePath.getName());

        FileContent mediaContent = new FileContent(mimeType, filePath);

        File file = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return file.getId();
    }
}
