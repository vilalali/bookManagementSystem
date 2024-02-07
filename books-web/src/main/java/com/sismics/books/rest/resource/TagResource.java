package com.sismics.books.rest.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.TagDao;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Tag REST resources.
 * 
 * @author bgamard
 */
@Path("/tag")
public class TagResource extends BaseResource {
    /**
     * Returns the list of all tags.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        TagDao tagDao = new TagDao();
        List<Tag> tagList = tagDao.getByUserId(principal.getId());
        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<>();
        for (Tag tag : tagList) {
            JSONObject item = new JSONObject();
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            item.put("color", tag.getColor());
            items.add(item);
        }
        response.put("tags", items);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Creates a new tag.
     * 
     * @param name Name
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("name") String name,
            @FormParam("color") String color) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 36, false);
        ValidationUtil.validateHexColor(color, "color", true);
        
        // Don't allow spaces
        if (name.contains(" ")) {
            throw new ClientException("SpacesNotAllowed", "Spaces are not allowed in tag name");
        }
        
        // Get the tag
        TagDao tagDao = new TagDao();
        Tag tag = tagDao.getByName(principal.getId(), name);
        if (tag != null) {
            throw new ClientException("AlreadyExistingTag", MessageFormat.format("Tag already exists: {0}", name));
        }
        
        // Create the tag
        tag = new Tag();
        tag.setName(name);
        tag.setColor(color);
        tag.setUserId(principal.getId());
        String tagId = tagDao.create(tag);
        
        JSONObject response = new JSONObject();
        response.put("id", tagId);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Update a tag.
     * 
     * @param name Name
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("color") String color) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 36, true);
        ValidationUtil.validateHexColor(color, "color", true);
        
        // Don't allow spaces
        if (name.contains(" ")) {
            throw new ClientException("SpacesNotAllowed", "Spaces are not allowed in tag name");
        }
        
        // Get the tag
        TagDao tagDao = new TagDao();
        Tag tag = tagDao.getByTagId(principal.getId(), id);
        if (tag == null) {
            throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", id));
        }
        
        // Check for name duplicate
        Tag tagDuplicate = tagDao.getByName(principal.getId(), name);
        if (tagDuplicate != null && !tagDuplicate.getId().equals(id)) {
            throw new ClientException("AlreadyExistingTag", MessageFormat.format("Tag already exists: {0}", name));
        }
        
        // Update the tag
        if (!StringUtils.isEmpty(name)) {
            tag.setName(name);
        }
        if (!StringUtils.isEmpty(color)) {
            tag.setColor(color);
        }
        
        JSONObject response = new JSONObject();
        response.put("id", id);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Delete a tag.
     * 
     * @param tagId Tag ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String tagId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the tag
        TagDao tagDao = new TagDao();
        Tag tag = tagDao.getByTagId(principal.getId(), tagId);
        if (tag == null) {
            throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
        }
        
        // Delete the tag
        tagDao.delete(tagId);
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
