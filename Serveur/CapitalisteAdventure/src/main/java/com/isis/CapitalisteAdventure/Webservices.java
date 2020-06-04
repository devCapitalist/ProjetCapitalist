package com.isis.CapitalisteAdventure;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;


@Path("generic")
    public class Webservices {
        Services services;
        public Webservices() {
            services = new Services();
        }
        @GET
        @Path("world")
        @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public Response getWorld() throws JAXBException {
            return Response.ok(services.getWorld()).build();
        }

    }
