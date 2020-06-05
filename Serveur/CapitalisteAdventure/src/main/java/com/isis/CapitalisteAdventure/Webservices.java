package com.isis.CapitalisteAdventure;

import com.isis.CapitalisteAdventure.generated.PallierType;
import com.isis.CapitalisteAdventure.generated.ProductType;
import com.isis.CapitalisteAdventure.generated.ProductsType;
import com.isis.CapitalisteAdventure.generated.World;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
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
        public Response getWorld(@Context HttpServletRequest request) throws JAXBException, FileNotFoundException {
            String username = request.getHeader("X-user");
            World world = services.getWorld(username);
            services.saveWorldToXml(username, world);
            return Response.ok(world).build();
        }

        @PUT
        @Path("product")
        @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public Response putProduct(@Context HttpServletRequest request, ProductType product) throws JAXBException, FileNotFoundException {
            if(services.updateProduct(request.getHeader("X-user"), product)){
                return Response.ok("").build();
            }
            else {
                return Response.notModified().build();
            }
        }

        @PUT
        @Path("manager")
        @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public Response putManager(@Context HttpServletRequest request, PallierType manager) throws JAXBException, FileNotFoundException {
            if(services.updateManager(request.getHeader("X-user"), manager)){
                return Response.ok("").build();
            }
            else {
                return Response.notModified().build();
            }
        }
}
