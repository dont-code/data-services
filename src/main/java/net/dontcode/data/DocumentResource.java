package net.dontcode.data;

import net.dontcode.core.store.UploadedDocumentInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;

@Path("/documents")
@ApplicationScoped
public class DocumentResource {
    private static Logger log = LoggerFactory.getLogger(DocumentResource.class);

    @ConfigProperty(name = "document-directory")
    String docDir;

    @ConfigProperty(name = "document-external-url")
    String docExternalUrl;

    @POST
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveDocuments(@RestForm(FileUpload.ALL) List<FileUpload> filesForm) {
        log.debug ("Receiving documents");
        ArrayList<UploadedDocumentInfo> resp = new ArrayList<UploadedDocumentInfo>();
        try {
        java.nio.file.Path documentDir = FileSystems.getDefault().getPath(docDir);
        Files.createDirectories(documentDir);
        String filePath;
            for (FileUpload file : filesForm) {
                filePath=file.uploadedFile().getFileName().toString()+'.'+file.contentType().substring(file.contentType().lastIndexOf('/')+1);
                java.nio.file.Path destPath = documentDir.resolve(filePath);
                Files.copy(file.uploadedFile(), destPath, StandardCopyOption.REPLACE_EXISTING);
                Files.setPosixFilePermissions(destPath, PosixFilePermissions.fromString("rwxr-xr-x"));
                resp.add(new UploadedDocumentInfo(file.fileName(), true, docExternalUrl+'/'+ URLEncoder.encode( filePath, Charset.defaultCharset())));
                log.debug ("Received document {} to url {}", file.fileName(), docExternalUrl+'/'+ URLEncoder.encode( filePath, Charset.defaultCharset()));
            }
            return Response.ok().entity(resp).build();
        } catch (Exception e) {
            log.error("Error receiving documents {}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.ordinal(), e.getMessage()).build();
        }
    }

}
