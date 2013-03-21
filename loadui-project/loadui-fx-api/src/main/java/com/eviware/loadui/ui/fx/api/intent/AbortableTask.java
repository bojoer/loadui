package com.eviware.loadui.ui.fx.api.intent;

/**
 * A task that can be triggered with a {@link IntentEvent#INTENT_RUN_BLOCKING_ABORTABLE}.
 * If a task is aborted, the 'onRun' Runnable may be interrupted if it's already running,
 * and then the OnAbort Runnable will be called.
 * @author renato
 *
 */
public class AbortableTask
{

	Runnable onRun;
	Runnable onAbort;

	private AbortableTask( final Runnable onRun, Runnable onAbort )
	{
		this.onRun = onRun;
		this.onAbort = onAbort;
	}

	/**
	 * Sets the onRun Runnable
	 * @param run
	 * @return OnRun which allows you to set the onAbort Runnable
	 */
	public static OnRun onRun( Runnable run )
	{
		return new OnRun( run );
	}
	
	public static class OnRun
	{

		private Runnable onRun;

		private OnRun( Runnable onRun )
		{
			this.onRun = onRun;
		}

		/**
		 * Sets the onAbort Runnable. This will be run only if the user aborts the task. 
		 * @param run
		 * @return {@link AbortableTask}
		 */
		public AbortableTask onAbort( Runnable run )
		{
			return new AbortableTask( onRun, run );
		}

	}

}
