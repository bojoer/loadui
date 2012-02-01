/* 
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.fx;

import com.eviware.loadui.LoadUI;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import javafx.stage.Stage;
import javafx.scene.Scene;
import org.slf4j.LoggerFactory;
import com.eviware.loadui.fx.FxUtils.*;
import javafx.scene.image.*;
import javafx.stage.StageStyle;
import javafx.util.Properties;

import java.lang.System;
import java.lang.Exception;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.layout.UnitValue;

//import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.fx.ui.dialogs.*;


public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.JavaFXActivator" );

public-read var scene:Scene;
public-read var wc:WindowControllerImpl;

public function getScene() { scene };
public function getWindowController() { wc };

public def stylesheets = for( f in new File(".").list( new CssFilter() ) ) "file:{f}";

/**
 * An OSGi Activator in JavaFX. Launches the main window.
 * 
 * @author dain.nilsson
 */
public class JavaFXActivator extends BundleActivator {

	def icons = [
		Image { url: new File( "res/icon_32x32.png" ).toURI().toString() },
		Image { url: new File( "res/icon_16x16.png" ).toURI().toString() },
	];

	/**
	 * {@inheritDoc}
	 * 
	 * Creates a JavaFX Stage and Scene.
	 */
	override function start( bc: BundleContext ) {
		runInFxThread( function():Void {
			try {
				def jidedata = new Properties();
				jidedata.load( com.eviware.loadui.fx.Dummy.class.getResourceAsStream("/properties/jide.properties") );
				com.jidesoft.utils.Lm.verifyLicense(jidedata.get("company"), jidedata.get("product"), jidedata.get("license"));
			} catch( e:Exception ) {
			    e.printStackTrace();
			}
			
			def buttonOrder = PlatformDefaults.getButtonOrder();
			PlatformDefaults.setPlatform( PlatformDefaults.WINDOWS_XP ); //Always use Windows margins.
			PlatformDefaults.setButtonOrder( buttonOrder ); //Preserve platform button order.
			def lpy = new UnitValue( 10, UnitValue.LPY, null );
			def lpx = new UnitValue( 10, UnitValue.LPX, null );
			PlatformDefaults.setPanelInsets( lpy, lpx, lpy, lpx );

			log.debug("JavaFX Bundle started!");
			
			def name = System.getProperty(LoadUI.NAME);
	
			//def stylesheets = "file:style.css";
			
			// Instantiate objects to fix Classloading problems in tablelog. Do not remove.
			new org.jdesktop.swingx.JXTable();
			new javax.swing.JScrollPane();
			
			wc = WindowControllerImpl {
				windowTitleFilter: name
			};
			
			wc.stage = Stage {
				title: "{name} {LoadUI.VERSION}"
				visible: false
				icons: icons
				width: 1280.0
				height: 768.0
				scene: scene = Scene {
					width: 1200.0
					height: 700.0
					//width: 1085.0
					//height: 720.0
					stylesheets: bind stylesheets
					//stylesheets: "{__ROOT__}themes/default/style.css";//bind stylesheets
				}
				
				override function close() {
					if( not wc.doClose ) {
						if( AppState.byName("MAIN").state == MainWindow.TESTCASE_FRONT 
												or AppState.byName("MAIN").state == MainWindow.PROJECT_FRONT ) {
							ExitConfirmDialog{
								wc: wc
							};
						} else {
							ExitConfirmDialogWorkspace{
								wc: wc
							};
						}
						throw new com.eviware.loadui.util.hacks.PreventClosingStageException(); // this a hack to keep stage open
					} else {
						bc.getBundle( 0 ).stop();
						super.close();
					}
				}
			}
			
			log.debug( "JavaFX Stage created!" );
		});
	}

	override function stop( bc: BundleContext ) {
		log.debug("JavaFX Bundle stopped!");
	}
}

class CssFilter extends FilenameFilter {
	override function accept( dir, name ) {
		name.endsWith(".css")
	}
}