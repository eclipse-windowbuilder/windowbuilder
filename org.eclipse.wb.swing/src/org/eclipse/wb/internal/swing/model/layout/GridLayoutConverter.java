/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.HorizontalAlignment;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.VerticalAlignment;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.grid.AbstractGridConverter;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper for converting coordinates of {@link ComponentInfo} children to {@link GridLayoutInfo}.
 *
 * @author sablin_aa
 * @coverage swing.model.layout
 */
public final class GridLayoutConverter extends AbstractGridConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private GridLayoutConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Conversion
	//
	////////////////////////////////////////////////////////////////////////////
	public static void convert(final ContainerInfo container, final GridLayoutInfo layout)
			throws Exception {
		GridLayoutContainer gridContainer = new GridLayoutContainer(container);
		GridLayoutInstance gridLayout = new GridLayoutInstance(gridContainer, layout);
		convert(gridContainer, gridLayout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Interfaces implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation {@link IGridLayoutContainer}.
	 */
	protected static class GridLayoutContainer implements IGridLayoutContainer {
		private final ContainerInfo m_container;

		GridLayoutContainer(ContainerInfo container) {
			m_container = container;
		}

		@Override
		public IAbstractComponentInfo getComponent() {
			return m_container;
		}

		public ContainerInfo getContainer() {
			return m_container;
		}

		@Override
		public List<IAbstractComponentInfo> getControls() {
			List<IAbstractComponentInfo> controls = new ArrayList<>();
			controls.addAll(m_container.getChildrenComponents());
			return controls;
		}

		public List<ComponentInfo> getComponents() {
			return m_container.getChildrenComponents();
		}
	}
	/**
	 * Implementation {@link IGridLayoutInstance}.
	 */
	protected static class GridLayoutInstance implements IGridLayoutInstance {
		private final GridLayoutContainer m_container;
		private final GridLayoutInfo m_layout;

		GridLayoutInstance(GridLayoutContainer container, GridLayoutInfo layout) {
			m_container = container;
			m_layout = layout;
		}

		@Override
		public IGridLayoutContainer getContainer() {
			return m_container;
		}

		public GridLayoutContainer getContainerEx() {
			return m_container;
		}

		private final Map<Object, GridLayoutData> m_layoutDatas = new HashMap<>();

		@Override
		public IGridLayoutData getLayoutData(IAbstractComponentInfo control) {
			return getLayoutDataEx(control);
		}

		public GridLayoutData getLayoutDataEx(IAbstractComponentInfo control) {
			GridLayoutData layoutData = m_layoutDatas.get(control);
			if (layoutData == null) {
				layoutData = new GridLayoutData(this, control);
				m_layoutDatas.put(control, layoutData);
			}
			return layoutData;
		}

		@Override
		public void setColumnCount(int value) throws Exception {
			m_layout.getPropertyByTitle("rows").setValue(0);
			m_layout.getPropertyByTitle("columns").setValue(value);
		}

		private int getColumnCount() throws Exception {
			Object value = m_layout.getPropertyByTitle("columns").getValue();
			return value == null || value == Property.UNKNOWN_VALUE ? 0 : (Integer) value;
		}

		@Override
		public void applyChanges() throws Exception {
			List<ComponentInfo> components = m_container.getComponents();
			List<ComponentInfo> fillers = new ArrayList<>();
			int colCount = getColumnCount();
			// calculate rows count
			int rowCount;
			if (colCount == 0) {
				colCount = 1;
				rowCount = components.size();
			} else {
				rowCount = (components.size() - 1) / colCount + 1;
			}
			for (ComponentInfo component : components) {
				GridLayoutData layoutData = getLayoutDataEx(component);
				if (layoutData != null) {
					rowCount = Math.max(rowCount, layoutData.y + layoutData.h);
				}
			}
			// fill cells by components & fillers
			ComponentInfo gridCells[][] = new ComponentInfo[colCount][rowCount];
			for (ComponentInfo component : components) {
				GridLayoutData layoutData = getLayoutDataEx(component);
				if (layoutData.x >= 0 && layoutData.y >= 0) {
					// fill multi cells
					for (int x = layoutData.x; x < layoutData.x + layoutData.w; x++) {
						for (int y = layoutData.y; y < layoutData.y + layoutData.h; y++) {
							if (x == layoutData.x && y == layoutData.y) {
								// destination cell
								ComponentInfo cell = gridCells[x][y];
								if (cell == null || fillers.contains(cell)) {
									// place component
									gridCells[x][y] = component;
									fillers.remove(cell);
								} else {
									// collision detected
									DesignerPlugin.log("swing.model.layout.GridLayout_Converter.convert(ContainerInfo, GridLayoutInfo) collision: in cell ("
											+ x
											+ ","
											+ y
											+ ")");
								}
							} else {
								// add filler if empty
								if (gridCells[x][y] == null) {
									// create filler
									ComponentInfo emptyLabel = createFiller();
									gridCells[x][y] = emptyLabel;
									fillers.add(emptyLabel);
								}
							}
						}
					}
				}
			}
			// reorder components for correct flow
			ContainerInfo parent = m_container.getContainer();
			List<ComponentInfo> childrenComponents = parent.getChildrenComponents();
			ComponentInfo referenceComponent =
					childrenComponents.size() == 0 ? null : childrenComponents.get(0);
			for (int y = 0; y < rowCount; y++) {
				for (int x = 0; x < colCount; x++) {
					ComponentInfo cell = gridCells[x][y];
					if (cell == null) {
						cell = createFiller();
						fillers.add(cell);
					}
					if (cell.getParent() == parent) {
						// move to new place
						if (cell == referenceComponent) {
							// skip current referenced
							referenceComponent =
									GenericsUtils.getNextOrNull(parent.getChildrenComponents(), referenceComponent);
						} else {
							// move
							m_layout.move(cell, referenceComponent);
						}
					} else {
						// add to container
						m_layout.add(cell, referenceComponent);
					}
					// clear text in filler label
					if (fillers.contains(cell)) {
						cell.getPropertyByTitle("text").setValue("");
					}
				}
			}
		}

		private ComponentInfo createFiller() throws Exception {
			ComponentInfo emptyLabel =
					(ComponentInfo) JavaInfoUtils.createJavaInfo(
							m_layout.getEditor(),
							"javax.swing.JLabel",
							new ConstructorCreationSupport());
			return emptyLabel;
		}
	}
	/**
	 * Implementation {@link IGridLayoutData}.
	 */
	protected static class GridLayoutData implements IGridLayoutData {
		private final GridLayoutInstance m_layout;
		private final IAbstractComponentInfo m_control;
		int x = -1;
		int y = -1;
		int w = 1;
		int h = 1;

		GridLayoutData(GridLayoutInstance layout, IAbstractComponentInfo control) {
			m_layout = layout;
			m_control = control;
		}

		@Override
		public IAbstractComponentInfo getComponent() {
			return m_control;
		}

		@Override
		public IGridLayoutInstance getLayout() {
			return m_layout;
		}

		@Override
		public void setGridX(int value) throws Exception {
			x = value;
		}

		@Override
		public void setGridY(int value) throws Exception {
			y = value;
		}

		@Override
		public void setSpanX(int value) throws Exception {
			w = value;
		}

		@Override
		public void setSpanY(int value) throws Exception {
			h = value;
		}

		@Override
		public void setHorizontalGrab(boolean value) throws Exception {
			// NONE
		}

		@Override
		public void setVerticalGrab(boolean value) throws Exception {
			// NONE
		}

		@Override
		public void setHorizontalAlignment(HorizontalAlignment value) throws Exception {
			// NONE
		}

		@Override
		public void setVerticalAlignment(VerticalAlignment value) throws Exception {
			// NONE
		}
	}
}
