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
package org.eclipse.wb.internal.swt.model.layout;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import java.util.List;

/**
 * SWT provider for layout assistant pages.
 *
 * @author lobas_av
 * @coverage swt.assistant
 */
public abstract class LayoutAssistantSupport
    extends
      org.eclipse.wb.core.editor.actions.assistant.LayoutAssistantSupport {
  protected final ILayoutInfo<?> m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutAssistantSupport(ILayoutInfo<?> layout) {
    super(layout.getUnderlyingModel());
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutAssistantSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final String getConstraintsPageTitle() {
    return ModelMessages.LayoutAssistantSupport_layoutDataPage;
  }

  @Override
  protected final ObjectInfo getContainer() {
    return m_layout.getComposite().getUnderlyingModel();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Converts {@link IControlInfo}s into their {@link ILayoutDataInfo}s.
   */
  protected final List<ILayoutDataInfo> getDataList(List<ObjectInfo> objects) {
    List<ILayoutDataInfo> dataList =
        Lists.transform(objects, new Function<ObjectInfo, ILayoutDataInfo>() {
          @Override
          public ILayoutDataInfo apply(ObjectInfo from) {
            return m_layout.getLayoutData2((IControlInfo) from);
          }
        });
    return dataList;
  }
}