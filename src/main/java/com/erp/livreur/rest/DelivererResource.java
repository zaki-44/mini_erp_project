package com.erp.livreur.rest;

import com.erp.livreur.entity.Deliverer;
import com.erp.livreur.service.DelivererService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/deliverers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DelivererResource {

    @Inject
    private DelivererService service;

    @GET
    public List<Deliverer> getAll() {
        return service.getAllDeliverers();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Integer id) {
        Deliverer d = service.getDeliverer(id);
        if (d == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(d).build();
    }

    @POST
    public Response create(Deliverer d) {
        Deliverer created = service.addDeliverer(d);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, Deliverer d) {
        Deliverer existing = service.getDeliverer(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        d.setId(id);
        Deliverer updated = service.updateDeliverer(d);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        Deliverer existing = service.getDeliverer(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        service.deleteDeliverer(id);
        return Response.noContent().build();
    }

    // New POST endpoint for changing availability
    @POST
    @Path("/{id}/availability")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response changeAvailability(@PathParam("id") Integer id, String availableStr) {
        Deliverer d = service.getDeliverer(id);
        if (d == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        boolean available = Boolean.parseBoolean(availableStr);
        service.changeAvailability(id, available);
        return Response.ok().build();
    }
}
