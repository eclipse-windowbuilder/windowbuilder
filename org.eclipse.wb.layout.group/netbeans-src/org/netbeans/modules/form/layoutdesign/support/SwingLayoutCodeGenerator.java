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
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutInterval;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.netbeans.modules.form.layoutdesign.VisualMapper;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generates Java layout code based on the passed layout model.
 *
 * @author Jan Stola
 */
public class SwingLayoutCodeGenerator {
	private static final String INDENT = "\t";
	private static final String LAYOUT_VAR_NAME = "layout"; // NOI18N
	private String layoutVarName;
	private boolean useLayoutLibrary;
	/**
	 * Maps from component ID to <code>ComponentInfo</code>.
	 */
	private final Map<String, ComponentInfo> componentIDMap;

	/**
	 * Creates new <code>SwingLayoutCodeGenerator</code>.
	 *
	 * @param layoutModel
	 *          layout model of the form.
	 */
	public SwingLayoutCodeGenerator(LayoutModel layoutModel) {
		componentIDMap = new HashMap<>();
	}

	/**
	 * Generates Java layout code for the specified container. The generated code is written to the
	 * <code>writer</code>.
	 *
	 * @param writer
	 *          the writer to generate the code into.
	 * @param container
	 *          the container whose code should be generated.
	 * @param contExprStr
	 *          code expression representing the container.
	 * @param contVarName
	 *          variable name of the container, used to derive the local variable name for the layout
	 *          instance
	 * @param infos
	 *          data about subcomponents.
	 * @param useLibrary
	 *          whether to use swing-layout library or Java 6 code
	 */
	public void generateContainerLayout(Writer writer,
			LayoutComponent container,
			String contVarName,
			ComponentInfo infos[],
			boolean useLibrary) throws IOException {
		useLayoutLibrary = useLibrary;
		if (contVarName == null) {
			layoutVarName = LAYOUT_VAR_NAME;
		} else {
			layoutVarName = contVarName;
		}
		fillMap(infos);
		// generateInstantiation(writer, contExprStr);
		LayoutInterval[][] extraRoots;
		int rootCount = container.getLayoutRootCount();
		if (rootCount > 1) {
			// prepare generating multiple roots into one group
			extraRoots = new LayoutInterval[LayoutConstants.DIM_COUNT][rootCount - 1];
			for (int i = 1; i < rootCount; i++) {
				for (int dim = 0; dim < LayoutConstants.DIM_COUNT; dim++) {
					extraRoots[dim][i - 1] = container.getLayoutRoot(i, dim);
					assert extraRoots[dim][i - 1].isParallel();
				}
			}
		} else {
			extraRoots = null;
		}
		for (int dim = 0; dim < LayoutConstants.DIM_COUNT; dim++) {
			StringBuilder sb = new StringBuilder();
			sb.append(INDENT);
			composeGroup(
					sb,
					container.getLayoutRoot(0, dim),
					extraRoots != null ? extraRoots[dim] : null,
							true,
							true,
							INDENT);
			writer.write(
					layoutVarName
					+ (dim == LayoutConstants.HORIZONTAL
					? ".setHorizontalGroup(\n"
							: ".setVerticalGroup(\n") // NOI18N
					+ sb.toString()
					+ "\n);\n"); // NOI18N
			sb = new StringBuilder();
			composeLinks(sb, container, layoutVarName, dim);
			writer.write(sb.toString());
		}
	}

	/**
	 * Fills the <code>componentIDMap</code>.
	 *
	 * @param infos
	 *          information about components.
	 */
	private void fillMap(ComponentInfo[] infos) {
		for (int counter = 0; counter < infos.length; counter++) {
			componentIDMap.put(infos[counter].id, infos[counter]);
		}
	}

	/**
	 * Generates the "header" of the code e.g. instantiation of the layout and call to the
	 * <code>setLayout</code> method.
	 */
	private void generateInstantiation(Writer writer, String contExprStr) throws IOException {
		writer.write(getLayoutName() + " " + layoutVarName + " "); // NOI18N
		writer.write("= new " + getLayoutName() + "(" + contExprStr + ");\n"); // NOI18N
		writer.write(contExprStr + ".setLayout(" + layoutVarName + ");\n"); // NOI18N
	}

	/**
	 * Generates layout code for a group that corresponds to the <code>interval</code>.
	 *
	 * @param layout
	 *          buffer to generate the code into.
	 * @param interval
	 *          layout model of the group.
	 */
	private void composeGroup(StringBuilder layout,
			LayoutInterval group,
			LayoutInterval[] extraGroups,
			boolean first,
			boolean last,
			String indent) throws IOException {
		int groupAlignment = group.getGroupAlignment();
		if (group.isParallel()) {
			boolean notResizable = group.getMaximumSize(false) == LayoutConstants.USE_PREFERRED_SIZE;
			String alignmentStr = convertAlignment(groupAlignment);
			layout.append(layoutVarName).append(".createParallelGroup("); // NOI18N
			layout.append(alignmentStr);
			if (notResizable) {
				layout.append(", false"); // NOI18N
			}
			layout.append(")"); // NOI18N
		} else {
			layout.append(layoutVarName).append(".createSequentialGroup()"); // NOI18N
		}
		Iterator<LayoutInterval> subIntervals = group.getSubIntervals();
		while (subIntervals.hasNext()) {
			layout.append("\n"); // NOI18N
			LayoutInterval subInterval = subIntervals.next();
			fillGroup(
					layout,
					subInterval,
					first,
					last && (!group.isSequential() || !subIntervals.hasNext() && extraGroups == null),
					groupAlignment,
					INDENT + indent);
			if (first && group.isSequential()) {
				first = false;
			}
		}
		if (extraGroups != null) {
			for (LayoutInterval g : extraGroups) {
				layout.append("\n"); // NOI18N
				fillGroup(layout, g, first, last, groupAlignment, INDENT);
				// assuming extra groups are always parallel
			}
		}
	}

	/**
	 * Generate layout code for one element in the group.
	 *
	 * @param layout
	 *          buffer to generate the code into.
	 * @param interval
	 *          layout model of the element.
	 * @param groupAlignment
	 *          alignment of the enclosing group.
	 */
	private void fillGroup(StringBuilder layout,
			LayoutInterval interval,
			boolean first,
			boolean last,
			int groupAlignment,
			String indent) throws IOException {
		if (interval.isGroup()) {
			layout.append(indent);
			layout.append(getAddGroupStr());
			int alignment = interval.getAlignment();
			if (alignment != LayoutConstants.DEFAULT
					&& interval.getParent().isParallel()
					&& alignment != groupAlignment
					&& alignment != LayoutConstants.BASELINE
					&& groupAlignment != LayoutConstants.BASELINE) {
				String alignmentStr = convertAlignment(alignment);
				layout.append(alignmentStr).append(", "); // NOI18N
			}
			composeGroup(layout, interval, null, first, last, indent);
		} else {
			int min = interval.getMinimumSize(false);
			int pref = interval.getPreferredSize(false);
			int max = interval.getMaximumSize(false);
			if (interval.isComponent()) {
				layout.append(indent);
				layout.append(getAddComponentStr());
				int alignment = interval.getAlignment();
				LayoutComponent layoutComp = interval.getComponent();
				ComponentInfo info = componentIDMap.get(layoutComp.getId());
				if (min == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
					int dimension = layoutComp.getLayoutInterval(LayoutConstants.HORIZONTAL) == interval
							? LayoutConstants.HORIZONTAL
									: LayoutConstants.VERTICAL;
					if (dimension == LayoutConstants.HORIZONTAL
							&& info.clazz.getName().equals("javax.swing.JComboBox")) { // Issue 68612 // NOI18N
						min = 0;
					} else if (pref >= 0) {
						int compMin =
								dimension == LayoutConstants.HORIZONTAL ? info.minSize.width : info.minSize.height;
						if (compMin > pref) {
							min = LayoutConstants.USE_PREFERRED_SIZE;
						}
					}
				}
				assert info.variableName != null;
				if (interval.getParent().isSequential()
						|| alignment == LayoutConstants.DEFAULT
						|| alignment == groupAlignment
						|| alignment == LayoutConstants.BASELINE
						|| groupAlignment == LayoutConstants.BASELINE) {
					layout.append(info.variableName);
				} else {
					String alignmentStr = convertAlignment(alignment);
					if (useLayoutLibrary()) {
						layout.append(alignmentStr).append(", ").append(info.variableName); // NOI18N
					} else {
						// in JDK the component comes first
						layout.append(info.variableName).append(", ").append(alignmentStr); // NOI18N
					}
				}
				int status = SwingLayoutUtils.getResizableStatus(info.clazz);
				if (!(pref == LayoutConstants.NOT_EXPLICITLY_DEFINED
						&& (min == LayoutConstants.NOT_EXPLICITLY_DEFINED
						|| min == LayoutConstants.USE_PREFERRED_SIZE
						&& !info.sizingChanged
						&& status == SwingLayoutUtils.STATUS_NON_RESIZABLE)
						&& (max == LayoutConstants.NOT_EXPLICITLY_DEFINED
						|| max == LayoutConstants.USE_PREFERRED_SIZE
						&& !info.sizingChanged
						&& status == SwingLayoutUtils.STATUS_NON_RESIZABLE
						|| max == Short.MAX_VALUE
						&& !info.sizingChanged
						&& status == SwingLayoutUtils.STATUS_RESIZABLE))) {
					layout.append(", "); // NOI18N
					generateSizeParams(layout, min, pref, max);
				}
			} else if (interval.isEmptySpace()) {
				boolean preferredGap;
				LayoutConstants.PaddingType gapType = interval.getPaddingType();
				if (interval.isDefaultPadding(false)) {
					if (gapType != null && gapType == LayoutConstants.PaddingType.SEPARATE) {
						// special case - SEPARATE padding not known by LayoutStyle
						preferredGap = false;
						if (min == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
							min = VisualMapper.PADDING_SEPARATE_VALUE;
						}
						if (pref == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
							pref = VisualMapper.PADDING_SEPARATE_VALUE;
						}
					} else {
						preferredGap = true;
					}
				} else {
					preferredGap = false;
				}
				if (preferredGap) {
					if (first || last) {
						layout.append(indent);
						layout.append(getAddContainerGapStr());
					} else {
						layout.append(indent);
						layout.append(getAddPreferredGapStr());
						if (gapType == LayoutConstants.PaddingType.INDENT) {
							// TBD: comp1, comp2
							pref = max = LayoutConstants.NOT_EXPLICITLY_DEFINED; // always fixed
						}
						layout.append(getPaddingTypeStr(gapType));
					}
					if (pref != LayoutConstants.NOT_EXPLICITLY_DEFINED
							|| max != LayoutConstants.NOT_EXPLICITLY_DEFINED
							// NOT_EXPLICITLY_DEFINED is the same as USE_PREFERRED_SIZE in this case
							&& max != LayoutConstants.USE_PREFERRED_SIZE) {
						if (!first && !last) {
							layout.append(',').append(' ');
						}
						layout.append(convertSize(pref)).append(", "); // NOI18N
						layout.append(convertSize(max));
					}
				} else {
					if (min == LayoutConstants.USE_PREFERRED_SIZE) {
						min = pref;
					}
					if (max == LayoutConstants.USE_PREFERRED_SIZE) {
						max = pref;
					}
					layout.append(indent);
					layout.append(getAddGapStr());
					if (min < 0) {
						min = pref; // min == GroupLayout.PREFERRED_SIZE
					}
					min = Math.min(pref, min);
					max = Math.max(pref, max);
					if (min == pref && pref == max) {
						layout.append(convertSize(pref));
					} else {
						generateSizeParams(layout, min, pref, max);
					}
				}
			} else {
				assert false;
			}
		}
		layout.append(")"); // NOI18N
	}

	/**
	 * Generates minimum/preferred/maximum size parameters..
	 *
	 * @param layout
	 *          buffer to generate the code into.
	 * @param min
	 *          minimum size.
	 * @param pref
	 *          preferred size.
	 * @param max
	 *          maximum size.
	 */
	private void generateSizeParams(StringBuilder layout, int min, int pref, int max) {
		layout.append(convertSize(min)).append(", "); // NOI18N
		layout.append(convertSize(pref)).append(", "); // NOI18N
		layout.append(convertSize(max));
	}

	/**
	 * Converts alignment from the layout model constants to <code>GroupLayout</code> constants.
	 *
	 * @param alignment
	 *          layout model alignment constant.
	 * @return <code>GroupLayout</code> alignment constant that corresponds to the given layout model
	 *         one.
	 */
	private String convertAlignment(int alignment) {
		String groupAlignment = null;
		switch (alignment) {
		case LayoutConstants.LEADING :
			groupAlignment = "LEADING";
			break; // NOI18N
		case LayoutConstants.TRAILING :
			groupAlignment = "TRAILING";
			break; // NOI18N
		case LayoutConstants.CENTER :
			groupAlignment = "CENTER";
			break; // NOI18N
		case LayoutConstants.BASELINE :
			groupAlignment = "BASELINE";
			break; // NOI18N
		default :
			assert false;
			break;
		}
		return useLayoutLibrary() ? getLayoutName() + "." + groupAlignment : // NOI18N
			getLayoutName() + ".Alignment." + groupAlignment; // NOI18N
	}

	/**
	 * Converts minimum/preferred/maximums size from the layout model constants to
	 * <code>GroupLayout</code> constants.
	 *
	 * @param size
	 *          minimum/preferred/maximum size from layout model.
	 * @return minimum/preferred/maximum size or <code>GroupLayout</code> constant that corresponds to
	 *         the given layout model one.
	 */
	private String convertSize(int size) {
		String convertedSize;
		switch (size) {
		case LayoutConstants.NOT_EXPLICITLY_DEFINED :
			convertedSize = getLayoutName() + ".DEFAULT_SIZE";
			break; // NOI18N
		case LayoutConstants.USE_PREFERRED_SIZE :
			convertedSize = getLayoutName() + ".PREFERRED_SIZE";
			break; // NOI18N
		case Short.MAX_VALUE :
			convertedSize = "Short.MAX_VALUE";
			break; // NOI18N
		default :
			assert size >= 0;
			convertedSize = Integer.toString(size);
			break;
		}
		return convertedSize;
	}

	private void composeLinks(StringBuilder layout,
			LayoutComponent containerLC,
			String layoutVarName,
			int dimension) throws IOException {
		Map<Integer, List<String>> linkSizeGroups =
				SwingLayoutUtils.createLinkSizeGroups(containerLC, dimension);
		Collection<List<String>> linkGroups = linkSizeGroups.values();
		Iterator<List<String>> linkGroupsIt = linkGroups.iterator();
		while (linkGroupsIt.hasNext()) {
			List<String> l = linkGroupsIt.next();
			// sort so that the generated line is always the same when no changes were made
			Collections.sort(l, (id1, id2) -> {
				ComponentInfo info1 = componentIDMap.get(id1);
				ComponentInfo info2 = componentIDMap.get(id2);
				return info1.variableName.compareTo(info2.variableName);
			});
			if (l.size() > 1) {
				layout.append("\n\n" + layoutVarName + ".linkSize("); // NOI18N
				if (!useLayoutLibrary()) {
					layout.append("javax.swing.SwingConstants"); // NOI18N
					layout.append(dimension == LayoutConstants.HORIZONTAL ? ".HORIZONTAL, " : ".VERTICAL, "); // NOI18N
				}
				layout.append("new java.awt.Component[] {"); //NOI18N
				Iterator<String> i = l.iterator();
				boolean first = true;
				while (i.hasNext()) {
					String cid = i.next();
					ComponentInfo info = componentIDMap.get(cid);
					if (first) {
						first = false;
						layout.append(info.variableName);
					} else {
						layout.append(", " + info.variableName); // NOI18N
					}
				}
				layout.append("}"); // NOI18N
				if (useLayoutLibrary()) {
					layout.append(", "); // NOI18N
					layout.append(getLayoutName());
					layout.append(dimension == LayoutConstants.HORIZONTAL ? ".HORIZONTAL" : ".VERTICAL"); // NOI18N
				}
				layout.append(");\n\n"); // NOI18N
			}
		}
	}

	/**
	 * Information about one component.
	 */
	public static class ComponentInfo {
		/** ID of the component. */
		public String id;
		/** Variable name of the component. */
		public String variableName;
		/** The component's class. */
		public Class<?> clazz;
		/** The component's minimum size. */
		public Dimension minSize;
		/**
		 * Determines whether size properties (e.g. minimumSize, preferredSize or maximumSize properties
		 * of the component has been changed).
		 */
		public boolean sizingChanged;
	}

	// -----
	// type of generated code: swing-layout library vs JDK
	boolean useLayoutLibrary() {
		return useLayoutLibrary;
	}

	private String getLayoutName() {
		return useLayoutLibrary() ? "org.jdesktop.layout.GroupLayout" : "javax.swing.GroupLayout"; // NOI18N
	}

	private String getLayoutStyleName() {
		return useLayoutLibrary() ? "org.jdesktop.layout.LayoutStyle" : "javax.swing.LayoutStyle"; // NOI18N
	}

	private String getAddComponentStr() {
		return useLayoutLibrary() ? ".add(" : ".addComponent("; // NOI18N
	}

	private String getAddGapStr() {
		return useLayoutLibrary() ? ".add(" : ".addGap("; // NOI18N
	}

	private String getAddPreferredGapStr() {
		return ".addPreferredGap("; // NOI18N
	}

	private String getAddContainerGapStr() {
		return ".addContainerGap("; // NOI18N
	}

	private String getAddGroupStr() {
		return useLayoutLibrary() ? ".add(" : ".addGroup("; // NOI18N
	}

	private String getPaddingTypeStr(LayoutConstants.PaddingType paddingType) {
		String str;
		if (paddingType == null || paddingType == LayoutConstants.PaddingType.RELATED) {
			str = ".RELATED"; // NOI18N
		} else if (paddingType == LayoutConstants.PaddingType.UNRELATED) {
			str = ".UNRELATED"; // NOI18N
		} else if (paddingType == LayoutConstants.PaddingType.INDENT) {
			str = ".INDENT"; // NOI18N
		} else {
			return null;
		}
		return getLayoutStyleName()
				+ (useLayoutLibrary ? "" : ".ComponentPlacement") // NOI18N
				+ str;
	}
}
