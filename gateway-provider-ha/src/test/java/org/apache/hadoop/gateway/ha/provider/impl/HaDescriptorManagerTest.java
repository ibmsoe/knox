/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.gateway.ha.provider.impl;

import org.apache.hadoop.gateway.ha.provider.HaDescriptor;
import org.apache.hadoop.gateway.ha.provider.HaServiceConfig;
import org.junit.Test;
import org.custommonkey.xmlunit.XMLTestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.xml.sax.SAXException;
import java.io.StringWriter;

import static org.junit.Assert.*;
import static org.xmlmatchers.XmlMatchers.isEquivalentTo;
import static org.xmlmatchers.transform.XmlConverters.the;

public class HaDescriptorManagerTest extends XMLTestCase {

   @Test
   public void testDescriptorLoad() throws IOException {
      String xml = "<ha><service name='foo' maxFailoverAttempts='42' failoverSleep='4000' maxRetryAttempts='2' retrySleep='2213' enabled='false'/>" +
            "<service name='bar' failoverLimit='3' enabled='true'/></ha>";
      ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
      HaDescriptor descriptor = HaDescriptorManager.load(inputStream);
      assertNotNull(descriptor);
      assertEquals(1, descriptor.getEnabledServiceNames().size());
      HaServiceConfig config =  descriptor.getServiceConfig("foo");
      assertNotNull(config);
      assertEquals("foo", config.getServiceName());
      assertEquals(42, config.getMaxFailoverAttempts());
      assertEquals(4000, config.getFailoverSleep());
      assertEquals(2, config.getMaxRetryAttempts());
      assertEquals(2213, config.getRetrySleep());
      assertFalse(config.isEnabled());
      config =  descriptor.getServiceConfig("bar");
      assertTrue(config.isEnabled());
   }

   @Test
   public void testDescriptorDefaults() throws IOException {
      String xml = "<ha><service name='foo'/></ha>";
      ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
      HaDescriptor descriptor = HaDescriptorManager.load(inputStream);
      assertNotNull(descriptor);
      assertEquals(1, descriptor.getEnabledServiceNames().size());
      HaServiceConfig config =  descriptor.getServiceConfig("foo");
      assertNotNull(config);
      assertEquals("foo", config.getServiceName());
      assertEquals(HaServiceConfigConstants.DEFAULT_MAX_FAILOVER_ATTEMPTS, config.getMaxFailoverAttempts());
      assertEquals(HaServiceConfigConstants.DEFAULT_FAILOVER_SLEEP, config.getFailoverSleep());
      assertEquals(HaServiceConfigConstants.DEFAULT_MAX_RETRY_ATTEMPTS, config.getMaxRetryAttempts());
      assertEquals(HaServiceConfigConstants.DEFAULT_RETRY_SLEEP, config.getRetrySleep());
      assertEquals(HaServiceConfigConstants.DEFAULT_ENABLED, config.isEnabled());
   }

   @Test
   public void testDescriptorStoring() throws IOException, SAXException {
      HaDescriptor descriptor = HaDescriptorFactory.createDescriptor();
      descriptor.addServiceConfig(HaDescriptorFactory.createServiceConfig("foo", "false", "42", "1000", "3", "3000"));
      descriptor.addServiceConfig(HaDescriptorFactory.createServiceConfig("bar", "true", "3", "5000", "5", "8000"));
      StringWriter writer = new StringWriter();
      HaDescriptorManager.store(descriptor, writer);
      String descriptorXml = writer.toString();
      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<ha>\n" +
            "  <service enabled=\"false\" failoverSleep=\"1000\" maxFailoverAttempts=\"42\" maxRetryAttempts=\"3\" name=\"foo\" retrySleep=\"3000\"/>\n" +
            "  <service enabled=\"true\" failoverSleep=\"5000\" maxFailoverAttempts=\"3\" maxRetryAttempts=\"5\" name=\"bar\" retrySleep=\"8000\"/>\n" +
            "</ha>\n";
      assertThat( the( descriptorXml ), isEquivalentTo( the( xml ) ) );
   }


}
