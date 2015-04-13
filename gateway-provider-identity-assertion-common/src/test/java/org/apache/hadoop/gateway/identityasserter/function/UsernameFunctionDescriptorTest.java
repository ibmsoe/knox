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
package org.apache.hadoop.gateway.identityasserter.function;

import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriteFunctionDescriptor;
import org.apache.hadoop.gateway.identityasserter.common.function.UsernameFunctionDescriptor;
import org.junit.Test;

import java.util.Iterator;
import java.util.ServiceLoader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class UsernameFunctionDescriptorTest {

  @Test
  public void testName() throws Exception {
    UsernameFunctionDescriptor descriptor = new UsernameFunctionDescriptor();
    assertThat( descriptor.name(), is( "username" ) );
  }

  @Test
  public void testServiceLoader() throws Exception {
    ServiceLoader loader = ServiceLoader.load( UrlRewriteFunctionDescriptor.class );
    Iterator iterator = loader.iterator();
    while( iterator.hasNext() ) {
      Object object = iterator.next();
      if( object instanceof UsernameFunctionDescriptor ) {
        return;
      }
    }
    fail( "Failed to find UsernameFunctionDescriptor via service loader." );
  }

}
