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

import com.cisco.gerrit.plugins.slack.config.ProjectConfig;
import com.google.gerrit.server.events.ChangeMergedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specific MessageGenerator implementation that can generate a message for
 * a change merged event.
 *
 * @author Matthew Montgomery
 */
public class ChangeMergedMessageGenerator implements MessageGenerator
{
    /**
     * The class logger instance.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ChangeMergedMessageGenerator.class);

    private ProjectConfig config;
    private ChangeMergedEvent event;

    /**
     * Creates a new ChangeMergedMessageGenerator instance using the provided
     * ChangeMergedEvent instance.
     *
     * @param event The ChangeMergedEvent instance to generate a message for.
     */
    ChangeMergedMessageGenerator(ChangeMergedEvent event,
            ProjectConfig config)
    {
        if (event == null)
        {
            throw new NullPointerException("event cannot be null");
        }

        this.event = event;
        this.config = config;
    }

    @Override
    public boolean shouldPublish()
    {
        return config.isEnabled() && config.shouldPublishOnChangeMerged();
    }

    @Override
    public String generate()
    {
        String message;
        message = "";

        try
        {
            MessageTemplate template;
            template = new MessageTemplate();

            template.setChannel(config.getChannel());
            template.setName(event.submitter.get().name);
            template.setAction("merged");
            template.setNumber(event.change.get().number);
            template.setProject(event.change.get().project);
            template.setBranch(event.change.get().branch);
            template.setUrl(event.change.get().url);
            template.setMessage(event.change.get().commitMessage.split("\n")[0]);

            message = template.render();
        }
        catch (Exception e)
        {
            LOGGER.error("Error generating message: " + e.getMessage(), e);
        }

        return message;
    }
}
