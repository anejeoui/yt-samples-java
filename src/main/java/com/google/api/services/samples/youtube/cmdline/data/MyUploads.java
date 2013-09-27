/*
 * Copyright (c) 2012 Google Inc.
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
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Prints a list of videos uploaded to the user's YouTube account using OAuth2 for authentication.
 * <p/>
 * Details: The app uses Youtube.Channnels.List to get the playlist id associated with all the
 * videos ever uploaded to the user's account. It then gets all the video info using
 * YouTube.PlaylistItems.List. Finally, it prints all the information to the screen.
 *
 * @author Jeremy Walker
 */
public class MyUploads {

    /**
     * Global instance of YouTube object to make all API requests.
     */
    private static YouTube youtube;

    /**
     * Authorizes user, runs Youtube.Channnels.List get the playlist id associated with uploaded
     * videos, runs YouTube.PlaylistItems.List to get information on each video, and prints out the
     * results.
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) {

        // Scope required to upload to YouTube.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorization.
            Credential credential = Auth.authorize(scopes, "myuploads");

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                    "youtube-cmdline-myuploads-sample").build();

      /*
       * Now that the user is authenticated, the app makes a channel list request to get the
       * authenticated user's channel. Returned with that data is the playlist id for the uploaded
       * videos. https://developers.google.com/youtube/v3/docs/channels/list
       */
            YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
            channelRequest.setMine(true);

      /*
       * Limit the results to only the data we need
       */
            channelRequest.setFields("items/contentDetails,nextPageToken,pageInfo");
            ChannelListResponse channelResult = channelRequest.execute();

      /*
       * Gets the list of channels associated with the user. This sample only pulls the uploaded
       * videos for the first channel (default channel for user).
       */
            List<Channel> channelsList = channelResult.getItems();

            if (channelsList != null) {
                // Gets user's default channel id (first channel in list).
                String uploadPlaylistId =
                        channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads();

                // List to store all PlaylistItem items associated with the uploadPlaylistId.
                List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

        /*
         * Now that we have the playlist id for your uploads, we will request the playlistItems
         * associated with that playlist id, so we can get information on each video uploaded. This
         * is the template for the list call. We call it multiple times in the do while loop below
         * (only changing the nextToken to get all the videos).
         * https://developers.google.com/youtube/v3/docs/playlistitems/list
         */
                YouTube.PlaylistItems.List playlistItemRequest =
                        youtube.playlistItems().list("id,contentDetails,snippet");
                playlistItemRequest.setPlaylistId(uploadPlaylistId);

                // This limits the results to only the data we need and makes things more efficient.
                playlistItemRequest.setFields(
                        "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

                String nextToken = "";

                // Loops over all search page results returned for the uploadPlaylistId.
                do {
                    playlistItemRequest.setPageToken(nextToken);
                    PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                    playlistItemList.addAll(playlistItemResult.getItems());

                    nextToken = playlistItemResult.getNextPageToken();
                } while (nextToken != null);

                // Prints results.
                prettyPrint(playlistItemList.size(), playlistItemList.iterator());
            }

        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /*
     * Method that prints all the PlaylistItems in an Iterator.
     *
     * @param size size of list
     *
     * @param iterator of Playlist Items from uploaded Playlist
     */
    private static void prettyPrint(int size, Iterator<PlaylistItem> playlistEntries) {
        System.out.println("=============================================================");
        System.out.println("\t\tTotal Videos Uploaded: " + size);
        System.out.println("=============================================================\n");

        while (playlistEntries.hasNext()) {
            PlaylistItem playlistItem = playlistEntries.next();
            System.out.println(" video name  = " + playlistItem.getSnippet().getTitle());
            System.out.println(" video id    = " + playlistItem.getContentDetails().getVideoId());
            System.out.println(" upload date = " + playlistItem.getSnippet().getPublishedAt());
            System.out.println("\n-------------------------------------------------------------\n");
        }
    }
}
