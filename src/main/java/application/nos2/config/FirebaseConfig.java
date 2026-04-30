package application.nos2.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    FirebaseApp firebaseApp(FirebaseProperties properties) throws IOException {
        try (InputStream credentialsStream = resolveCredentials(properties.credentialsPath()).getInputStream()) {
            var credentials = GoogleCredentials.fromStream(credentialsStream);
            var options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(properties.projectId())
                    .build();

            return FirebaseApp.getApps().stream()
                    .findFirst()
                    .orElseGet(() -> FirebaseApp.initializeApp(options));
        }
    }

    @Bean
    FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    private Resource resolveCredentials(String credentialsPath) {
        Resource resource = resourceLoader.getResource(credentialsPath);
        if (!resource.exists() && !credentialsPath.contains(":")) {
            resource = resourceLoader.getResource("file:" + credentialsPath);
        }
        return resource;
    }
}
