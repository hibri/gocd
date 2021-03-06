/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.config;

import com.thoughtworks.go.domain.materials.ValidationBean;
import com.thoughtworks.go.security.GoCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4ClassRunner.class)
public class GoSmtpMailSenderTest {

    private final String hostName = "smtp.company.test";

    @Test
    public void testShouldNotSendOutTestEmailToAdminstrator() {
        GoSmtpMailSender sender = new GoSmtpMailSender(hostName, 465, "cruise.test.admin@gmail.com",
                "password123", true,
                "from.cruise.test@gmail.com", "cruise.test.admin@gmail.com");
        ValidationBean bean = sender.send("Subject", "Body", "cruise.test.admin@gmail.com");

        assertThat(bean, is(not(ValidationBean.valid())));
    }
    
    @Test
    public void shouldNotSendTestEmailToSmtpServerIfTlsConfiguredIncorrect() {
        GoSmtpMailSender sender = new GoSmtpMailSender(hostName, 25, "cruise2", "password123", true, "from.cruise.test@gmail.com", "cruise.test.admin@gmail.com");
        ValidationBean bean = sender.send("Subject", "Body", "cruise.test.admin@gmail.com");

        assertThat(bean, is(not(ValidationBean.valid())));
    }

    @Test
    public void shouldCreateSmtpMailSender() throws Exception {
        MailHost mailHost = new MailHost(hostName, 25, "smtpuser", "password", true, true, "cruise@me.com", "jez@me.com");
        GoMailSender sender = GoSmtpMailSender.createSender(mailHost);

        GoMailSender goSmtpMailSender = new GoSmtpMailSender(hostName, 25, "smtpuser", "password", true, "cruise@me.com", "jez@me.com");
        GoMailSender backgroundSender = new BackgroundMailSender(goSmtpMailSender);
        assertThat(sender, is(backgroundSender));
    }

    @Test
    public void shouldUseTheNewPasswordIfThePasswordIsChanged() {
        MailHost mailHost = new MailHost(hostName, 25, "smtpuser", "password", "encrypted_password", true, true, "cruise@me.com", "jez@me.com");
        GoMailSender sender = GoSmtpMailSender.createSender(mailHost);
        assertThat((BackgroundMailSender) sender, is(new BackgroundMailSender(new GoSmtpMailSender(hostName, 25, "smtpuser", "password", true, "cruise@me.com", "jez@me.com"))));
    }

    @Test
    public void shouldUseTheEncryptedPasswordIfThePasswordIsNotChanged() throws InvalidCipherTextException {
        String encryptedPassword = new GoCipher().encrypt("encrypted_password");
        MailHost mailHost = new MailHost(hostName, 25, "smtpuser", "password", encryptedPassword, false, true, "cruise@me.com", "jez@me.com");
        GoMailSender sender = GoSmtpMailSender.createSender(mailHost);
        assertThat((BackgroundMailSender) sender, is(new BackgroundMailSender(new GoSmtpMailSender(hostName, 25, "smtpuser", "encrypted_password", true, "cruise@me.com", "jez@me.com"))));
    }

}