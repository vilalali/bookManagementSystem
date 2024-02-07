package com.sismics.books.rest;

import com.sismics.books.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

/**
 * Test the app resource.
 * 
 * @author jtremeaux
 */
public class TestAppResource extends BaseJerseyTest {
    /**
     * Test the app resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testAppResource() throws JSONException {
        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Check the application info
        WebResource appResource = resource().path("/app");
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        ClientResponse response = appResource.get(ClientResponse.class);
        response = appResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertNotNull(json.getString("current_version"));
        Assert.assertNotNull(json.getString("min_version"));
        Assert.assertNotNull(json.getString("api_key_google"));
        Long freeMemory = json.getLong("free_memory");
        Assert.assertTrue(freeMemory > 0);
        Long totalMemory = json.getLong("total_memory");
        Assert.assertTrue(totalMemory > 0 && totalMemory > freeMemory);
        
        // Update config
        appResource = resource().path("/app");
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("api_key_google", json.getString("api_key_google"));
        response = appResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
    }

    /**
     * Test the log resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testLogResource() throws JSONException {
        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Check the logs (page 1)
        WebResource appResource = resource()
                .path("/app/log")
                .queryParam("level", "DEBUG");
        ClientResponse response = appResource.get(ClientResponse.class);
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = appResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        JSONArray logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() > 0);
        Long date1 = logs.optJSONObject(0).optLong("date");
        Long date2 = logs.optJSONObject(9).optLong("date");
        Assert.assertTrue(date1 > date2);
        
        // Check the logs (page 2)
        appResource = resource()
                .path("/app/log")
                .queryParam("offset",  "10")
                .queryParam("level", "DEBUG");
        response = appResource.get(ClientResponse.class);
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = appResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() > 0);
        Long date3 = logs.optJSONObject(0).optLong("date");
        Long date4 = logs.optJSONObject(9).optLong("date");
        Assert.assertTrue(date3 > date4);
    }
}