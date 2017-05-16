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
package org.eclipse.wb.internal.core.editor.errors.report2;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.ErrorDialog;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.prefs.BackingStoreException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Fetches WindowBuilder preferences.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public final class PreferencesFileReportEntry extends FileReportEntry {
  private static final String EXPORT_ERROR_MESSAGE =
      Messages.PreferencesFileReportEntry_errorMessage;
  private static final String PREFERENCES_PREFIX = "org.eclipse.wb";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PreferencesFileReportEntry() {
    super("preferences.epf");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractFileReportInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected InputStream getContents() {
    IStatus exportStatus = null;
    try {
      // get preference service
      IPreferencesService service = Platform.getPreferencesService();
      // we need instance scope only
      IEclipsePreferences node =
          (IEclipsePreferences) service.getRootNode().node(InstanceScope.SCOPE);
      // prepare list of preferences to exclude to pass later into export method
      final List<String> excludesList = Lists.newArrayList();
      node.accept(new IPreferenceNodeVisitor() {
        public boolean visit(IEclipsePreferences childNode) throws BackingStoreException {
          // don't exclude root instance node
          if (childNode.name().equals(InstanceScope.SCOPE)) {
            return true;
          }
          // exclude all which not relative do WindowBuilder
          if (!StringUtils.contains(childNode.absolutePath(), PREFERENCES_PREFIX)) {
            excludesList.add(childNode.name());
          }
          return true;
        }
      });
      // do export
      ByteArrayOutputStream exportStream = new ByteArrayOutputStream();
      exportStatus =
          service.exportPreferences(
              node,
              exportStream,
              excludesList.toArray(new String[excludesList.size()]));
      if (exportStatus.getCode() != IStatus.OK) {
        throw new RuntimeException(EXPORT_ERROR_MESSAGE);
      }
      // return input stream to be saved
      return new ByteArrayInputStream(exportStream.toByteArray());
    } catch (Throwable e) {
      DesignerPlugin.log(e);
      // show error
      ErrorDialog.openError(
          DesignerPlugin.getShell(),
          Messages.PreferencesFileReportEntry_errorTitle,
          EXPORT_ERROR_MESSAGE,
          exportStatus == null
              ? new Status(IStatus.ERROR, DesignerPlugin.PLUGIN_ID, 0, null, e)
              : exportStatus);
    }
    return null;
  }
}
