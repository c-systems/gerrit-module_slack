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
import com.google.common.base.Suppliers;
import com.google.gerrit.extensions.client.ChangeKind;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.data.AccountAttribute;
import com.google.gerrit.server.data.ChangeAttribute;
import com.google.gerrit.server.data.PatchSetAttribute;
import com.google.gerrit.server.events.PatchSetCreatedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the PatchSetCreatedMessageGeneratorTest class.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Project.NameKey.class})
public class PatchSetCreatedMessageGeneratorTest
{
    private static final String PROJECT_NAME = "test-project";

    private Project.NameKey mockNameKey =
            mock(Project.NameKey.class);

    private PluginConfigFactory mockConfigFactory =
            mock(PluginConfigFactory.class);

    private PluginConfig mockPluginConfig =
            mock(PluginConfig.class);

    private PatchSetCreatedEvent mockEvent = mock(PatchSetCreatedEvent.class);
    private AccountAttribute mockAccount = mock(AccountAttribute.class);
    private ChangeAttribute mockChange = mock(ChangeAttribute.class);
    private PatchSetAttribute mockPatchSet = mock(PatchSetAttribute.class);

    @Before
    public void setup() throws Exception
    {
        PowerMockito.mockStatic(Project.NameKey.class);
        when(Project.NameKey.parse(PROJECT_NAME)).thenReturn(mockNameKey);
    }

    private ProjectConfig getConfig(
        String ignore,
        boolean publishOnPatchSetCreated,
        boolean ignoreUnchangedPatchSet,
        boolean ignoreWorkInProgressPatchSet,
        boolean ignorePrivatePatchSet)
        throws Exception
    {
        Project.NameKey projectNameKey;
        projectNameKey = Project.NameKey.parse(PROJECT_NAME);

        // Setup mocks
        when(mockConfigFactory.getFromProjectConfigWithInheritance(
                projectNameKey, ProjectConfig.CONFIG_NAME))
                .thenReturn(mockPluginConfig);

        when(mockPluginConfig.getBoolean("enabled", false))
                .thenReturn(true);
        when(mockPluginConfig.getString("webhookurl", ""))
                .thenReturn("https://webook/");
        when(mockPluginConfig.getString("channel", "general"))
                .thenReturn("testchannel");
        when(mockPluginConfig.getString("username", "gerrit"))
                .thenReturn("testuser");
        when(mockPluginConfig.getString("ignore", ""))
                .thenReturn(ignore);
        when(mockPluginConfig.getBoolean("publish-on-patch-set-created", true))
                .thenReturn(publishOnPatchSetCreated);
        when(mockPluginConfig.getBoolean("ignore-unchanged-patch-set", true))
                .thenReturn(ignoreUnchangedPatchSet);
        when(mockPluginConfig.getBoolean("ignore-wip-patch-set", true))
                .thenReturn(ignoreWorkInProgressPatchSet);
        when(mockPluginConfig.getBoolean("ignore-private-patch-set", true))
                .thenReturn(ignorePrivatePatchSet);

        return new ProjectConfig(mockConfigFactory, PROJECT_NAME);
    }

    private ProjectConfig getConfig(String ignore) throws Exception
    {
        return getConfig(
            ignore,
            true /* publishOnPatchSetCreated */,
            true /* ignoreUnchangedPatchSet */,
            true /* ignoreWorkInProgressPatchSet */,
            true /* ignorePrivatePatchSet */
        );
    }

    private ProjectConfig getConfig(boolean publishOnPatchSetCreated)
                                        throws Exception
    {
        return getConfig(
            "^WIP.*",
            publishOnPatchSetCreated,
            true /* ignoreUnchangedPatchSet */,
            true /* ignoreWorkInProgressPatchSet */,
            true /* ignorePrivatePatchSet */
        );
    }

    private ProjectConfig getConfig(boolean publishOnPatchSetCreated,
                                        boolean ignoreUnchangedPatchSet)
                                        throws Exception
    {
        return getConfig(
            "^WIP.*",
            publishOnPatchSetCreated,
            ignoreUnchangedPatchSet,
            true /* ignoreWorkInProgressPatchSet */,
            true /* ignorePrivatePatchSet */
        );
    }

    private ProjectConfig getConfig(boolean publishOnPatchSetCreated,
                                        boolean ignoreUnchangedPatchSet,
                                        boolean ignoreWorkInProgressPatchSet,
                                        boolean ignorePrivatePatchSet)
                                        throws Exception
    {
        return getConfig(
            "^WIP.*",
            publishOnPatchSetCreated,
            ignoreUnchangedPatchSet,
            ignoreWorkInProgressPatchSet,
            ignorePrivatePatchSet
        );
    }

    private ProjectConfig getConfig() throws Exception
    {
        return getConfig(
            "^WIP.*",
            true /* publishOnPatchSetCreated */,
            true /* ignoreUnchangedPatchSet */,
            true /* ignoreWorkInProgressPatchSet */,
            true /* ignorePrivatePatchSet */
        );
    }

    @Test
    public void factoryCreatesExpectedType() throws Exception
    {
        ProjectConfig config = getConfig();
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator instanceof PatchSetCreatedMessageGenerator,
                is(true));
    }

    @Test
    public void publishesWhenExpected() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockChange.commitMessage = "This is a title\nAnd a the body.";

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void doesNotPublishWhenMessageMatchesIgnore() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockChange.commitMessage = "WIP-This is a title\nAnd a the body.";

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(false));
    }

    @Test
    public void doesNotPublishWhenTurnedOff() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig(false /* publishOnPatchSetCreated */);
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockChange.commitMessage = "This is a title\nAnd a the body.";

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(false));
    }

    @Test
    public void handlesInvalidIgnorePatterns() throws Exception
    {
        ProjectConfig config = getConfig(null /* ignore */);

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void doesNotPublishWhenTrivialRebase() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.patchSet = Suppliers.ofInstance(mockPatchSet);
        mockPatchSet.kind = ChangeKind.TRIVIAL_REBASE;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(false));
    }

    @Test
    public void publishesWhenTrivialRebase() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig(true, false);
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.patchSet = Suppliers.ofInstance(mockPatchSet);
        mockPatchSet.kind = ChangeKind.TRIVIAL_REBASE;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void doesNotPublishWhenMergeUpdate() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.patchSet = Suppliers.ofInstance(mockPatchSet);
        mockPatchSet.kind = ChangeKind.MERGE_FIRST_PARENT_UPDATE;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(false));
    }

    @Test
    public void publishesWhenMergeUpdate() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig(true, false);
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.patchSet = Suppliers.ofInstance(mockPatchSet);
        mockPatchSet.kind = ChangeKind.MERGE_FIRST_PARENT_UPDATE;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void doesNotPublishWhenNoCodeChange() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.patchSet = Suppliers.ofInstance(mockPatchSet);
        mockPatchSet.kind = ChangeKind.NO_CODE_CHANGE;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(false));
    }

    @Test
    public void publishesWhenNoCodeChange() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig(true, false);
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.patchSet = Suppliers.ofInstance(mockPatchSet);
        mockPatchSet.kind = ChangeKind.NO_CODE_CHANGE;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void doesNotPublishWhenNoChange() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.patchSet = Suppliers.ofInstance(mockPatchSet);
        mockPatchSet.kind = ChangeKind.NO_CHANGE;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(false));
    }

    @Test
    public void publishesWhenNoChange() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig(true, false);
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.patchSet = Suppliers.ofInstance(mockPatchSet);
        mockPatchSet.kind = ChangeKind.NO_CHANGE;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void publishesWhenRework() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.patchSet = Suppliers.ofInstance(mockPatchSet);
        mockPatchSet.kind = ChangeKind.REWORK;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void doesNotPublishWhenWorkInProgress() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockChange.wip = true;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(false));
    }

    @Test
    public void doesNotPublishWhenPrivate() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockChange.isPrivate = true;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(false));
    }

    @Test
    public void publishesWhenWorkInProgress() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig(true /* publishOnPatchSetCreated */,
            false /* ignoreRebaseEmptyPatchSet */,
            false /* ignoreWorkInProgressPatchSet */,
            false /* ignorePrivatePatchSet */);
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockChange.wip = true;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void publishesWhenPrivate() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig(true /* publishOnPatchSetCreated */,
            false /* ignoreRebaseEmptyPatchSet */,
            false /* ignoreWorkInProgressPatchSet */,
            false /* ignorePrivatePatchSet */);
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockChange.isPrivate = true;

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void generatesExpectedMessage() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.uploader = Suppliers.ofInstance(mockAccount);

        mockChange.number = 1234;
        mockChange.project = "testproject";
        mockChange.branch = "master";
        mockChange.url = "https://change/";
        mockChange.commitMessage = "This is the title\nThis is the message body.";

        mockAccount.name = "Unit Tester";

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        String expectedResult;
        expectedResult = "{\n" +
                "  \"channel\": \"#testchannel\",\n" +
                "  \"attachments\": [\n" +
                "    {\n" +
                "      \"fallback\": \"Unit Tester proposed testproject (master) https://change/: This is the title\",\n" +
                "      \"pretext\": \"Unit Tester proposed <https://change/|testproject (master) change 1234>\",\n" +
                "      \"title\": \"This is the title\",\n" +
                "      \"title_link\": \"https://change/\",\n" +
                "      \"text\": \"\",\n" +
                "      \"color\": \"good\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        String actualResult;
        actualResult = messageGenerator.generate();

        assertThat(actualResult, is(equalTo(expectedResult)));
    }
}
