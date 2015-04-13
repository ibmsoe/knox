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
package org.apache.hadoop.gateway.filter.rewrite.impl;

import org.apache.hadoop.gateway.filter.AbstractGatewayFilter;
import org.apache.hadoop.gateway.filter.GatewayRequestWrapper;
import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriteFilterContentDescriptor;
import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriteFilterDescriptor;
import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriteRulesDescriptor;
import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriteServletContextListener;
import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriteServletFilter;
import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriteStreamFilterFactory;
import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriter;
import org.apache.hadoop.gateway.filter.rewrite.i18n.UrlRewriteMessages;
import org.apache.hadoop.gateway.i18n.messages.MessagesFactory;
import org.apache.hadoop.gateway.util.urltemplate.Parser;
import org.apache.hadoop.gateway.util.urltemplate.Resolver;
import org.apache.hadoop.gateway.util.urltemplate.Template;

import javax.activation.MimeType;
import javax.servlet.FilterConfig;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.apache.hadoop.gateway.filter.rewrite.impl.UrlRewriteUtil.pickFirstRuleWithEqualsIgnoreCasePathMatch;

public class UrlRewriteRequest extends GatewayRequestWrapper implements Resolver {

  private static final UrlRewriteMessages LOG = MessagesFactory.get( UrlRewriteMessages.class );

  private UrlRewriter rewriter;
  private String urlRuleName;
  private String bodyFilterName;
  private String headersFilterName;
  private UrlRewriteFilterContentDescriptor headersFilterConfig;
  private String cookiesFilterName;
  private UrlRewriteFilterContentDescriptor cookiesFilterConfig;

  /**
   * Constructs a request object wrapping the given request.
   *
   * @throws IllegalArgumentException if the request is null
   */
  public UrlRewriteRequest( FilterConfig config, HttpServletRequest request ) throws IOException {
    super( request );
    this.rewriter = UrlRewriteServletContextListener.getUrlRewriter( config.getServletContext() );
    this.urlRuleName = config.getInitParameter( UrlRewriteServletFilter.REQUEST_URL_RULE_PARAM );
    this.bodyFilterName = config.getInitParameter( UrlRewriteServletFilter.REQUEST_BODY_FILTER_PARAM );
    this.headersFilterName = config.getInitParameter( UrlRewriteServletFilter.REQUEST_HEADERS_FILTER_PARAM );
    this.headersFilterConfig = getRewriteFilterConfig( headersFilterName, UrlRewriteServletFilter.HEADERS_MIME_TYPE );
    this.cookiesFilterName = config.getInitParameter( UrlRewriteServletFilter.REQUEST_COOKIES_FILTER_PARAM );
    this.cookiesFilterConfig = getRewriteFilterConfig( cookiesFilterName, UrlRewriteServletFilter.COOKIES_MIME_TYPE );
  }

  private Template getSourceUrl() {
    Template urlTemplate;
    //KNOX-439[
    //StringBuffer urlString = super.getRequestURL();
    StringBuffer urlString = new StringBuffer( 128 );
    urlString.append( getScheme() );
    urlString.append( "://" );
    urlString.append( getServerName() );
    urlString.append( ":" );
    urlString.append( getServerPort() );
    urlString.append( super.getRequestURI() );
    //]
    String queryString = super.getQueryString();
    if( queryString != null ) {
      urlString.append( '?' );
      urlString.append( queryString );
    }
    try {
      urlTemplate = Parser.parse( urlString.toString() );
    } catch( URISyntaxException e ) {
      LOG.failedToParseValueForUrlRewrite( urlString.toString() );
      // Shouldn't be possible given that the URL is constructed from parts of an existing URL.
      urlTemplate = null;
    }
    return urlTemplate;
  }

  // Note: Source url was added to the request attributes by the GatewayFilter doFilter method.
  private Template getTargetUrl() {
    boolean rewriteRequestUrl = true;
    Template targetUrl;
    if( rewriteRequestUrl ) {
      targetUrl = (Template)getAttribute( AbstractGatewayFilter.TARGET_REQUEST_URL_ATTRIBUTE_NAME );
      if( targetUrl == null ) {
        Template sourceUrl = getSourceUrl();
        targetUrl = rewriter.rewrite( this, sourceUrl, UrlRewriter.Direction.IN, urlRuleName );
        setAttribute( AbstractGatewayFilter.TARGET_REQUEST_URL_ATTRIBUTE_NAME, targetUrl );
      }
    } else {
      targetUrl = (Template)getAttribute( AbstractGatewayFilter.SOURCE_REQUEST_URL_ATTRIBUTE_NAME );
    }
    return targetUrl;
  }

  private String[] splitTargetUrl( Template url ) {
    String s = url.toString();
    return s.split( "\\?" );
  }

  @Override
  public StringBuffer getRequestURL() {
    return new StringBuffer( getRequestURI() );
  }

  //TODO: I think this method is implemented wrong based on the HttpServletRequest.getRequestURI docs.
  // It should not include the scheme or authority parts.
  @Override
  public String getRequestURI() {
    String[] split = splitTargetUrl( getTargetUrl() );
    if( split.length > 0 ) {
      return split[0];
    } else {
      return "";
    }
  }

  @Override
  public String getQueryString() {
    String[] split = splitTargetUrl( getTargetUrl() );
    if( split.length > 1 ) {
      return split[1];
    } else {
      return null;
    }
  }

  private String rewriteValue( UrlRewriter rewriter, String value, String rule ) {
    try {
      Template input = Parser.parse( value );
      Template output = rewriter.rewrite( this, input, UrlRewriter.Direction.IN, rule );
      value = output.getPattern();
    } catch( URISyntaxException e ) {
      LOG.failedToParseValueForUrlRewrite( value );
    }
    return value;
  }

  @Override
  public String getHeader( String name ) {
    String value = super.getHeader( name );
    if( value != null ) {
      value = rewriteValue( rewriter, super.getHeader( name ), pickFirstRuleWithEqualsIgnoreCasePathMatch( headersFilterConfig, name ) );
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  public Enumeration getHeaders( String name ) {
    return new EnumerationRewriter( rewriter, super.getHeaders( name ), pickFirstRuleWithEqualsIgnoreCasePathMatch( headersFilterConfig, name ) );
  }

  @Override
  public List<String> resolve( String name ) {
    return Collections.emptyList();
  }

  private class EnumerationRewriter implements Enumeration<String> {

    private UrlRewriter rewriter;
    private Enumeration<String> delegate;
    private String rule;

    private EnumerationRewriter( UrlRewriter rewriter, Enumeration<String> delegate, String rule ) {
      this.rewriter = rewriter;
      this.delegate = delegate;
      this.rule = rule;
    }

    @Override
    public boolean hasMoreElements() {
      return delegate.hasMoreElements();
    }

    @Override
    public String nextElement() {
      return rewriteValue( rewriter, delegate.nextElement(), rule );
    }
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    MimeType mimeType = getMimeType();
    UrlRewriteFilterContentDescriptor filterContentConfig = getRewriteFilterConfig( bodyFilterName, mimeType );
    InputStream stream = UrlRewriteStreamFilterFactory.create(
        mimeType, null, super.getInputStream(), rewriter, this, UrlRewriter.Direction.IN, filterContentConfig );
    return new UrlRewriteRequestStream( stream );
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader( new InputStreamReader( getInputStream(), getCharacterEncoding() ) );
  }

  @Override
  public int getContentLength() {
    // The rewrite might change the content length so return the default of -1 to indicate the length is unknown.
    return -1;
  }

  private UrlRewriteFilterContentDescriptor getRewriteFilterConfig( String filterName, MimeType mimeType ) {
    UrlRewriteFilterContentDescriptor filterContentConfig = null;
    UrlRewriteRulesDescriptor rewriteConfig = rewriter.getConfig();
    if( rewriteConfig != null ) {
      UrlRewriteFilterDescriptor filterConfig = rewriteConfig.getFilter( filterName );
      if( filterConfig != null ) {
        filterContentConfig = filterConfig.getContent( mimeType );
      }
    }
    return filterContentConfig;
  }

}
