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
package org.eclipse.wb.internal.xwt.model.forms;

import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.xwt.model.widgets.AbstractPositionCompositeInfo;

import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * Model for {@link ExpandableComposite}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.forms
 */
public final class ExpandableCompositeInfo extends AbstractPositionCompositeInfo {
  private static final String[] POSITIONS = {"textClient", "client"};

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExpandableCompositeInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport, POSITIONS);
  }
}
