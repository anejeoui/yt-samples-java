/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Demo of subscribing user to a channel using the YouTube Data API (V3) with OAuth2 for
 * authorization.
 *
 * @author Ibrahim Ulukaya
 */
public class AddSubscription {

    /**
     * Global instance of Youtube object to make all API requests.
     */
    private static YouTube youtube;

    /**
     * Subscribes user's YouTube account to a user selected channel using OAuth2 for authentication.
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) {

        // An OAuth 2 access scope that allows for full read/write access.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorization.

            Credential credential = Auth.authorize(scopes, "addsubscription");

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                    "youtube-cmdline-addsubscription-sample").build();

            // We get the user selected channel to subscribe.
            String channelId = getChannelId();
            System.out.println("You chose " + channelId + " to subscribe.");

            // We create a resourceId with channel id.
            ResourceId resourceId = new ResourceId();
            resourceId.setChannelId(channelId);
            resourceId.setKind("youtube#channel");

            // We create a snippet with ResourceId.
            SubscriptionSnippet snippet = new SubscriptionSnippet();
            snippet.setResourceId(resourceId);

            // We create a subscription request with snippet.
            Subscription subscription = new Subscription();
            subscription.setSnippet(snippet);

      /*
       * The subscription insert command includes: 1. Information we want returned after file is
       * successfully uploaded. 2. Subscription metadata we want to insert.
       */
            YouTube.Subscriptions.Insert subscriptionInsert =
                    youtube.subscriptions().insert("snippet,contentDetails", subscription);

            // Execute subscription.
            Subscription returnedSubscription = subscriptionInsert.execute();

            // Print out returned results.
            System.out.println("\n================== Returned Subscription ==================\n");
            System.out.println("  - Id: " + returnedSubscription.getId());
            System.out.println("  - Title: " + returnedSubscription.getSnippet().getTitle());

        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /*
     * Returns a channel id (String) from user via the terminal.
     */
    private static String getChannelId() throws IOException {

        String channelId = "";

        System.out.print("Please enter a channel id: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        channelId = bReader.readLine();

        if (channelId.length() < 1) {
            // If nothing is entered, defaults to "YouTube For Developers."
            channelId = "UCtVd0c0tGXuTSbU5d8cSBUg";
        }
        return channelId;
    }
}
