package zhuang.jesse.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import zhuang.jesse.config.AppConfig;
import zhuang.jesse.constants.GoogleConstants;
import zhuang.jesse.constants.MailChimpConstants;
import zhuang.jesse.util.FileUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class GoogleDoc {

    private static Drive service;

    private static Properties properties = FileUtils.loadProperties(GoogleConstants.PROPERTIES_FILE_PATH);

    private static final Logger LOGGER = Logger.getLogger(GoogleDoc.class.getName());

    static {
        try {
            service = getDriveService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build and return an authorized Drive client service.
     *
     * @return an authorized Drive client service
     * @throws IOException read json secrect in error
     */
    private static Drive getDriveService() throws IOException {
        Credential credential = GetAuthCred.authorize();
        return new Drive.Builder(
                GetAuthCred.HTTP_TRANSPORT, GetAuthCred.JSON_FACTORY, credential)
                .setApplicationName(GetAuthCred.APPLICATION_NAME)
                .build();
    }

    private void quickstart() throws IOException {
        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list().setOrderBy("folder,modifiedTime desc,name")
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.size() == 0) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }

    public String uploadFile() throws IOException {
        String thisYear = String.valueOf(LocalDate.now().getYear());

        // https://developers.google.com/drive/v3/web/mime-types
        File fileMetadata = new File();
        fileMetadata.setName(MailChimpConstants.CAMPAIGN_TITLE);
        fileMetadata.setMimeType("application/vnd.google-apps.file");
        String folderId = GoogleConstants.properties.getProperty(thisYear + ".folderId");
        if (folderId == null) folderId = createFolder(thisYear);
        fileMetadata.setParents(Collections.singletonList(folderId));

        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
        java.io.File filePath = new java.io.File("io/forGoogleDoc.txt");
        FileContent mediaContent = new FileContent("text/plain", filePath);
        File file = service.files().create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute();
        LOGGER.info("upload google doc succeeded for file " + file.getId() + " to folder " + thisYear);

        return file.getId();
    }

    public void createPermission(String fileId) throws IOException {
        Permission permission = new Permission();
        permission.setType("user").setRole("commenter").setEmailAddress(properties.getProperty("mitchair.email"));
        Drive.Permissions.Create share = service.permissions().create(fileId, permission).
                setSendNotificationEmail(true).setEmailMessage("ready for review.");
        String requestId = share.execute().getId();
        LOGGER.info("Sahred google doc with MIT chair, " + requestId);
    }

    private String findThisYearFolder() throws IOException {
        String thisYear = String.valueOf(LocalDate.now().getYear());
        FileList result = service.files().list().setQ("name='" + thisYear + "'").execute();
        List<File> files = result.getFiles();
        if (files == null) throw new RuntimeException("file search result is null");
        if (files.size() > 1) {
            LOGGER.log(Level.SEVERE, files.size() + " folders found for name " + thisYear);
            throw new RuntimeException("There should be only one folder with name " + thisYear);
        }
        if (files.size() == 0) createFolder(thisYear);

        return files.get(0).getId();
    }

    private String createFolder(String folderName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = service.files().create(fileMetadata)
                .setFields("id")
                .execute();

        properties.setProperty(folderName + ".folderId", file.getId());
        FileUtils.writeProperties(GoogleConstants.PROPERTIES_FILE_PATH, properties);
        LOGGER.info("Created folder " + folderName + " with id " + file.getId());
        return file.getId();
    }

    public void wholeJob() throws IOException {
        createPermission(uploadFile());
    }

    public static void main(String[] args) throws IOException {

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        GoogleDoc gd = context.getBean(GoogleDoc.class);

//        gd.quickstart();
//        gd.uploadFile();
//        gd.createPermission("1ytcOZNGEitA2vP90c-7OSM9F69J7eNaksjJZ1Kcea6U");
//        gd.findThisYearFolder();
//        gd.createFolder("test_folder");
//        gd.wholeJob();
    }

}
