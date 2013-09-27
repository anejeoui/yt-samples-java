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
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Thumbnails.Set;
import com.google.api.services.youtube.model.ThumbnailSetResponse;
import com.google.common.collect.Lists;

import java.io.*;
import java.util.List;

/**
 * This sample uploads and sets a custom thumbnail for a video by:
 * <p/>
 * 1. Uploading a image utilizing "MediaHttpUploader"
 * 2. Setting the uploaded image as a custom thumbnail to the video via "youtube.thumbnails.set"
 * method
 *
 * @author Ibrahim Ulukaya
 */
public class UploadThumbnail {

    /**
     * Global instance of Youtube object to make all API requests.
     */
    private static YouTube youtube;

    /* Global instance of the format used for the image being uploaded (MIME type). */
    private static final String IMAGE_FILE_FORMAT = "image/png";

    /**
     * This is a very simple code sample that looks up a user's channel, then features the most
     * recently uploaded video in the bottom left hand corner of every single video in the channel.
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) {

        // An OAuth 2 access scope that allows for full read/write access.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorization.
            Credential credential = Auth.authorize(scopes, "uploadthumbnail");

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                    "youtube-cmdline-uploadthumbnail-sample").build();

            // Get the video ID to update from the user via terminal input.
            String videoId = getVideoIdFromUser();
            System.out.println("You chose " + videoId + " to upload a thumbnail.");

            // Get image file to upload from the user via terminal input.
            File imageFile = getImageFromUser();
            System.out.println("You chose " + imageFile + " to upload.");

            InputStreamContent mediaContent = new InputStreamContent(
                    IMAGE_FILE_FORMAT, new BufferedInputStream(new FileInputStream(imageFile)));
            mediaContent.setLength(imageFile.length());

            // Create a request to set the selected mediaContent as the thumbnail of the selected video.
            Set thumbnailSet = youtube.thumbnails().set(videoId, mediaContent);

            // Set the upload type and add event listener.
            MediaHttpUploader uploader = thumbnailSet.getMediaHttpUploader();

      /*
       * Sets whether direct media upload is enabled or disabled. True = whole media content is
       * uploaded in a single request. False (default) = resumable media upload protocol which lets
       * you resume an upload operation after a network interruption or other transmission failure,
       * saving time and bandwidth in the event of network failures.
       */
            uploader.setDirectUploadEnabled(false);

            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        /** Set before the initiation request is sent. */
                        case INITIATION_STARTED:
                            System.out.println("Initiation Started");
                            break;
                        /** Set after the initiation request completes. */
                        case INITIATION_COMPLETE:
                            System.out.println("Initiation Completed");
                            break;
                        /** Set after a media file chunk is uploaded. */
                        case MEDIA_IN_PROGRESS:
                            System.out.println("Upload in progress");
                            System.out.println("Upload percentage: " + uploader.getProgress());
                            break;
                        /** Set after the complete media file is successfully uploaded. */
                        case MEDIA_COMPLETE:
                            System.out.println("Upload Completed!");
                            break;
                        /** The upload process has not started yet. */
                        case NOT_STARTED:
                            System.out.println("Upload Not Started!");
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            // Execute upload and set thumbnail.
            ThumbnailSetResponse setResponse = thumbnailSet.execute();

            // Print out returned results.
            System.out.println("\n================== Uploaded Thumbnail ==================\n");
            System.out.println("  - Url: " + setResponse.getItems().get(0).getDefault().getUrl());

        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
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

    /*
     * Prompts for the path of the image file to upload from standard input and returns it.
     */
    private static File getImageFromUser() throws IOException {

        String path = "";

        System.out.print("Please enter the path of the image file to upload: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        path = bReader.readLine();

        if (path.length() < 1) {
            // If nothing is entered, exits
            System.out.print("Path can not be empty!");
            System.exit(1);
        }

        return new File(path);
    }
}
