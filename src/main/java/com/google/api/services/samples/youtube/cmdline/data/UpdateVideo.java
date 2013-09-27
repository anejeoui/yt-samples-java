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
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo of updating a video by adding a tag, using the YouTube Data API (V3) with OAuth2 for
 * authorization.
 *
 * @author Ibrahim Ulukaya
 */
public class UpdateVideo {

    /**
     * Global instance of YouTube object to make all API requests.
     */
    private static YouTube youtube;

    /**
     * Uploads user selected video in the project folder to the user's YouTube account using OAuth2
     * for authentication.
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) {

        // An OAuth 2 access scope that allows for full read/write access.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorization.
            Credential credential = Auth.authorize(scopes, "updatevideo");

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                    .setApplicationName("youtube-cmdline-updatevideo-sample").build();

            // Get the video ID to update via user input from the terminal.
            String videoId = getVideoIdFromUser();
            System.out.println("You chose " + videoId + " to update.");

            // Get a keyword to add to the video we are updating via user input from the terminal.
            String tag = getTagFromUser();
            System.out.println("You chose " + tag + " as a tag.");

            // Create the video list request
            YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet").setId(videoId);

            // Request is executed and video list response is returned
            VideoListResponse listResponse = listVideosRequest.execute();

            List<Video> videoList = listResponse.getItems();
            if (videoList.isEmpty()) {
                System.out.println("Can't find a video with video id: " + videoId);
                return;
            }

            // Since a unique video id is given, it will only return 1 video.
            Video video = videoList.get(0);
            VideoSnippet snippet = video.getSnippet();

            List<String> tags = snippet.getTags();

            // getTags() returns null if the video didn't have any tags, so we will check for this and
            // create a new list if needed
            if (tags == null) {
                tags = new ArrayList<String>(1);
                snippet.setTags(tags);
            }
            tags.add(tag);

            // Create the video update request
            YouTube.Videos.Update updateVideosRequest = youtube.videos().update("snippet", video);

            // Request is executed and updated video is returned
            Video videoResponse = updateVideosRequest.execute();

            // Print out returned results.
            System.out.println("\n================== Returned Video ==================\n");
            System.out.println("  - Title: " + videoResponse.getSnippet().getTitle());
            System.out.println("  - Tags: " + videoResponse.getSnippet().getTags());

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
     * Prompts for a tag from standard input and returns it.
     */
    private static String getTagFromUser() throws IOException {

        String title = "";

        System.out.print("Please enter a tag for your video: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        title = bReader.readLine();

        if (title.length() < 1) {
            // If nothing is entered, defaults to "New Tag"
            title = "New Tag";
        }
        return title;
    }

    /*
     * Prompts for a video ID from standard input and returns it.
     */
    private static String getVideoIdFromUser() throws IOException {

        String title = "";

        System.out.print("Please enter a video Id to update: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        title = bReader.readLine();

        if (title.length() < 1) {
            // If nothing is entered, exits
            System.out.print("Video Id can't be empty!");
            System.exit(1);
        }

        return title;
    }

}
