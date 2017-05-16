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
package org.eclipse.wb.internal.core.model;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

import java.util.List;

/**
 * Broadcasts based functionality for {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage code.model
 */
public final class JavaInfoRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new JavaInfoRootProcessor();

  private JavaInfoRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
    processRoot(root);
  }

  private static void processRoot(final JavaInfo rootJavaInfo) throws Exception {
    rootJavaInfo.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void dispose() throws Exception {
        JavaInfoUtils.getState(rootJavaInfo).dispose();
      }
    });
    rootJavaInfo.addBroadcastListener(new EditorActivatedListener() {
      public void invoke(EditorActivatedRequest request) throws Exception {
        if (JavaInfoUtils.isDependencyChanged(rootJavaInfo)) {
          request.requestReparse();
        }
      }
    });
    // visibility in tree/GEF
    rootJavaInfo.addBroadcastListener(new ObjectInfoChildTree() {
      public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
        if (object instanceof JavaInfo) {
          JavaInfo javaInfo = (JavaInfo) object;
          String visibilityTreeString = JavaInfoUtils.getParameter(javaInfo, "visible.inTree");
          if (visibilityTreeString != null) {
            visible[0] = Boolean.parseBoolean(visibilityTreeString);
          } else {
            String visibilityString = JavaInfoUtils.getParameter(javaInfo, "visible");
            if (visibilityString != null) {
              visible[0] = Boolean.parseBoolean(visibilityString);
            }
          }
        }
      }
    });
    rootJavaInfo.addBroadcastListener(new ObjectInfoChildGraphical() {
      public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
        if (object instanceof JavaInfo) {
          JavaInfo javaInfo = (JavaInfo) object;
          String visibilityGraphString =
              JavaInfoUtils.getParameter(javaInfo, "visible.inGraphical");
          if (visibilityGraphString != null) {
            visible[0] = Boolean.parseBoolean(visibilityGraphString);
          } else {
            String visibilityString = JavaInfoUtils.getParameter(javaInfo, "visible");
            if (visibilityString != null) {
              visible[0] = Boolean.parseBoolean(visibilityString);
            }
          }
        }
      }
    });
    // text decoration
    rootJavaInfo.addBroadcastListener(new ObjectInfoPresentationDecorateText() {
      public void invoke(ObjectInfo object, String[] text) throws Exception {
        if (object instanceof JavaInfo) {
          JavaInfo javaInfo = (JavaInfo) object;
          IPreferenceStore preferences = javaInfo.getDescription().getToolkit().getPreferences();
          if (preferences.getBoolean(IPreferenceConstants.P_GENERAL_TEXT_SUFFIX)) {
            broadcast_presentation_decorateText(javaInfo, text);
          }
        }
      }
    });
  }

  /**
   * Adds "text" property prefix to the given presentation text of this {@link JavaInfo}.
   */
  private static void broadcast_presentation_decorateText(JavaInfo javaInfo, String[] text)
      throws Exception {
    for (Property property : javaInfo.getProperties()) {
      if (property instanceof GenericPropertyImpl) {
        GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
        GenericPropertyDescription propertyDescription = genericProperty.getDescription();
        if (propertyDescription != null
            && propertyDescription.hasTrueTag("isText")
            && genericProperty.getJavaInfo() == javaInfo
            && genericProperty.isModified()) {
          String suffix = (String) genericProperty.getValue();
          text[0] = text[0] + " - \"" + suffix + "\"";
          break;
        }
      }
    }
  }
}