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
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * This program adds a featured video to a channel via the Invideo Programming API.
 *
 * @author Ikai Lan <ikai@google.com>
 */
public class InvideoProgramming {

    /**
     * Global instance of Youtube object to make all API requests.
     */
    private static YouTube youtube;

    /**
     * Sample video introducing WebM on YouTube Developers Live.
     */
    private static final String FEATURED_VIDEO_ID = "w4eiUiauo2w";

    /**
     * This code sample demonstrates the different ways the InvideoProgramming API can be used. In this sample, we
     * demonstrate sample code that
     * <ol>
     * <li>Features a video</li>
     * <li>Features a link to some social media channel</li>
     * <li>Sets a watermark for videos on our channel</li>
     * </ol>
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) {

        // An OAuth 2 access scope that allows for full read/write access.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorization.
            Credential credential = Auth.authorize(scopes, "invideoprogramming");

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                    .setApplicationName("youtube-cmdline-invideoprogramming-sample")
                    .build();

            // Fetch the user's channel. We also fetch the uploads playlist so we can use this later
            // to find the most recently uploaded video
            ChannelListResponse channelListResponse = youtube.channels().list("id,contentDetails")
                    .setMine(true)
                    .setFields("items(contentDetails/relatedPlaylists/uploads,id)")
                    .execute();

            // This assumes the user has a channel already. If the user does not have a channel, this should
            // throw a GoogleJsonResponseException explaining the issue
            Channel myChannel = channelListResponse.getItems().get(0);
            String channelId = myChannel.getId();

            // The promotion will appear 15000ms (15 seconds) before the end of the video
            InvideoTiming invideoTiming = new InvideoTiming();
            invideoTiming.setOffsetMs(BigInteger.valueOf(15000l));
            invideoTiming.setType("offsetFromEnd");

            // This is one of the types of promotions - this promotes a video
            PromotedItemId promotedItemId = new PromotedItemId();
            promotedItemId.setType("video");
            promotedItemId.setVideoId(FEATURED_VIDEO_ID);

            // Custom message can be set programmatically. This could be used for additional information
            // promoting the video or other item
            PromotedItem promotedItem = new PromotedItem();
            promotedItem.setCustomMessage("Check out this video about WebM!");
            promotedItem.setId(promotedItemId);

            // Construct the Invideo promotion
            InvideoPromotion invideoPromotion = new InvideoPromotion();
            invideoPromotion.setDefaultTiming(invideoTiming);
            invideoPromotion.setItems(Lists.newArrayList(promotedItem));

            // Now let's add the invideo promotion to the channel
            Channel channel = new Channel();
            channel.setId(channelId);
            channel.setInvideoPromotion(invideoPromotion);

            // Make the API call
            Channel updateChannelResponse = youtube.channels()
                    .update("invideoPromotion", channel)
                    .execute();

            // Print out returned results.
            System.out.println("\n================== Updated Channel Information ==================\n");
            System.out.println("\t- Channel ID: " + updateChannelResponse.getId());

            InvideoPromotion promotions = updateChannelResponse.getInvideoPromotion();
            promotedItem = promotions.getItems().get(0); // We only care about the first item
            System.out.println("\t- Invideo promotion video ID: " + promotedItem
                    .getId()
                    .getVideoId());
            System.out.println("\t- Promotion message: " + promotedItem.getCustomMessage());

            // InvideoProgramming can also be used to feature a link to associated websites, merchant sites,
            // or social networking sites. The code below will override the earlier settings by featuring a link
            // to the YouTube Developers Twitter feed.
            PromotedItemId promotedTwitterFeed = new PromotedItemId();
            promotedTwitterFeed.setType("website");
            promotedTwitterFeed.setWebsiteUrl("https://twitter.com/youtubedev");

            promotedItem = new PromotedItem();
            promotedItem.setCustomMessage("Follow us on Twitter!");
            promotedItem.setId(promotedTwitterFeed);

            invideoPromotion.setItems(Lists.newArrayList(promotedItem));
            channel.setInvideoPromotion(invideoPromotion);

            // Make the API call
            updateChannelResponse = youtube.channels()
                    .update("invideoPromotion", channel)
                    .execute();

            // Print out returned results.
            System.out.println("\n================== Updated Channel Information ==================\n");
            System.out.println("\t- Channel ID: " + updateChannelResponse.getId());

            promotions = updateChannelResponse.getInvideoPromotion();
            promotedItem = promotions.getItems().get(0);
            System.out.println("\t- Invideo promotion URL: " + promotedItem
                    .getId()
                    .getWebsiteUrl());
            System.out.println("\t- Promotion message: " + promotedItem.getCustomMessage();

            // This example below sets a custom watermark for the channel. We'll use a sample watermark.jpg that's
            // provided in the resources directory.
            InputStreamContent mediaContent = new InputStreamContent("image/jpeg",
                    InvideoProgramming.class.getResourceAsStream("/watermark.jpg"));

            // Set the timing of displaying the watermark to 15 seconds before the end of the video
            InvideoTiming watermarkTiming = new InvideoTiming();
            watermarkTiming.setType("offsetFromEnd");
            watermarkTiming.setDurationMs(BigInteger.valueOf(15000l));
            watermarkTiming.setOffsetMs(BigInteger.valueOf(15000l));

            InvideoBranding invideoBranding = new InvideoBranding();
            invideoBranding.setTiming(watermarkTiming);
            youtube.watermarks().set(channelId, invideoBranding, mediaContent).execute();

        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
