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

import org.junit.Test;

public class MessageTemplateTest
{
    @Test
    public void rendersTemplate() throws Exception
    {
        MessageTemplate template;
        template = new MessageTemplate();

        template.setChannel("general");
        template.setName("Mr. Developer");
        template.setAction("proposed");
        template.setNumber(1234);
        template.setProject("project");
        template.setBranch("master");
        template.setUrl("https://gerrit-review.googlesource.com/#/admin/projects/plugins/slack-integration");
        template.setMessage("This is a really great commit.");

        System.out.println(template.render());
    }
}
