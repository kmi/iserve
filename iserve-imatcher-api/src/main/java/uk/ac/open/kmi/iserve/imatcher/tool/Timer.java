/*
 * $Id: Timer.java 448 2007-12-10 17:01:19Z kiefer $
 *
 * Created by Christoph Kiefer, kiefer@ifi.uzh.ch
 *
 * See LICENSE for more information about licensing and warranties.
 */
package uk.ac.open.kmi.iserve.imatcher.tool;

public class Timer {

	public static long start() {
		return System.currentTimeMillis();
	}

	public static long stop() {
		return System.currentTimeMillis();
	}

	public static long elapsed(long start, long stop) {
		return stop - start;
	}
}
