package org.dontcode.data;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.List;

public class FilesFormData {
        @RestForm()
        public List<FileUpload> files;
}
