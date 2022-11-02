/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.model.network.Node;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") public abstract class PotentialPanel extends JPanel {
	private List<PanelResizeEventListener> listeners;
	/**
	 * If true, values inside the panel will not be editable
	 */
	private boolean readOnly;

	public PotentialPanel() {
		listeners = new ArrayList<>();
	}

	/**
	 * Fill the panel with the data from the node
	 *
	 * @param node
	 */
	public abstract void setData(Node node);

	/**
	 * Modify the node according to the changes entered by the user in the panel
	 */
	public boolean saveChanges() {
		close();
		return true;
	}

	public abstract void close();

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @param readOnly the readOnly to set
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void suscribePanelResizeEventListener(PanelResizeEventListener listener) {
		listeners.add(listener);
	}

	public boolean unsuscribePanelResizeEventListener(PanelResizeEventListener listener) {
		return listeners.remove(listener);
	}

	public void notifyPanelResizeEventListeners() {
		PanelResizeEvent event = new PanelResizeEvent(this, getSize());
		for (PanelResizeEventListener listener : listeners) {
			listener.panelSizeChanged(event);
		}
	}

}
