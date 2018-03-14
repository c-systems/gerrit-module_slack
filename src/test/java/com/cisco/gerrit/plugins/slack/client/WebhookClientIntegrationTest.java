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

package com.cisco.gerrit.plugins.slack.client;

import static org.junit.Assert.assertTrue;

import com.cisco.gerrit.plugins.slack.message.MessageTemplate;
import com.cisco.gerrit.plugins.slack.util.ResourceHelper;
import java.io.InputStream;
import java.util.Properties;
import org.junit.Test;

public class WebhookClientIntegrationTest {
  @Test
  public void canPublishMessage() throws Exception {
    WebhookClient client;
    client = new WebhookClient();

    InputStream testProperties;
    testProperties = ResourceHelper.loadNamedResourceAsStream("test.properties");

    Properties properties;
    properties = new Properties();
    properties.load(testProperties);

    testProperties.close();

    MessageTemplate template;
    template = new MessageTemplate();

    template.setChannel("general");
    template.setName("Integration Tester");
    template.setAction("proposed");
    template.setProject("project");
    template.setBranch("master");
    template.setUrl("http://gerrit/1234");
    template.setNumber(1234);
    template.setTitle("Adds a test commit message");

    String webhookUrl;
    webhookUrl = properties.getProperty("webhook-url");

    assertTrue(client.publish(template.render(), webhookUrl));
  }

  @Test
  public void canPublishMessageWithLongMessage() throws Exception {
    WebhookClient client;
    client = new WebhookClient();

    InputStream testProperties;
    testProperties = ResourceHelper.loadNamedResourceAsStream("test.properties");

    Properties properties;
    properties = new Properties();
    properties.load(testProperties);

    testProperties.close();

    MessageTemplate template;
    template = new MessageTemplate();

    template.setChannel("general");
    template.setName("Integration Tester");
    template.setAction("commented on");
    template.setProject("project");
    template.setBranch("master");
    template.setUrl("http://gerrit/1234");
    template.setNumber(1234);
    template.setTitle("Adds a test commit message");
    template.setMessage(
        "It provides a bunch of really great things. "
            + "I am mostly trying to fill out a really long comment to "
            + "test message rendering. Slack should do the right thing "
            + "but this will be on multiple lines in IRC.\n\n\n\n\n"
            + "This is hidden.");

    String webhookUrl;
    webhookUrl = properties.getProperty("webhook-url");

    assertTrue(client.publish(template.render(), webhookUrl));
  }
}
