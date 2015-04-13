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
package org.apache.hadoop.gateway.services.security;

import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.services.ServiceLifecycleException;
import org.apache.hadoop.gateway.services.security.impl.AESEncryptor;
import org.apache.hadoop.gateway.services.security.impl.DefaultCryptoService;
import org.apache.hadoop.test.category.ManualTests;
import org.apache.hadoop.test.category.MediumTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category( { ManualTests.class, MediumTests.class } )
public class CryptoServiceTest {
  static CryptoService cs = null;
  static AliasService as = null;
  
  @BeforeClass
  public static void setupSuite() throws Exception {
    as = new AliasService() {
      @Override
      public void init(GatewayConfig config, Map<String, String> options)
          throws ServiceLifecycleException {
      }

      @Override
      public void start() throws ServiceLifecycleException {
      }

      @Override
      public void stop() throws ServiceLifecycleException {
      }

      @Override
      public void addAliasForCluster(String clusterName, String alias,
          String value) {
      }

      @Override
      public char[] getPasswordFromAliasForCluster(String clusterName,
          String alias) {
        return "password".toCharArray();
      }

      @Override
      public char[] getPasswordFromAliasForCluster(String clusterName,
          String alias, boolean generate) {
        return null;
      }

      @Override
      public void generateAliasForCluster(String clusterName, String alias) {
      }

      @Override
      public char[] getPasswordFromAliasForGateway(String alias) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void generateAliasForGateway(String alias) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public Certificate getCertificateForGateway(String alias) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void removeAliasForCluster(String clusterName, String alias) {
      }

      @Override
      public List<String> getAliasesForCluster(String clusterName) {
        // TODO Auto-generated method stub
        return null;
      }
    };
    cs = new DefaultCryptoService();
    ((DefaultCryptoService)cs).setAliasService(as);
  }
  
  @Test
  public void testAESEncryptor() throws Exception {
    // password to create key - same Encryptor
    String queryString = "url=http://localhost:50070/api/v1/blahblah";
    AESEncryptor aes0 = new AESEncryptor("password");
    EncryptionResult result0 = cs.encryptForCluster("Test", "encrypt_url", queryString.getBytes("UTF8"));
    byte[] decrypted0 = aes0.decrypt(result0.salt, result0.iv, result0.cipher);
    assertEquals(queryString, new String(decrypted0, "UTF8"));
    assertEquals(queryString.getBytes("UTF8").length, decrypted0.length);
    assertEquals(queryString.getBytes("UTF8").length, new String(decrypted0, "UTF8").toCharArray().length);
    
    // password to create key - same Encryptor
    AESEncryptor aes = new AESEncryptor("Test");
    EncryptionResult result = aes.encrypt("larry".getBytes("UTF8"));
    byte[] decrypted = aes.decrypt(result.salt, result.iv, result.cipher);
    assertEquals(new String(decrypted, "UTF8"), "larry");

    // password to create key - different Encryptor
    AESEncryptor aes2 = new AESEncryptor("Test");
    decrypted = aes2.decrypt(result.salt, result.iv, result.cipher);
    assertEquals(new String(decrypted, "UTF8"), "larry");

    
    // password to create key resolved from alias - same Encryptor
    AESEncryptor aes3 = new AESEncryptor(new String(as.getPasswordFromAliasForCluster("test", "encrypt_url")));
    result = aes3.encrypt("larry".getBytes("UTF8"));
    decrypted = aes3.decrypt(result.salt, result.iv, result.cipher);
    assertEquals(new String(decrypted, "UTF8"), "larry");

    // password to create key resolved from alias - different Encryptor
    AESEncryptor aes4 = new AESEncryptor(new String(as.getPasswordFromAliasForCluster("test", "encrypt_url")));
    decrypted = aes4.decrypt(result.salt, result.iv, result.cipher);
    assertEquals(new String(decrypted, "UTF8"), "larry");
  }
  
  @Test
  //@Ignore
  public void testEncryptionOfQueryStrings() throws Exception {
    String alias = "encrypt-url";
    String queryString = "url=http://localhost:50070/api/v1/blahblah";    
    
    EncryptionResult result = cs.encryptForCluster("Test", alias, queryString.getBytes("UTF8"));
    assertTrue("Resulted cipertext length should be a multiple of 16", (result.cipher.length % 16) == 0);
    byte[] decryptedQueryString = cs.decryptForCluster("Test", alias, result.cipher, result.iv, result.salt);
    assertEquals(queryString.getBytes("UTF8").length, decryptedQueryString.length);
  }
}
