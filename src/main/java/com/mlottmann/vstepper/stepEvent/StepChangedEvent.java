/*   
* Copyright (c) 2020 Invent IOT GmbH   
* All rights reserved.   
*/
package com.mlottmann.vstepper.stepEvent;

import com.mlottmann.vstepper.Step;

/**
 * @author Andreas Huber
 *
 */
public class StepChangedEvent extends StepEvent {

	public StepChangedEvent(Step step) {
		super(step);
	}

}
