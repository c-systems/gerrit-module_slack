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
import com.google.gerrit.server.events.WorkInProgressStateChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang.StringUtils.substringBefore;

/**
 * A specific MessageGenerator implementation that can generate a message for
 * a work-in-progress state changed event.
 *
 * @author James Hartig
 */
public class WorkInProgressStateChangedGenerator implements MessageGenerator
{
    /**
     * The class logger instance.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(WorkInProgressStateChangedGenerator.class);

    private ProjectConfig config;
    private WorkInProgressStateChangedEvent event;

    /**
     * Creates a new WorkInProgressStateChangedGenerator instance using the provided
     * WorkInProgressStateChangedEvent instance.
     *
     * @param event The WorkInProgressStateChangedEvent instance to generate a message for.
     */
    WorkInProgressStateChangedGenerator(WorkInProgressStateChangedEvent event,
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
        if (!config.isEnabled() || !config.shouldPublishOnWipReady())
        {
            return false;
        }

        // If the change is still work-in-progress then ignore
        if (Boolean.TRUE.equals(event.change.get().wip))
        {
            return false;
        }
        return true;
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
            template.setName(event.changer.get().name);
            template.setAction("proposed");
            template.setNumber(event.change.get().number);
            template.setProject(event.change.get().project);
            template.setBranch(event.change.get().branch);
            template.setUrl(event.change.get().url);
            template.setTitle(substringBefore(event.change.get().commitMessage, "\n"));

            message = template.render();
        }
        catch (Exception e)
        {
            LOGGER.error("Error generating message: " + e.getMessage(), e);
        }

        return message;
    }
}
