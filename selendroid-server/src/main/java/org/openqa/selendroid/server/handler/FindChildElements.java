/*
 * Copyright 2012 selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openqa.selendroid.server.handler;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.NoSuchElementException;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.exceptions.UnsupportedOperationException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.By;
import org.openqa.selendroid.server.model.internal.NativeAndroidBySelector;
import org.webbitserver.HttpRequest;

public class FindChildElements extends RequestHandler {

  public FindChildElements(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException{
    JSONObject payload = getPayload();
    String method = payload.getString("using");
    String selector = payload.getString("value");
    System.out.println(String.format("find child element command using %s with selector %s.",
        method, selector));
    Long elementId = getElementId();
    AndroidElement root = getElementFromCache(elementId);
    if (root == null) {
      return new Response(getSessionId(), 10, new SelendroidException("The element with Id: "
          + elementId + " was not found."));
    }
    By by = new NativeAndroidBySelector().pickFrom(method, selector);
    List<AndroidElement> elements = null;
    try {
      elements = root.findElements(by);
    } catch (NoSuchElementException e) {
      return new Response(getSessionId(), new JsonArray());
    }catch (UnsupportedOperationException e) {
      return new Response(getSessionId(), 32, e);
    }

    JSONArray result = new JSONArray();
    for (AndroidElement element : elements) {
      JSONObject jsonElement = new JSONObject();
      Long id = getIdOfKnownElement(element);
      if (id == null) {
        continue;
      }
      jsonElement.put("ELEMENT", id);
      result.put(jsonElement);
    }
    return new Response(getSessionId(), result);
  }
}
