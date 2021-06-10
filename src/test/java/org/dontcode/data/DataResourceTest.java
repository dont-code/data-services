package org.dontcode.data;

import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.dontcode.prj.MongoTestProfile;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(org.dontcode.data.DataResource.class)
@TestProfile(MongoTestProfile.class)
public class DataResourceTest {

    @Inject
    @MongoClientName("data")
    ReactiveMongoClient mongoClient;

    @Test
    public void testList () {
/*        Document doc = new Document();
        AtomicReference<Throwable> error = new AtomicReference<>();

        doc.append("name","TestProject1").append("creation", new Date());
        removeProjects();
        getProjects().insertOne(doc).onFailure().invoke(throwable -> {
            error.set(throwable);
        }).await().atMost(Duration.ofSeconds(10));

        Throwable isError = error.get();
        String errorMessage=(isError!=null)? isError.getMessage() : "";

        Assertions.assertNull(isError, "Error writing test data to Mongo "+errorMessage);
        given().accept(ContentType.JSON).when().get().then().statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.hasItem("TestProject1") );*/
    }

    @Test
    public void testCreateAndRead () {
/*
        removeProjects();
        given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{" +
                "\"name\":\"PrjCreated1\"," +
                "\"creation\":\"2021-03-04\"" +
                "}").when().post().then().statusCode(HttpStatus.SC_OK)
                .body("_id", Matchers.notNullValue() )
                .and().extract().as(Document.class);


        given().accept(ContentType.JSON).when().get("/{prjName}","PrjCreated1").then().statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.is("PrjCreated1"));*/
    }

    @Test
    public void testNotFound () {
/*        removeProjects();
        given().accept(ContentType.JSON).when().get("/{prjName}","NonExistingProject").then().statusCode(HttpStatus.SC_NOT_FOUND);*/
    }

    @Test
    public void testCompleteFlow () {
/*
        removeProjects();
        Document created = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{" +
                "\"name\":\"PrjCreated2\"," +
                "\"creation\":\"2021-05-05\"" +
                "}").when().post().then().statusCode(HttpStatus.SC_OK)
                .body("_id", Matchers.notNullValue() )
                .and().extract().as(Document.class);

        given().accept(ContentType.JSON).when().get("/{prjName}","PrjCreated2").then().statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.is("PrjCreated2"));

        Document updated = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{" +
                "\"_id\":\""+created.getString("_id")+"\","+
                "\"name\":\"PrjUpdated2\"," +
                "\"creation\":\"2021-06-07\"" +
                "}").when().put("/{prjName}","PrjCreated2").then().statusCode(HttpStatus.SC_OK)
                .body("_id", Matchers.notNullValue() )
                .and().extract().as(Document.class);
        Assertions.assertEquals(created.get("_id"), updated.get("_id"));

        given().accept(ContentType.JSON).when().get("/{prjName}","PrjUpdated2").then().statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.is("PrjUpdated2"))
                .body("_id", Matchers.equalTo(created.get("_id")));

        given().accept(ContentType.JSON).when().get("/{prjName}","PrjCreated2").then().statusCode(HttpStatus.SC_NOT_FOUND);
        given().accept(ContentType.JSON).when().delete("/{prjName}","PrjUpdated2").then().statusCode(HttpStatus.SC_OK);

        given().accept(ContentType.JSON).when().get("/{prjName}","PrjUpdated2").then().statusCode(HttpStatus.SC_NOT_FOUND);*/
    }


    protected ReactiveMongoCollection<Document> getEntities(String entityName) {
        return mongoClient.getDatabase("unitTestDataDb").getCollection("data-"+entityName);
    }
    protected void removeEntities(String entityName) {
        if( mongoClient.getDatabase("unitTestDataDb").listCollectionNames().collect().asList().await().indefinitely().contains("data-"+entityName))
            mongoClient.getDatabase("unitTestDataDb").getCollection("data-"+entityName).drop().await().indefinitely();
    }

}
