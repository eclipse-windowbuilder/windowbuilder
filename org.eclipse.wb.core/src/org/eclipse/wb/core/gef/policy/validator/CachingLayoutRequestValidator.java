/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.gef.policy.validator;

import com.google.common.collect.Maps;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;

import java.util.Map;

/**
 * {@link ILayoutRequestValidator} which caches results of validation.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class CachingLayoutRequestValidator implements ILayoutRequestValidator {
  private final ILayoutRequestValidator m_validator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CachingLayoutRequestValidator(ILayoutRequestValidator validator) {
    m_validator = validator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutRequestValidator
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean validateCreateRequest(EditPart host, CreateRequest request) {
    Map<EditPart, Boolean> cache = getCache(request);
    Boolean cachedResult = cache.get(host);
    if (cachedResult == null) {
      cachedResult = m_validator.validateCreateRequest(host, request);
      cache.put(host, cachedResult);
    }
    return cachedResult;
  }

  public boolean validatePasteRequest(final EditPart host, final PasteRequest request) {
    Map<EditPart, Boolean> cache = getCache(request);
    Boolean cachedResult = cache.get(host);
    if (cachedResult == null) {
      cachedResult = m_validator.validatePasteRequest(host, request);
      cache.put(host, cachedResult);
    }
    return cachedResult;
  }

  public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
    Map<EditPart, Boolean> cache = getCache(request);
    Boolean cachedResult = cache.get(host);
    if (cachedResult == null) {
      cachedResult = m_validator.validateMoveRequest(host, request);
      cache.put(host, cachedResult);
    }
    return cachedResult;
  }

  public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
    Map<EditPart, Boolean> cache = getCache(request);
    Boolean cachedResult = cache.get(host);
    if (cachedResult == null) {
      cachedResult = m_validator.validateAddRequest(host, request);
      cache.put(host, cachedResult);
    }
    return cachedResult;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the cache for validation results.
   */
  @SuppressWarnings("unchecked")
  private Map<EditPart, Boolean> getCache(Request request) {
    Map<EditPart, Boolean> cache = (Map<EditPart, Boolean>) request.getArbitraryValue(this);
    if (cache == null) {
      cache = Maps.newHashMap();
      request.putArbitraryValue(this, cache);
    }
    return cache;
  }
}
