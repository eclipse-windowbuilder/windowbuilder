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
package org.eclipse.wb.internal.core.xml.model.utils;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.util.generic.ModelMethodPropertyChildSupport;
import org.eclipse.wb.internal.core.model.util.generic.ModelMethodPropertySupport;
import org.eclipse.wb.internal.core.xml.model.IXMLObjectInitializationParticipator;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * Helper to configure any {@link JavaInfo} to support configuration based features (from
 * <code>parameter</code> tags).
 * 
 * @author scheglov_ke
 * @coverage XML.model.util
 */
public final class DescriptionDrivenFeaturesParticipator
    implements
      IXMLObjectInitializationParticipator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IXMLObjectInitializationParticipator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(XmlObjectInfo object) throws Exception {
    //CopyPropertyTop_Support.install(javaInfo);
    //CopyPropertyTop_ChildSupport.install(javaInfo);
    ModelMethodPropertySupport.install(object, "x-modelMethodProperty ");
    ModelMethodPropertyChildSupport.install(object, "x-modelMethodChildProperty ");
  }
}
