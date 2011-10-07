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
package com.eviware.loadui.api.component.categories;

import java.util.List;
import java.util.Map;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.summary.SampleStats;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;

/**
 * Runner components take some type of sample, which takes some time and has
 * some result. Each sample results in a TerminalMessage which includes a
 * timestamp of when the sample was initiated, the time the sample took, and a
 * status boolean to indicate whether the sample succeeded or failed.
 * 
 * Each runner should be able to run multiple samples in parallel, and should
 * make the number of currently running samples available through the designated
 * OutputTerminal
 * 
 * @author dain.nilsson
 */
public interface RunnerCategory extends ComponentBehavior
{
	/**
	 * The String identifier of the category.
	 */
	public static final String CATEGORY = "Runners";

	/**
	 * An optional identifier for a sample (for filtering, etc)
	 */
	public static final String SAMPLE_ID = "ID";

	/**
	 * The color of the category.
	 */
	public static final String COLOR = "#98c206";

	/**
	 * Property for the max number of concurrent samples to run. Once this limit
	 * is reached, samples will be queued.
	 */
	public static final String CONCURRENT_SAMPLES_PROPERTY = "concurrentSamples";

	/**
	 * Property for the max size of queued samples. Once this queue is filled,
	 * additional sample requests will be dropped.
	 */
	public static final String MAX_QUEUE_SIZE_PROPERTY = "maxQueueSize";

	/**
	 * Property specifying whether to count discarded requests as failed
	 * requests.
	 */
	public static final String COUNT_DISCARDED_REQUESTS_PROPERTY = "countDiscarded";

	/**
	 * Counter for the number of samples that have been discarded due to queue
	 * overflow.
	 */
	public static final String DISCARDED_SAMPLES_COUNTER = "DiscardedSamples";

	/**
	 * Action which triggers sample.
	 */
	public static final String SAMPLE_ACTION = "SAMPLE";

	/**
	 * The label of the InputTerminal which is returned by getTriggerTerminal().
	 */
	public static final String TRIGGER_TERMINAL = "triggerTerminal";

	/**
	 * The label of the OutputTerminal which is returned by getResultTerminal().
	 */
	public static final String RESULT_TERMINAL = "resultTerminal";

	/**
	 * The label of the OutputTerminal which is returned by
	 * getCurrentlyRunningTerminal().
	 */
	public static final String CURRENLY_RUNNING_TERMINAL = "runningTerminal";

	/**
	 * The key to be used for the timestamp parameter in the result message.
	 */
	public static final String TIMESTAMP_MESSAGE_PARAM = "Timestamp";

	/**
	 * The key to be used for the time taken parameter in the result message.
	 */
	public static final String TIME_TAKEN_MESSAGE_PARAM = "TimeTaken";

	/**
	 * The key to be used for the status parameter in the result message.
	 */
	public static final String STATUS_MESSAGE_PARAM = "Status";

	/**
	 * The key to be used for the currently running parameter in the currently
	 * running message.
	 */
	public static final String CURRENTLY_RUNNING_MESSAGE_PARAM = "CurrentlyRunning";

	/**
	 * Returns the InputTerminal which is used to trigger a sample.
	 * 
	 * @return
	 */
	public InputTerminal getTriggerTerminal();

	/**
	 * Returns the OutputTerminal which is used to output the result of the
	 * sample.
	 * 
	 * @return
	 */
	public OutputTerminal getResultTerminal();

	/**
	 * Returns the OutputTerminal which is used to output the current number of
	 * running samples.
	 * 
	 * @return
	 */
	public OutputTerminal getCurrentlyRunningTerminal();

	/**
	 * Gets the Requests Counter.
	 * 
	 * @return
	 */
	public Counter getRequestCounter();

	/**
	 * Gets the Samples Counter.
	 * 
	 * @return
	 */
	public Counter getSampleCounter();

	/**
	 * Gets the Discarded Samples Counter.
	 * 
	 * @return
	 */
	public Counter getDiscardCounter();

	/**
	 * Gets the current number of queued samples.
	 * 
	 * @return
	 */
	public long getQueueSize();

	/**
	 * Gets a Map containing statistics collected by the Runner since the last
	 * RESET action.
	 * 
	 * @return
	 */
	public Map<String, String> getStatistics();

	public List<SampleStats> getTopSamples();

	public List<SampleStats> getBottomSamples();
}
