/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General Public License
 * Version 2 only ("GPL") or the Common Development and Distribution License("CDDL") (collectively,
 * the "License"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP.
 * See the License for the specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header Notice in each file and
 * include the License file at nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this particular file
 * as subject to the "Classpath" exception as provided by Sun in the GPL Version 2 section of the
 * License file that accompanied this code. If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software is Sun
 * Microsystems, Inc. Portions Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the GPL Version 2,
 * indicate your decision by adding "[Contributor] elects to include this software in this
 * distribution under the [CDDL or GPL Version 2] license." If you do not indicate a single choice
 * of license, a recipient has the option to distribute your version of this file under either the
 * CDDL, the GPL Version 2 or to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then the
 * option applies only if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.layoutdesign.support;

import org.netbeans.modules.form.layoutdesign.LayoutComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for swing layout support.
 * 
 * @author Jan Stola
 */
public class SwingLayoutUtils {
  /** The default resizability of the component is not known. */
  public static final int STATUS_UNKNOWN = -1;
  /** The component is not resizable by default. */
  public static final int STATUS_NON_RESIZABLE = 0;
  /** The component is resizable by default. */
  public static final int STATUS_RESIZABLE = 1;
  /**
   * Contains class names of non-resizable components e.g. components that are non-resizable unless
   * one (or more) of minimumSize, preferredSize or maximumSize properties is changed.
   */
  private static Set<String> nonResizableComponents = new HashSet<String>();
  static {
    nonResizableComponents.addAll(Arrays.asList(new String[]{"javax.swing.JLabel", // NOI18N
        "javax.swing.JButton", // NOI18N
        "javax.swing.JToggleButton", // NOI18N
        "javax.swing.JCheckBox", // NOI18N
        "javax.swing.JRadioButton", // NOI18N
        "javax.swing.JList", // NOI18N
    }));
  }
  /**
   * Contains class names of resizable components e.g. components that are resizable unless one (or
   * more) of minimumSize, preferredSize or maximumSize properties is changed.
   */
  private static Set<String> resizableComponents = new HashSet<String>();
  static {
    resizableComponents.addAll(Arrays.asList(new String[]{"javax.swing.JComboBox", // NOI18N
        "javax.swing.JTextField", // NOI18N
        "javax.swing.JTextArea", // NOI18N
        "javax.swing.JTabbedPane", // NOI18N
        "javax.swing.JScrollPane", // NOI18N
        "javax.swing.JSplitPane", // NOI18N
        "javax.swing.JFormattedTextField", // NOI18N
        "javax.swing.JPasswordField", // NOI18N
        "javax.swing.JSpinner", // NOI18N
        "javax.swing.JSeparator", // NOI18N
        "javax.swing.JTextPane", // NOI18N
        "javax.swing.JEditorPane", // NOI18N
        "javax.swing.JInternalFrame", // NOI18N
        "javax.swing.JLayeredPane", // NOI18N
        "javax.swing.JDesktopPane" // NOI18N
    }));
  }

  /**
   * Determines whether the given class represents component that is resizable (by default) or not.
   * 
   * @param componentClass
   *          <code>Class</code> object corresponding to component we are interested in.
   * @return <code>STATUS_RESIZABLE</code>, <code>STATUS_NON_RESIZABLE</code> or
   *         <code>STATUS_UNKNOWN</code>.
   */
  public static int getResizableStatus(Class componentClass) {
    String className = componentClass.getName();
    if (resizableComponents.contains(className)) {
      return STATUS_RESIZABLE;
    }
    if (nonResizableComponents.contains(className)) {
      return STATUS_NON_RESIZABLE;
    }
    return STATUS_UNKNOWN;
  }

  public static Map<Integer, List<String>> createLinkSizeGroups(LayoutComponent layoutComponent,
      int dimension) {
    Map<Integer, List<String>> linkSizeGroup = new HashMap<Integer, List<String>>();
    if (layoutComponent.isLayoutContainer()) {
      for (LayoutComponent lc : layoutComponent.getSubcomponents()) {
        if (lc != null) {
          if (lc.isLinkSized(dimension)) {
            String cid = lc.getId();
            Integer id = new Integer(lc.getLinkSizeId(dimension));
            List<String> l = linkSizeGroup.get(id);
            if (l == null) {
              l = new ArrayList<String>();
              l.add(cid);
              linkSizeGroup.put(id, l);
            } else {
              l.add(cid);
            }
          }
        }
      }
    }
    return linkSizeGroup;
  }
}
