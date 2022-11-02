/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.openmarkov.gui.dialog.common.OkCancelHorizontalDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Dialog box used to open a network from a URL.
 *
 * @author artasom
 * @version 1.0
 */
public class URLNetworkChooserDialog extends OkCancelHorizontalDialog {

	private static final long serialVersionUID = -5995268997231553014L;
	/**
	 * Text field for the url.
	 */
	private JTextField urlTextField = null;

	/**
	 * Contents panel.
	 */
	private JPanel contentsPanel = null;

	private URL networkURL;

	/**
	 * initialises and configures the dialog box.
	 *
	 * @param owner window that owns the dialog box.
	 */
	public URLNetworkChooserDialog(Window owner) {
		super(owner);
		initialize();
		setLocationRelativeTo(owner);
	}

	/**
	 * @param url a URL
	 * @return the final URL in case of redirection
	 * @throws IOException http://stackoverflow.com/questions/14951696/java-urlconnection-get-the-final-redirected-url
	 */
	public static String getFinalURL(String url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setInstanceFollowRedirects(false);
		con.connect();
		con.getInputStream();

		if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
				|| con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
			String redirectUrl = con.getHeaderField("Location");
			return getFinalURL(redirectUrl);
		}
		return url;
	}

	/**
	 * This method initialises this instance.
	 */
	private void initialize() {
		Dimension dialogDimension = new Dimension(400, 120);
		setSize(dialogDimension);
		setTitle(stringDatabase.getString("SelectNetworkURL.Title.Label"));
		configureComponentsPanel();
		setMinimumSize(dialogDimension);
		setMaximumSize(dialogDimension);
		setPreferredSize(dialogDimension);
		pack();
	}

	/**
	 * Sets up the panel where all components, except the buttons of the buttons
	 * panel, will be appear.
	 */
	private void configureComponentsPanel() {
		getComponentsPanel().setLayout(new BorderLayout());
		getComponentsPanel().add(getContentsPanel(), BorderLayout.CENTER);
	}

	/**
	 * This method initialises contentsPanel.
	 *
	 * @return a new contentsPanel.
	 */
	private JPanel getContentsPanel() {
		if (contentsPanel == null) {
			contentsPanel = new JPanel();
			contentsPanel.setLayout(new GridLayout(0, 1, 0, 0));
			contentsPanel.setBorder(BorderFactory
					.createTitledBorder(null, stringDatabase.getString("NetworkURLValues.Title.Label"),
							TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null,
							new Color(51, 51, 51)));
			contentsPanel.add(getURLTextField(), null);
		}
		return contentsPanel;
	}

	/**
	 * This method initialises the URL text field.
	 *
	 * @return a new text field to indicate the network URL.
	 */
	private JTextField getURLTextField() {
		if (urlTextField == null) {
			urlTextField = new JTextField();
		}
		return urlTextField;
	}

	/**
	 * This method carries out the actions when the user press the Ok button
	 * before hide the dialog.
	 *
	 * @return true always and set the URL of the network that can be retrieved with the method getNetworkURL
	 */
	@Override protected boolean doOkClickBeforeHide() {
		try {
			if (!urlTextField.getText().isEmpty()) {
				String finalURL = getFinalURL(urlTextField.getText().trim());
				networkURL = new URL(finalURL);
			} else {
				networkURL = null;
			}
		} catch (IOException e) {
			networkURL = null;
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * This method carries out the actions when the user press the Cancel button
	 * before hiding the dialog.
	 */
	@Override protected void doCancelClickBeforeHide() {
		networkURL = null;
	}

	/**
	 * Returns a URL .
	 *
	 * @return the URL of the network.
	 */
	public URL getNetworkURL() {
		return networkURL;
	}

	public int requestNetworkURL() {
		setVisible(true);
		return selectedButton;
	}

}
