package net.dontcode.data;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestHTTPEndpoint(DocumentResource.class)
public class DocumentResourceTest {


    @Test
    public void testStoreDocuments () {
        String file1Content = "Pdf Test 1";
        String file2Content = "Png Test 1";

        given().contentType(ContentType.MULTIPART).accept(ContentType.JSON)
                .multiPart("file#1", "document1.pdf", file1Content.getBytes(StandardCharsets.UTF_8),"application/pdf")
                .multiPart("file#2", "document2.png", file2Content.getBytes(StandardCharsets.UTF_8),"image/png")
                .when().post().then().statusCode(HttpStatus.SC_OK)
                .and ().body("size()", is (2));

    }


}
