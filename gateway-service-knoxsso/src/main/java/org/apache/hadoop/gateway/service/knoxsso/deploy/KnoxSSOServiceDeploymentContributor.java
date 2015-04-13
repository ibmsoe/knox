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
package org.apache.hadoop.gateway.service.knoxsso.deploy;

import org.apache.hadoop.gateway.jersey.JerseyServiceDeploymentContributorBase;

/**
 *
 */
public class KnoxSSOServiceDeploymentContributor extends JerseyServiceDeploymentContributorBase {

  /* (non-Javadoc)
   * @see org.apache.hadoop.gateway.deploy.ServiceDeploymentContributor#getRole()
   */
  @Override
  public String getRole() {
    // TODO Auto-generated method stub
    return "KNOXSSO";
  }

  /* (non-Javadoc)
   * @see org.apache.hadoop.gateway.deploy.ServiceDeploymentContributor#getName()
   */
  @Override
  public String getName() {
    return "KnoxSSOService";
  }

  /* (non-Javadoc)
   * @see org.apache.hadoop.gateway.jersey.JerseyServiceDeploymentContributorBase#getPackages()
   */
  @Override
  protected String[] getPackages() {
    return new String[]{ "org.apache.hadoop.gateway.service.knoxsso" };
  }

  /* (non-Javadoc)
   * @see org.apache.hadoop.gateway.jersey.JerseyServiceDeploymentContributorBase#getPatterns()
   */
  @Override
  protected String[] getPatterns() {
    return new String[]{ "knoxsso/**?**" };
  }

}
