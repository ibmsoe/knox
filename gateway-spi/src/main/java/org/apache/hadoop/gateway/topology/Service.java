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
package org.apache.hadoop.gateway.topology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Service {

  private String role;
  private String name;
  private Version version;
  private Map<String, String> params = new LinkedHashMap<String, String>();
  private List<String> urls;

  public String getRole() {
    return role;
  }

  public void setRole( String role ) {
    this.role = role;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public List<String> getUrls() {
    if ( urls == null ) {
      urls = new ArrayList<String>();
    }
    return urls;
  }

  public void setUrls( List<String> urls ) {
    this.urls = urls;
  }

  public String getUrl() {
    if ( !getUrls().isEmpty() ) {
      return getUrls().get( 0 );
    }
    return null;
  }

  public void addUrl( String url) {
    getUrls().add( url );
  }

  public Map<String, String> getParams() {
    return params;
  }

  private Collection<Param> getParamsList(){

    ArrayList<Param> paramList = new ArrayList<Param>();

    for(Map.Entry<String, String> entry : params.entrySet()){
      Param p = new Param();
      p.setName(entry.getKey());
      p.setValue(entry.getValue());
      paramList.add(p);
    }

    return paramList;
  }

  public void setParams(Map<String, String> params) {
    this.params = params;
  }

  public void addParam( Param param ) {
    params.put(param.getName(), param.getValue());
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Service)) {
      return false;
    }
    Service that = (Service) object;
    String thatName = that.getName();
    if (thatName != null && !(thatName.equals(name))) {
        return false;
    }
    String thatRole = that.getRole();
    if (thatRole != null && !thatRole.equals(role)) {
        return false;
    }
    Version thatVersion = that.getVersion();
    if (thatVersion != null && !(thatVersion.equals(version))) {
        return false;
    }
    return true;
  }
}
