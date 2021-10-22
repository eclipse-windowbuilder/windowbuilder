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
package org.eclipse.wb.internal.core.databinding.xml.model;

import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.xml.parser.DatabindingRootProcessor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This interface is contributed via extension point and used by {@link DatabindingRootProcessor}
 * for creating {@link IDatabindingsProvider} for root {@link XmlObjectInfo}.
 *
 * @author lobas_av
 * @coverage bindings.xml.model
 */
public interface IDatabindingFactory {
  /**
   * @return {@link IDatabindingsProvider} for given root {@link XmlObjectInfo} or <code>null</code>
   *         if operation for given root not supported.
   */
  IDatabindingsProvider createProvider(XmlObjectInfo xmlObjectRoot) throws Exception;

  /**
   * @return {@link AbstractUIPlugin} host for this factory.
   */
  AbstractUIPlugin getPlugin();
}