/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.gateway;

import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.config.impl.GatewayConfigImpl;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class GatewayGlobalConfigTest {

  private String getHomeDirName( String resource ) {
    URL url = ClassLoader.getSystemResource( resource );
    String fileName = url.getFile();
    File file = new File( fileName );
    String dirName = file.getParentFile().getParent();
    return dirName;
  }

  @Test
  public void testFullConfig() {
    System.setProperty( GatewayConfigImpl.GATEWAY_HOME_VAR, getHomeDirName( "conf-full/conf/gateway-default.xml" ) );
    GatewayConfig config = new GatewayConfigImpl();
    assertThat( config.getGatewayPort(), is( 7777 ) );
    assertThat( config.isClientAuthNeeded(), is( false ) );
    assertNull("ssl.exclude.protocols should be null.", config.getExcludedSSLProtocols());
    //assertThat( config.getShiroConfigFile(), is( "full-shiro.ini") );
  }

  @Test
  public void testDemoConfig() {
    System.setProperty( GatewayConfigImpl.GATEWAY_HOME_VAR, getHomeDirName( "conf-demo/conf/gateway-default.xml" ) );
    GatewayConfig config = new GatewayConfigImpl();
    assertThat(config.getGatewayPort(), is( 8888 ) );
    assertTrue( config.getExcludedSSLProtocols().get(0).equals("SSLv3"));
    //assertThat( config.getShiroConfigFile(), is( "full-shiro.ini") );
  }

  @Test
  public void testSiteConfig() {
    System.setProperty( GatewayConfigImpl.GATEWAY_HOME_VAR, getHomeDirName( "conf-site/conf/gateway-site.xml" ) );
    GatewayConfig config = new GatewayConfigImpl();
    assertThat( config.getGatewayPort(), is( 5555 ) );
    assertThat( config.isClientAuthNeeded(), is( true ) );
    assertThat( config.getTruststorePath(), is("./gateway-trust.jks"));
    assertThat( config.getTruststoreType(), is( "PKCS12" ) );
    assertThat( config.getKeystoreType(), is( "JKS" ) );
  }

  @Test
  public void testEmptyConfig() {
    System.setProperty( GatewayConfigImpl.GATEWAY_HOME_VAR, getHomeDirName( "conf-empty/conf/empty" ) );
    GatewayConfig config = new GatewayConfigImpl();
    assertThat( config.getGatewayPort(), is( 8888 ) );
    //assertThat( config.getShiroConfigFile(), is( "shiro.ini") );
  }

  @Test
  public void testDefaultTopologyName() {
    GatewayConfig config = new GatewayConfigImpl();
    assertThat( config.getDefaultTopologyName(), is( "sandbox" ) );
  }

  @Test
  public void testDefaultAppRedirectPath() {
    GatewayConfig config = new GatewayConfigImpl();
    assertThat( config.getDefaultAppRedirectPath(), is( "/gateway/sandbox" ) );
  }

  @Test
  public void testForUpdatedDeploymentDir() {
    String homeDirName = getHomeDirName("conf-demo/conf/gateway-site.xml");
    System.setProperty(GatewayConfigImpl.GATEWAY_HOME_VAR, homeDirName);
    System.setProperty(GatewayConfigImpl.GATEWAY_DATA_HOME_VAR, homeDirName);
    GatewayConfig config = new GatewayConfigImpl();
    assertTrue(("target/test").equalsIgnoreCase(config.getGatewayDeploymentDir()));
  }

  @Test
  public void testDefaultDeploymentDir() {
    String homeDirName = getHomeDirName("conf-site/conf/gateway-site.xml");
    System.setProperty(GatewayConfigImpl.GATEWAY_HOME_VAR, homeDirName);
    System.setProperty(GatewayConfigImpl.GATEWAY_DATA_HOME_VAR, homeDirName);
    GatewayConfig config = new GatewayConfigImpl();
    assertThat(config.getGatewayDeploymentDir(), is(homeDirName + File.separator + "deployments"));
  }

  @Test
  public void testForDefaultSecurityDataDir() {
    String homeDirName = getHomeDirName("conf-site/conf/gateway-site.xml");
    System.setProperty(GatewayConfigImpl.GATEWAY_HOME_VAR, homeDirName);
    System.setProperty(GatewayConfigImpl.GATEWAY_DATA_HOME_VAR, homeDirName);
    GatewayConfig config = new GatewayConfigImpl();
    assertThat(config.getGatewaySecurityDir(), is(homeDirName + File.separator + "security"));
  }

  @Test
  public void testForUpdatedSecurityDataDir() {
    String homeDirName = getHomeDirName("conf-demo/conf/gateway-site.xml");
    System.setProperty(GatewayConfigImpl.GATEWAY_HOME_VAR, homeDirName);
    System.setProperty(GatewayConfigImpl.GATEWAY_DATA_HOME_VAR, homeDirName);
    GatewayConfig config = new GatewayConfigImpl();
    assertTrue(("target/test").equalsIgnoreCase(config.getGatewaySecurityDir()));
  }

  @Test
  public void testForDataDirSetAsSystemProperty() {
    String homeDirName = getHomeDirName("conf-demo/conf/gateway-site.xml");
    System.setProperty(GatewayConfigImpl.GATEWAY_DATA_HOME_VAR, homeDirName + File.separator
        + "DataDirSystemProperty");
    GatewayConfig config = new GatewayConfigImpl();
    assertTrue((homeDirName + File.separator + "DataDirSystemProperty").equalsIgnoreCase(config
        .getGatewayDataDir()));
  }

  @Test
  public void testForDataDirSetAsConfiguration() {
    String homeDirName = getHomeDirName("conf-demo/conf/gateway-site.xml");
    System.setProperty(GatewayConfigImpl.GATEWAY_HOME_VAR, homeDirName);
    System.clearProperty(GatewayConfigImpl.GATEWAY_DATA_HOME_VAR);
    GatewayConfig config = new GatewayConfigImpl();
    assertTrue(("target/testDataDir").equalsIgnoreCase(config
        .getGatewayDataDir()));
  }

  @Test
  public void testForDefaultDataDir() {
    String homeDirName = getHomeDirName("conf-site/conf/gateway-site.xml");
    System.setProperty(GatewayConfigImpl.GATEWAY_HOME_VAR, homeDirName);
    System.clearProperty(GatewayConfigImpl.GATEWAY_DATA_HOME_VAR);
    GatewayConfig config = new GatewayConfigImpl();
    assertTrue((homeDirName + File.separator + "data").equalsIgnoreCase(config.getGatewayDataDir()));
  }

  /**
   * When data dir is set at both system property and configuration level , then system property
   * value should be considered
   **/
  @Test
  public void testDataDirSetAsBothSystemPropertyAndConfig() {
    String homeDirName = getHomeDirName("conf-demo/conf/gateway-site.xml");
    System.setProperty(GatewayConfigImpl.GATEWAY_HOME_VAR, homeDirName);
    System.setProperty(GatewayConfigImpl.GATEWAY_DATA_HOME_VAR, homeDirName + File.separator
        + "DataDirSystemProperty");
    GatewayConfig config = new GatewayConfigImpl();
    assertTrue((homeDirName + File.separator + "DataDirSystemProperty").equalsIgnoreCase(config
        .getGatewayDataDir()));
  }

  @Test
  public void testStacksServicesDir() {
    System.clearProperty(GatewayConfigImpl.GATEWAY_HOME_VAR);
    GatewayConfig config = new GatewayConfigImpl();
    assertThat(config.getGatewayServicesDir(), Matchers.endsWith("data/services"));
    String homeDirName = getHomeDirName("conf-demo/conf/gateway-site.xml");
    System.setProperty(GatewayConfigImpl.GATEWAY_HOME_VAR, homeDirName);
    config = new GatewayConfigImpl();
    assertEquals("target/test", config.getGatewayServicesDir());
  }

}