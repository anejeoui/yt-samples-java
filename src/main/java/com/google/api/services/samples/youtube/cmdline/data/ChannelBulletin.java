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
import com.google.api.services.youtube.model.*;
import com.google.common.collect.Lists;

import java.util.Calendar;
import java.util.List;

/**
 * Creates a video bulletin that is posted to the user's channel feed.
 *
 * @author Jeremy Walker
 */
public class ChannelBulletin {

    /**
     * Global instance of YouTube object to make all API requests.
     */
    private static YouTube youtube;

    /*
     * Global instance of the video id we want to post as a bulletin into our channel feed. You will
     * probably pull this from a search or your app.
     */
    private static String VIDEO_ID = "L-oNKK1CrnU";


    /**
     * Authorizes user, runs Youtube.Channnels.List to get the default channel, and posts a bulletin
     * with a video id to the user's default channel.
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) {

        // Scope required to upload to YouTube.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorization.
            Credential credential = Auth.authorize(scopes, "channelbulletin");

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                    "youtube-cmdline-channelbulletin-sample").build();

      /*
       * Now that the user is authenticated, the app makes a channel list request to get the
       * authenticated user's channel. https://developers.google.com/youtube/v3/docs/channels/list
       */
            YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
            channelRequest.setMine(true);
      /*
       * Limits the results to only the data we need making your app more efficient.
       */
            channelRequest.setFields("items/contentDetails");
            ChannelListResponse channelResult = channelRequest.execute();

      /*
       * Gets the list of channels associated with the user.
       */
            List<Channel> channelsList = channelResult.getItems();

            if (channelsList != null) {
                // Gets user's default channel id (first channel in list).
                String channelId = channelsList.get(0).getId();

        /*
         * We create the snippet to set the channel we will post to and the description that goes
         * along with our bulletin.
         */
                ActivitySnippet snippet = new ActivitySnippet();
                snippet.setChannelId(channelId);
                Calendar cal = Calendar.getInstance();
                snippet.setDescription("Bulletin test video via YouTube API on " + cal.getTime());

        /*
         * We set the kind of the ResourceId to video (youtube#video). Please note, you could set
         * the type to a playlist (youtube#playlist) and use a playlist id instead of a video id.
         */
                ResourceId resource = new ResourceId();
                resource.setKind("youtube#video");
                resource.setVideoId(VIDEO_ID);

                ActivityContentDetailsBulletin bulletin = new ActivityContentDetailsBulletin();
                bulletin.setResourceId(resource);

                // We construct the ActivityContentDetails now that we have the Bulletin.
                ActivityContentDetails contentDetails = new ActivityContentDetails();
                contentDetails.setBulletin(bulletin);

        /*
         * Finally, we construct the activity we will write to YouTube via the API. We set the
         * snippet (covers description and channel we are posting to) and the content details
         * (covers video id and type).
         */
                Activity activity = new Activity();
                activity.setSnippet(snippet);
                activity.setContentDetails(contentDetails);

        /*
         * We specify the parts (contentDetails and snippet) we will write to YouTube. Those also
         * cover the parts that are returned.
         */
                YouTube.Activities.Insert insertActivities =
                        youtube.activities().insert("contentDetails,snippet", activity);
                // This returns the Activity that was added to the user's YouTube channel.
                Activity newActivityInserted = insertActivities.execute();

                if (newActivityInserted != null) {
                    System.out.println(
                            "New Activity inserted of type " + newActivityInserted.getSnippet().getType());
                    System.out.println(" - Video id "
                            + newActivityInserted.getContentDetails().getBulletin().getResourceId().getVideoId());
                    System.out.println(
                            " - Description: " + newActivityInserted.getSnippet().getDescription());
                    System.out.println(" - Posted on " + newActivityInserted.getSnippet().getPublishedAt());
                } else {
                    System.out.println("Activity failed.");
                }

            } else {
                System.out.println("No channels are assigned to this user.");
            }
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}