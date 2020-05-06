/*   
* Copyright (c) 2020 Invent IOT GmbH   
* All rights reserved.   
*/
package com.mlottmann.vstepper.stepEvent;


/**
 * @author Andreas Huber
 *
 */
public interface StepChangedListener extends  StepEventListener {

	
	void onChanged(StepChangedEvent event);

}
