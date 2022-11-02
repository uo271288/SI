/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import javax.swing.*;
import java.net.URL;

/**
 * SplashScreenOpenMarkov Splash Screen Loader in OpenMarkov to prevent impatient user
 * and to show the progress of loading elements in the Main Program
 *
 * @author jlgozalo
 * @version 1.0 16/11/2008
 */
public class SplashScreenLoader {

	/**
	 * the logo file
	 */
	private final String logoFile = "images/OpenMarkovSplash.jpg";
	private SplashScreen splash;

	/**
	 * start the splash screen, do work and destroy
	 */
	public SplashScreenLoader() {

		/*
		 * splashScreenInit(); simulateDoingWork(); splashScreenDestroy();
		 */
	}

	/**
	 * This method draws on the splash screen.
	 */
	public void splashScreenInit() {

		// TODO externalize to OpenMarkov Properties the string for the icon

		URL url = this.getClass().getClassLoader().getResource(logoFile);
		ImageIcon myImage = new ImageIcon(url);
		splash = new SplashScreen(myImage);
		splash.setLocationRelativeTo(null);
		splash.setProgressMax(100);
		splash.setScreenVisible(true);

	}

	/**
	 * simulate the main program is being loaded
	 */
	public void doingWork() {

		// do something here to simulate the program doing something that
		// is time consuming
		/*String poop = "";
		for (int i = 0; i <= 1000; i++) {
			for (long j = 0; j < 2000; ++j) {
				poop = " " + (j + i);
			}
			
		}
		*/

	}

	/**
	 * destroy the splash Screen turning not visible
	 */
	public void splashScreenDestroy() {

		splash.setScreenVisible(false);
	}

	/**
	 * get splash
	 *
	 * @return aSplash The real splash screen
	 */
	public SplashScreen getSplash() {

		return splash;
	}

}
