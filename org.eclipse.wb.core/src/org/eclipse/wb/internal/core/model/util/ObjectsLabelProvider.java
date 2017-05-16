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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateIcon;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link LabelProvider} for {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ObjectsLabelProvider extends LabelProvider {
  public static final ObjectsLabelProvider INSTANCE = new ObjectsLabelProvider();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ObjectsLabelProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LabelProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getImage(final Object element) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
      public Image runObject() throws Exception {
        ObjectInfo objectInfo = (ObjectInfo) element;
        Image icon = objectInfo.getPresentation().getIcon();
        // decorate
        {
          Image[] decoratedIcon = new Image[]{icon};
          objectInfo.getBroadcast(ObjectInfoPresentationDecorateIcon.class).invoke(
              objectInfo,
              decoratedIcon);
          icon = decoratedIcon[0];
        }
        // final icon
        return icon;
      }
    },
        null);
  }

  @Override
  public String getText(final Object element) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        ObjectInfo objectInfo = (ObjectInfo) element;
        String text = objectInfo.getPresentation().getText();
        // decorate
        {
          String[] decoratedText = new String[]{text};
          objectInfo.getBroadcast(ObjectInfoPresentationDecorateText.class).invoke(
              objectInfo,
              decoratedText);
          text = decoratedText[0];
        }
        // final text
        return text;
      }
    },
        "<exception, see log>");
  }
}
