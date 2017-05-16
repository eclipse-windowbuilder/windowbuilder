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
package org.eclipse.wb.internal.core.gef.policy.nonvisual;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.part.nonvisual.NonVisualBeanEditPart;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Implementation of {@link ILayoutRequestValidator} for validate only <i>non-visual bean</i>
 * objects.
 *
 * @author lobas_av
 * @coverage core.gef.policy.nonvisual
 */
final class NonVisualValidator implements ILayoutRequestValidator {
  protected final JavaInfo m_infoForMemento;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NonVisualValidator(JavaInfo infoForMemento) {
    m_infoForMemento = infoForMemento;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create/Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean validateCreateRequest(EditPart host, CreateRequest request) {
    if (!acceptDropNonVisualBeans()) {
      return false;
    }
    final Object newObject = request.getNewObject();
    if (newObject instanceof JavaInfo) {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
        public Boolean runObject() throws Exception {
          JavaInfo newInfo = (JavaInfo) newObject;
          return validateJavaInfo(newInfo);
        }
      }, false);
    }
    return false;
  }

  public boolean validatePasteRequest(EditPart host, final PasteRequest request) {
    if (!acceptDropNonVisualBeans()) {
      return false;
    }
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
      @SuppressWarnings("unchecked")
      public Boolean runObject() throws Exception {
        List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
        if (mementos.size() == 1) {
          JavaInfo newInfo = mementos.get(0).create(m_infoForMemento);
          return validateJavaInfo(newInfo);
        }
        return false;
      }
    }, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add/Move
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
    return validateMoveRequest(host, request);
  }

  public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
    for (EditPart part : request.getEditParts()) {
      if (!(part instanceof NonVisualBeanEditPart)) {
        return false;
      }
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static boolean validateJavaInfo(JavaInfo newInfo) throws Exception {
    ComponentDescription description = newInfo.getDescription();
    // ignore special models (absolute layout and etc)
    if (description.getComponentClass() == null) {
      return false;
    }
    // ignore is disabled
    if (JavaInfoUtils.hasTrueParameter(newInfo, "NVO.disabled")) {
      return false;
    }
    // validate only java bean objects
    String source = newInfo.getCreationSupport().add_getSource(null);
    return !StringUtils.contains(source, "%parent%") && !StringUtils.contains(source, "%child%");
  }

  protected static boolean acceptDropNonVisualBeans() {
    return DesignerPlugin.getPreferences().getBoolean(
        IPreferenceConstants.P_COMMON_ACCEPT_NON_VISUAL_BEANS);
  }
}