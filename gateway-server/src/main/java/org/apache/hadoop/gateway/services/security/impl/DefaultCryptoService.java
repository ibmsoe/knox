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
package org.apache.hadoop.gateway.services.security.impl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;

import org.apache.hadoop.gateway.GatewayMessages;
import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.i18n.messages.MessagesFactory;
import org.apache.hadoop.gateway.services.security.AliasService;
import org.apache.hadoop.gateway.services.security.CryptoService;
import org.apache.hadoop.gateway.services.security.EncryptionResult;
import org.apache.hadoop.gateway.services.security.KeystoreService;
import org.apache.hadoop.gateway.services.security.KeystoreServiceException;
import org.apache.hadoop.gateway.services.ServiceLifecycleException;

public class DefaultCryptoService implements CryptoService {
  private static final String GATEWAY_IDENTITY_PASSPHRASE = "gateway-identity-passphrase";
  private static final GatewayMessages LOG = MessagesFactory.get( GatewayMessages.class ); 

  private AliasService as = null;
  private KeystoreService ks = null;

  public void setKeystoreService(KeystoreService ks) {
    this.ks = ks;
  }

  public void setAliasService(AliasService as) {
    this.as = as;
  }

  @Override
  public void init(GatewayConfig config, Map<String, String> options)
      throws ServiceLifecycleException {
    if (as == null) {
      throw new ServiceLifecycleException("Alias service is not set");
    }
  }

  @Override
  public void start() throws ServiceLifecycleException {
    // TODO Auto-generated method stub

  }

  @Override
  public void stop() throws ServiceLifecycleException {
    // TODO Auto-generated method stub

  }

  @Override
  public void createAndStoreEncryptionKeyForCluster(String clusterName, String alias) {
    as.generateAliasForCluster(clusterName, alias);
  }

  @Override
  public EncryptionResult encryptForCluster(String clusterName, String alias, byte[] clear) {
    char[] password = as.getPasswordFromAliasForCluster(clusterName, alias);
    if (password != null) {
      AESEncryptor aes = null;
      try {
        aes = new AESEncryptor(new String(password));
        return aes.encrypt(clear);
      } catch (NoSuchAlgorithmException e1) {
        LOG.failedToEncryptPasswordForCluster( clusterName, e1 );
      } catch (InvalidKeyException e) {
        LOG.failedToEncryptPasswordForCluster( clusterName, e );
      } catch (Exception e) {
        LOG.failedToEncryptPasswordForCluster( clusterName, e );
      }
    }
    return null;
  }

  @Override
  public byte[] decryptForCluster(String clusterName, String alias, String cipherText) {
    try {
      return decryptForCluster(clusterName, alias, cipherText.getBytes("UTF8"), null, null);
    } catch (UnsupportedEncodingException e) {
      LOG.unsupportedEncoding( e );
    }
    return null;
  }

  @Override
  public byte[] decryptForCluster(String clusterName, String alias, byte[] cipherText, byte[] iv, byte[] salt) {
  char[] password = as.getPasswordFromAliasForCluster(clusterName, alias);
    if (password != null) {
      AESEncryptor aes = new AESEncryptor(new String(password));
      try {
        return aes.decrypt(salt, iv, cipherText);
      } catch (Exception e) {
        LOG.failedToDecryptPasswordForCluster( clusterName, e );
      }
    }
    else {
      LOG.failedToDecryptCipherForClusterNullPassword( clusterName );
    }
    return null;
  }

  @Override
  public boolean verify(String algorithm, String alias, String signed, byte[] signature) {
    boolean verified = false;
    try {
      Signature sig=Signature.getInstance(algorithm);
      sig.initVerify(ks.getKeystoreForGateway().getCertificate(alias).getPublicKey());
      sig.update(signed.getBytes("UTF-8"));
      verified = sig.verify(signature);
    } catch (SignatureException e) {
      LOG.failedToVerifySignature( e );
    } catch (NoSuchAlgorithmException e) {
      LOG.failedToVerifySignature( e );
    } catch (InvalidKeyException e) {
      LOG.failedToVerifySignature( e );
    } catch (KeyStoreException e) {
      LOG.failedToVerifySignature( e );
    } catch (UnsupportedEncodingException e) {
      LOG.failedToVerifySignature( e );
    } catch (KeystoreServiceException e) {
      LOG.failedToVerifySignature( e );
    }
    LOG.signatureVerified( verified );
    return verified;
  }

  @Override
  public byte[] sign(String algorithm, String alias, String payloadToSign) {
    try {
      char[] passphrase = as.getPasswordFromAliasForGateway(GATEWAY_IDENTITY_PASSPHRASE);
      PrivateKey privateKey = (PrivateKey) ks.getKeyForGateway(alias, passphrase);
      Signature signature = Signature.getInstance(algorithm);
      signature.initSign(privateKey);
      signature.update(payloadToSign.getBytes("UTF-8"));
      return signature.sign();
    } catch (NoSuchAlgorithmException e) {
      LOG.failedToSignData( e );
    } catch (InvalidKeyException e) {
      LOG.failedToSignData( e );
    } catch (SignatureException e) {
      LOG.failedToSignData( e );
    } catch (UnsupportedEncodingException e) {
      LOG.failedToSignData( e );
    } catch (KeystoreServiceException e) {
      LOG.failedToSignData( e );
    }
    return null;
  }
}
