/*
 * $Id: IStrategy.java 448 2007-12-10 17:01:19Z kiefer $
 *
 * Created by Christoph Kiefer, kiefer@ifi.uzh.ch
 *
 * See LICENSE for more information about licensing and warranties.
 */
package uk.ac.open.kmi.iserve.imatcher.strategy.api;

import java.util.List;

public interface IStrategy {

	public String getShortName();

	public String getName();

	public String getStrategy();

	public String getStrategy(List<String> parameters);

}
