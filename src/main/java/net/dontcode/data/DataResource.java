package net.dontcode.data;

import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.quarkus.mongodb.reactive.ReactiveMongoDatabase;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/data")
@ApplicationScoped
public class DataResource {
    private static Logger log = LoggerFactory.getLogger(DataResource.class);

    @Inject
    @MongoClientName("data")
    ReactiveMongoClient mongoClient;

    @ConfigProperty(name = "data-database-name")
    String dataDbName;

    @GET
    @Path("/{entityName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Document> listEntities (UriInfo info,@PathParam("entityName") String entityName, @RestHeader("DbName") String dbName) {
        log.debug("Hostname = {}, DbName Header = {}", info.getAbsolutePath(), dbName);
        Multi<Document> ret = getEntities(entityName, dbName).find().map(document -> {
            changeIdToString(document);
            return document;
        });
        return ret;
    }

    @GET
    @Path("/{entityName}/{entityId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Response> getEntity (@PathParam("entityName") String entityName, @PathParam("entityId") String entityId,  @HeaderParam("DbName") String dbName) {
        Uni<Response> ret = getEntities(entityName, dbName).find(new Document().append("_id", new ObjectId (entityId))).toUni().map(document -> {
           if( document != null) {
               changeIdToString(document);
               return Response.ok(document).build();
           } else {
               return Response.status(Response.Status.NOT_FOUND).build();
           }
        });
        return ret;
    }

    @PUT
    @Path("/{entityName}/{entityId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> updateProject (@PathParam("entityName") String entityName, @PathParam("entityId") String entityId, @HeaderParam("DbName") String dbName, Document body) {
        changeIdToObjectId(body);
        Uni<Response> ret = getEntities(entityName, dbName).findOneAndReplace(new Document().append("_id", body.get("_id")), body).map(document -> {
            if( document != null) {
                changeIdToString(document);
                return Response.ok(document).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        });
        return ret;
    }

    protected void changeIdToObjectId(Document body) {
        body.put("_id", new ObjectId(body.getString("_id")));
    }

    protected void changeIdToString(Document body) {
        body.put("_id", body.getObjectId("_id").toHexString());
    }

    @DELETE
    @Path("/{entityName}/{entityId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> deleteProject (@PathParam("entityName") String entityName, @PathParam("entityId") String entityId, @HeaderParam("DbName") String dbName) {
        Uni<Response> ret = getEntities(entityName, dbName).findOneAndDelete(new Document().append("_id", new ObjectId(entityId))).map(document -> {
            if( document != null) {
                changeIdToString(document);
                return Response.ok(document).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        });
        return ret;
    }

    @POST
    @Path("/{entityName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> insertProject(Document body, @PathParam("entityName") String entityName, @HeaderParam("DbName") String dbName) {
        //System.out.println("Received"+ body);
        return getEntities(entityName, dbName).insertOne(body).map(result -> {
            changeIdToString(body);
            return Response.ok(body).build();
        });
    }

    protected ReactiveMongoCollection<Document> getEntities(String entity, String dbName) {
        return getDatabase(dbName).getCollection(entity, Document.class);
    }

    protected ReactiveMongoDatabase getDatabase (String dbName) {
        if( dbName==null) dbName = dataDbName;
        return mongoClient.getDatabase(dbName);
    }
}
