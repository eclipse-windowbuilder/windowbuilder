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
package org.eclipse.wb.internal.core.editor.palette.model.entry;

import org.eclipse.core.runtime.IConfigurationElement;

import org.xml.sax.Attributes;

import java.util.Map;

/**
 * Factory for {@link AttributesProvider} implementations.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class AttributesProviders {
  public static AttributesProvider get(final IConfigurationElement element) {
    return new AttributesProvider() {
      public String getAttribute(String name) {
        return element.getAttribute(name);
      }
    };
  }

  public static AttributesProvider get(final Attributes attributes) {
    return new AttributesProvider() {
      public String getAttribute(String name) {
        return attributes.getValue(name);
      }
    };
  }

  public static AttributesProvider get(final Map<String, String> attributes) {
    return new AttributesProvider() {
      public String getAttribute(String name) {
        return attributes.get(name);
      }
    };
  }
}
