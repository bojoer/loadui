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
package com.eviware.loadui.fx.async;

import javafx.async.RunnableFuture;
import javafx.async.JavaTaskBase;
import java.lang.Runnable;

public class BlockingTask extends JavaTaskBase {
	public-init var runnable:Runnable;
	public-init var task:function():Void;
	
	public override function create() : RunnableFuture {
		new RunnableRunnableFuture(
			if( FX.isInitialized( runnable ) ) runnable else TaskRunnable { task: task }
		)
	}
}

class TaskRunnable extends Runnable {
	public-init var task:function():Void;
	
	override function run():Void {
		task();
	}
}