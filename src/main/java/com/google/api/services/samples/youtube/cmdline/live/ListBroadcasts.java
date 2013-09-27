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

package com.google.api.services.samples.youtube.cmdline.live;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

/**
 * Demo of listing broadcasts using the YouTube Live API (V3) with OAuth2 for authorization.
 *
 * @author Ibrahim Ulukaya
 */
public class ListBroadcasts {

    /**
     * Global instance of Youtube object to make all API requests.
     */
    private static YouTube youtube;

    /**
     * List user's broadcasts using OAuth2 for authentication.
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) {

        // Scope required to read from YouTube.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.readonly");

        try {
            // Authorization.
            Credential credential = Auth.authorize(scopes, "listbroadcasts");

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                    .setApplicationName("youtube-cmdline-listbroadcasts-sample").build();

            // Create request to list broadcasts.
            YouTube.LiveBroadcasts.List liveBroadcastRequest =
                    youtube.liveBroadcasts().list("id,snippet");

            // Modify results to have broadcasts in all states.
            liveBroadcastRequest.setBroadcastStatus("all");

            // List request is executed and list of broadcasts are returned
            LiveBroadcastListResponse returnedListResponse = liveBroadcastRequest.execute();

            // Get the list of broadcasts associated with the user.
            List<LiveBroadcast> returnedList = returnedListResponse.getItems();

            // Print out returned results.
            System.out.println("\n================== Returned Broadcasts ==================\n");
            for (LiveBroadcast broadcast : returnedList) {
                System.out.println("  - Id: " + broadcast.getId());
                System.out.println("  - Title: " + broadcast.getSnippet().getTitle());
                System.out.println("  - Description: " + broadcast.getSnippet().getDescription());
                System.out.println("  - Published At: " + broadcast.getSnippet().getPublishedAt());
                System.out.println(
                        "  - Scheduled Start Time: " + broadcast.getSnippet().getScheduledStartTime());
                System.out.println(
                        "  - Scheduled End Time: " + broadcast.getSnippet().getScheduledEndTime());
                System.out.println("\n-------------------------------------------------------------\n");
            }

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
}