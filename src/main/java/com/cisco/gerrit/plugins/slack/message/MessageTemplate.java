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

import com.cisco.gerrit.plugins.slack.util.ResourceHelper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a message template.
 *
 * @author Matthew Montgomery
 */
public class MessageTemplate
{
    /**
     * The class logger instance.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(MessageTemplate.class);

    private String channel;
    private String name;
    private String action;
    private int number;
    private String project;
    private String branch;
    private String url;
    private String message;


    public String getChannel()
    {
        return escape(channel);
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getName()
    {
        return escape(name);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAction()
    {
        return escape(action);
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    public String getProject()
    {
        return escape(project);
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public String getBranch()
    {
        return escape(branch);
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public String getMessage()
    {
        return escape(message);
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getUrl()
    {
        return escape(url);
    }

    public void setUrl(String url)
    {
        this.url = url;
    }


    /**
     * Renders the message template into a String.
     *
     * @return A String representation of the rendered template.
     */
    public String render()
    {
        String result;
        result = "";

        try
        {
            String template;
            template = ResourceHelper.loadNamedResourceAsString(
                    "message-template.json");

            result = String.format(template, getChannel(), getName(),
                    getAction(), getProject(), getBranch(), getNumber(),
                    getUrl(), getMessage(), "good");
        }
        catch (IOException e)
        {
            LOGGER.error("Error rendering template: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Escapes the double quote character.
     *
     * @param str The message in which to search escape double quote
     *            characters
     *
     * @return The message with all occurrences of the double quote character
     * escaped.
     */
    private String escape(String str)
    {
        if (str != null)
        {
            str = str.replace("\"", "\\\"");
        }

        return str;
    }
}
