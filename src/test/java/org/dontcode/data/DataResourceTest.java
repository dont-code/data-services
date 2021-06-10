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
        String entityName="TestEntity";
        String otherEntityName="OtherTestEntity";
        Document doc = new Document();
        AtomicReference<Throwable> error = new AtomicReference<>();

        doc.append("name","TestProject1").append("creation", new Date());
        removeEntities(entityName);
        removeEntities(otherEntityName);
        getEntities(entityName).insertOne(doc).onFailure().invoke(throwable -> {
            error.set(throwable);
        }).await().atMost(Duration.ofSeconds(10));
        doc.put("name","TestProject2");
        doc.remove("_id");
        getEntities(entityName).insertOne(doc).onFailure().invoke(throwable -> {
            error.set(throwable);
        }).await().atMost(Duration.ofSeconds(10));
        doc.put("name","OtherTestProject");
        doc.remove("_id");
        getEntities(otherEntityName).insertOne(doc).onFailure().invoke(throwable -> {
            error.set(throwable);
        }).await().atMost(Duration.ofSeconds(10));

        Throwable isError = error.get();
        String errorMessage=(isError!=null)? isError.getMessage() : "";

        Assertions.assertNull(isError, "Error writing test data to Mongo "+errorMessage);
        given().accept(ContentType.JSON).when().get("/{entityName}",entityName).then().statusCode(HttpStatus.SC_OK)
                .body("[0].name", Matchers.equalTo("TestProject1")).body( "[1].name", Matchers.equalTo("TestProject2") );

        given().accept(ContentType.JSON).when().get("/{entityName}",otherEntityName).then().statusCode(HttpStatus.SC_OK)
                .body("[0].name", Matchers.equalTo("OtherTestProject") );
    }

    @Test
    public void testCreateAndRead () {
        String entityName="TestEntityCreate";

        removeEntities(entityName);
        Document resp = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{" +
                "\"name\":\"PrjCreated1\"," +
                "\"creation\":\"2021-03-04\"" +
                "}").when().post("/{entityName}", entityName).then().statusCode(HttpStatus.SC_OK)
                .body("_id", Matchers.notNullValue() )
                .and().extract().as(Document.class);


        given().accept(ContentType.JSON).when().get("/{entityName}/{entityId}",entityName, resp.get("_id").toString()).then().statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.is("PrjCreated1"));
    }

    @Test
    public void testNotFound () {
        String entityName="TestEntityCreate";
        removeEntities(entityName);
        given().accept(ContentType.JSON).when().get("/{entityName}/{entityId}",entityName, "60b7b06ba5f1da79a15dc448").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCompleteFlow () {
        String entityName="TestCompleteFlow";

        removeEntities(entityName);
        Document created = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{" +
                "\"name\":\"PrjCreated2\"," +
                "\"creation\":\"2021-05-05\"" +
                "}").when().post("/{entityName}", entityName).then().statusCode(HttpStatus.SC_OK)
                .body("_id", Matchers.notNullValue() )
                .and().extract().as(Document.class);
        String entityId = created.get("_id").toString();

        given().accept(ContentType.JSON).when().get("/{entityName}/{entityId}", entityName, entityId).then().statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.is("PrjCreated2"));

        Document updated = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{" +
                "\"_id\":\""+entityId+"\","+
                "\"name\":\"PrjUpdated2\"," +
                "\"creation\":\"2021-06-07\"" +
                "}").when().put("/{entityName}/{entityId}",entityName, entityId).then().statusCode(HttpStatus.SC_OK)
                .body("_id", Matchers.notNullValue() )
                .and().extract().as(Document.class);
        Assertions.assertEquals(entityId, updated.get("_id").toString());

        given().accept(ContentType.JSON).when().get("/{entityName}/{entityId}",entityName, entityId).then().statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.is("PrjUpdated2"))
                .body("_id", Matchers.equalTo(created.get("_id")));

        given().accept(ContentType.JSON).when().delete("/{entityName}/{entityId}",entityName, entityId).then().statusCode(HttpStatus.SC_OK);
        given().accept(ContentType.JSON).when().get("/{entityName}/{entityId}",entityName, entityId).then().statusCode(HttpStatus.SC_NOT_FOUND);
    }


    protected ReactiveMongoCollection<Document> getEntities(String entityName) {
        return mongoClient.getDatabase("unitTestDataDb").getCollection(entityName);
    }
    protected void removeEntities(String entityName) {
        if( mongoClient.getDatabase("unitTestDataDb").listCollectionNames().collect().asList().await().indefinitely().contains("data-"+entityName))
            mongoClient.getDatabase("unitTestDataDb").getCollection(entityName).drop().await().indefinitely();
    }

}
