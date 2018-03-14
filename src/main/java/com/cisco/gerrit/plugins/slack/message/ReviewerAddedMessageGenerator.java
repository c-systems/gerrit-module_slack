/*
 * Copyright 2017 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package com.cisco.gerrit.plugins.slack.message;

import static org.apache.commons.lang.StringUtils.substringBefore;

import com.cisco.gerrit.plugins.slack.config.ProjectConfig;
import com.google.gerrit.server.data.ChangeAttribute;
import com.google.gerrit.server.events.ReviewerAddedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specific MessageGenerator implementation that can generate a message for a reviewer added
 * event.
 *
 * @author Nathan Wall
 */
public class ReviewerAddedMessageGenerator implements MessageGenerator {
  /** The class logger instance. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ReviewerAddedMessageGenerator.class);

  private ProjectConfig config;
  private ReviewerAddedEvent event;

  /**
   * Creates a new ReviewerAddedMessageGenerator instance using the provided ReviewerAddedEvent
   * instance.
   *
   * @param event The ReviewerAddedEvent instance to generate a message for.
   */
  ReviewerAddedMessageGenerator(ReviewerAddedEvent event, ProjectConfig config) {
    if (event == null) {
      throw new NullPointerException("event cannot be null");
    }

    this.event = event;
    this.config = config;
  }

  @Override
  public boolean shouldPublish() {
    if (!config.isEnabled() || !config.shouldPublishOnReviewerAdded()) {
      return false;
    }

    try {
      ChangeAttribute change;
      change = event.change.get();
      if (config.getIgnorePrivatePatchSet() && Boolean.TRUE.equals(change.isPrivate)) {
        return false;
      }
      if (config.getIgnoreWorkInProgressPatchSet() && Boolean.TRUE.equals(change.wip)) {
        return false;
      }
    } catch (Exception e) {
      LOGGER.warn("Error checking private and work-in-progress status", e);
    }

    return true;
  }

  @Override
  public String generate() {
    String message;
    message = "";

    try {
      MessageTemplate template;
      template = new MessageTemplate();

      template.setChannel(config.getChannel());
      template.setName(event.reviewer.get().name);
      template.setAction("was added to review");
      template.setNumber(event.change.get().number);
      template.setProject(event.change.get().project);
      template.setBranch(event.change.get().branch);
      template.setUrl(event.change.get().url);
      template.setTitle(substringBefore(event.change.get().commitMessage, "\n"));

      message = template.render();
    } catch (Exception e) {
      LOGGER.error("Error generating message: " + e.getMessage(), e);
    }

    return message;
  }
}
