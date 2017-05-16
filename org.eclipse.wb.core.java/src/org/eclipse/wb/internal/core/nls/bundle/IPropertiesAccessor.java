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
package org.eclipse.wb.internal.core.nls.bundle;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

/**
 * This interface is used to load and save *.properties files.
 *
 * We need it because GWT supports *.properties files in UTF-8, but standard {@link Properties}
 * class supports only ISO-8859-1.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface IPropertiesAccessor {
  /**
   * @return {@link Map} loaded from given stream (based on *.properties file)
   */
  Map<String, String> load(InputStream is, String charset) throws Exception;

  /**
   * Saves given map and comments into given stream.
   */
  void save(OutputStream os, String charset, Map<String, String> map, String comments)
      throws Exception;
}
