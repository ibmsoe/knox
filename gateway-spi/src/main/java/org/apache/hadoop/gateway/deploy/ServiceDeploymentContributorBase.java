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
package org.apache.hadoop.gateway.deploy;

import org.apache.hadoop.gateway.descriptor.FilterParamDescriptor;
import org.apache.hadoop.gateway.descriptor.ResourceDescriptor;
import org.apache.hadoop.gateway.topology.Provider;
import org.apache.hadoop.gateway.topology.Service;
import org.apache.hadoop.gateway.topology.Version;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

public abstract class ServiceDeploymentContributorBase extends DeploymentContributorBase implements ServiceDeploymentContributor {

  @Override
  public Version getVersion() {
    return new Version();
  }

  public void initializeContribution( DeploymentContext context ) {
    // Noop.
  }

  public void finalizeContribution( DeploymentContext context ) {
    // Noop.
  }

  protected boolean topologyContainsProviderType(DeploymentContext context, String role) {
    Provider provider = getProviderByRole(context, role);
    return (provider != null);
  }
  
  protected Provider getProviderByRole(DeploymentContext context, String role) {
    Provider p = null;
    Collection<Provider> providers = context.getTopology().getProviders();
    for (Provider provider : providers) {
      if (role.equals(provider.getRole())) {
        p = provider;
        break;
      }
    }
    return p;
  }
  
  protected void addWebAppSecFilters( DeploymentContext context, Service service, ResourceDescriptor resource ) {
    if (topologyContainsProviderType(context, "webappsec")) {
      context.contributeFilter( service, resource, "webappsec", null, null );
    }
  }

  protected void addAuthenticationFilter( DeploymentContext context, Service service, ResourceDescriptor resource ) {
    if (topologyContainsProviderType(context, "authentication")) {
      context.contributeFilter( service, resource, "authentication", null, null );
    }
    if (topologyContainsProviderType(context, "federation")) {
      context.contributeFilter( service, resource, "federation", null, null );
    }
  }

  protected void addIdentityAssertionFilter(DeploymentContext context, Service service, ResourceDescriptor resource) {
    context.contributeFilter( service, resource, "identity-assertion", null, null );
  }

  protected void addAuthorizationFilter(DeploymentContext context, Service service, ResourceDescriptor resource) {
    if (topologyContainsProviderType(context, "authorization")) {
      context.contributeFilter( service, resource, "authorization", null, null );
    }
  }

  protected void addRewriteFilter(
      DeploymentContext context,
      Service service,
      ResourceDescriptor resource,
      List<FilterParamDescriptor> params ) throws URISyntaxException {
    context.contributeFilter( service, resource, "rewrite", null, params );
  }

  protected void addDispatchFilter(DeploymentContext context, Service service, ResourceDescriptor resource, String role, String name ) {
    if (name == null) {
      name = "http-client";
    }
    context.contributeFilter( service, resource, role, name, null );
  }

}
