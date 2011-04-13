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
package org.eclipse.wb.internal.discovery.core;

/**
 * Represents a feature that is a component of a WBToolkit.
 */
public class WBToolkitFeature {
  private String featureId;
  private boolean optional;

  public WBToolkitFeature(String featureId) {
    this(featureId, false);
  }

  public WBToolkitFeature(String featureId, boolean optional) {
    this.featureId = featureId;
    this.optional = optional;
  }

  public String getFeatureId() {
    return featureId;
  }

  public boolean isOptional() {
    return optional;
  }

  @Override
  public int hashCode() {
    return featureId.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof WBToolkitFeature)) {
      return false;
    }
    WBToolkitFeature other = (WBToolkitFeature) obj;
    return getFeatureId().equals(other.getFeatureId());
  }

  public String toString() {
    return featureId;
  }
}
