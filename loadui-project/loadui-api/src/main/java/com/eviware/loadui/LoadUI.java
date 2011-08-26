package com.eviware.loadui;

public class LoadUI
{
	/**
	 * The main version number of loadUI.
	 */
	public static final String VERSION = "2.0.0-SNAPSHOT";

	/**
	 * Internal version number used to determine controller/agent compatibility.
	 * Compatibility is only ensured when this version string is the same for
	 * both agent and controller.
	 */
	public static final String AGENT_VERSION = "7";

	public static final String INSTANCE = "loadui.instance";
	public static final String CONTROLLER = "controller";
	public static final String AGENT = "agent";

	public static final String BUILD_NUMBER = "loadui.build.number";
	public static final String BUILD_DATE = "loadui.build.date";

	public static final String LOADUI_HOME = "loadui.home";

	public static final String HTTP_PORT = "loadui.http.port";
	public static final String HTTPS_PORT = "loadui.https.port";

	public static final String DISABLE_STATISTICS = "loadui.statistics.disable";

	public static final String KEY_STORE = "loadui.ssl.keyStore";
	public static final String TRUST_STORE = "loadui.ssl.trustStore";
	public static final String KEY_STORE_PASSWORD = "loadui.ssl.keyStorePassword";
	public static final String TRUST_STORE_PASSWORD = "loadui.ssl.trustStorePassword";
}
