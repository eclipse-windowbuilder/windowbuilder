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
package org.eclipse.wb.internal.layout.group.model;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Clipboard command for copy/paste a container with GroupLayout.
 * 
 * @author mitin_aa
 */
public abstract class GroupLayoutClipboardCommand extends ClipboardCommand {
  private static final long serialVersionUID = 0L;
  private final String m_layoutXml;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutClipboardCommand(GroupLayoutSupport _this) {
    Map<String, String> componentsMap = Maps.newHashMap();
    // map current component ids to xml ids; this requires child order preserving
    int i = 0;
    for (AbstractComponentInfo component : _this.getLayoutChildren()) {
      componentsMap.put(ObjectInfoUtils.getId(component), "" + i++);
    }
    // store to xml
    m_layoutXml =
        "<layout>"
            + _this.getLayoutModel().saveContainerLayout(
                _this.getLayoutRoot(),
                componentsMap,
                0,
                false)
            + "</layout>";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClipboardCommand
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void execute(JavaInfo javaInfo) throws Exception {
    try {
      // prepare
      GroupLayoutSupport _this = getLayoutSupport(javaInfo);
      // map xml ids back to newly created component ids
      Map<String, String> componentsMap = Maps.newHashMap();
      int i = 0;
      for (AbstractComponentInfo component : _this.getLayoutChildren()) {
        componentsMap.put("" + i++, ObjectInfoUtils.getId(component));
      }
      // parse xml
      Document document =
          DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
              IOUtils.toInputStream(m_layoutXml));
      NodeList nodes = document.getChildNodes().item(0).getChildNodes();
      // load
      _this.getLayoutModel().loadContainerLayout(
          ObjectInfoUtils.getId(_this.getLayoutContainer()),
          nodes,
          componentsMap);
      // save
      _this.saveLayout();
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  protected abstract GroupLayoutSupport getLayoutSupport(JavaInfo container);
}