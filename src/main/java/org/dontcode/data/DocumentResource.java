package org.dontcode.data;

import net.dontcode.core.store.UploadedDocumentInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;

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
    public Response receiveDocuments(FilesFormData filesForm) {

        ArrayList<UploadedDocumentInfo> resp = new ArrayList<UploadedDocumentInfo>();
        try {
        java.nio.file.Path documentDir = FileSystems.getDefault().getPath(docDir);
        String filePath;
            for (FileUpload file : filesForm.files) {
                filePath=file.uploadedFile().getFileName().toString()+'.'+file.contentType().substring(file.contentType().lastIndexOf('/')+1);
                Files.copy(file.uploadedFile(), documentDir.resolve(filePath));
                resp.add(new UploadedDocumentInfo(file.fileName(), true, docExternalUrl+'/'+ URLEncoder.encode( filePath, Charset.defaultCharset())));
            }
            return Response.ok().entity(resp).build();
        } catch (Exception e) {
            log.error("Error receiving documents {}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.ordinal(), e.getMessage()).build();
        }
    }

}