package com.mlottmann.vstepper;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
/**
 * @author Matthias Lottmann
 * <p>
 * Vaadin addon for displaying a series of components one at a time.
 */
@Tag("v-stepper")
@JsModule("./v-stepper.js")
public class VStepper extends PolymerTemplate<TemplateModel> implements HasSize, HasStyle {

	private List<Step> steps;
	private final List<Step> allSteps;
	private Step currentStep;
	private ValidationMode validationMode = ValidationMode.ON_CHANGE;

	@Id
	private Div header;
	@Id
	private Div content;
	@Id
	private Div footer;

	@Getter
	private Button cancel;
	@Getter
	private Button back;
	@Getter
	private Button next;
	@Getter
	private Button finish;

	public VStepper() {
		this.steps = Collections.emptyList();
		this.allSteps = new ArrayList<>();
		initFooter();
	}

	/**
	 * @param components the components to display in the the different stepper steps.
	 */
	public VStepper(Component... components) {
		this();
		for (Component component : components) {
			addStep(component);
		}
	}

	/**
	 * @param steps the steps to display in this stepper. A step consists of a header and a content component.
	 */
	public VStepper(Step... steps) {
		this();
		for (Step step : steps) {
			addStep(step);
		}
	}

	private void initFooter() {
		cancel = new Button("Cancel");
		cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
		back = new Button("Back");
		back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		next = new Button("Next");
		next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		finish = new Button("Finish");
		finish.setWidth("90px");
		finish.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

		next.addClickListener(click -> showNextStep());
		back.addClickListener(click -> showPreviousStep());
		finish.addClickListener(click -> currentStep.complete());

		back.setVisible(false);
		finish.setVisible(false);
		setCancelVisible(false);
	}

	private void showNextStep() {
		if (currentStep.isValid()) {
			currentStep.complete();
			Step nextStep = getNextStep(currentStep);
			changeStep(nextStep);
		}
	}

	private void showPreviousStep() {
		currentStep.abort();
		Step previousStep = getPreviousStep(currentStep);
		changeStep(previousStep);
	}

	private void changeStep(Step newStep) {
		content.removeAll();
		currentStep = newStep;
		currentStep.enter();
		content.add(currentStep.getContent());
		updateButtons();
	}

	private void updateButtons() {
		if (currentStep != null) {
			updateButtonVisibility();
			updateButtonEnabledState();
		}
	}

	private void updateButtonVisibility() {
		next.setVisible(!isLastStep(currentStep));
		back.setVisible(!isFirstStep(currentStep));
		finish.setVisible(isLastStep(currentStep));
	}

	private void updateButtonEnabledState() {
		if (validationMode == ValidationMode.ON_CHANGE) {
			next.setEnabled(currentStep.isValid());
			finish.setEnabled(currentStep.isValid());
		}
	}

	protected void updateSteps() {
		header.removeAll();
		steps = allSteps.stream().filter(p-> p.isVisible()).collect(Collectors.toList());
		steps.forEach(s -> header.add(s.getHeader()));
	}
	
	private Step getNextStep(Step step) {
		if (isLastStep(step)) {
			return step;
		}
		return steps.get(steps.indexOf(step) + 1);
	}

	private Step getPreviousStep(Step step) {
		if (isFirstStep(step)) {
			return step;
		}
		return steps.get(steps.indexOf(step) - 1);
	}

	private boolean isFirstStep(Step step) {
		return steps.indexOf(step) == 0;
	}

	private boolean isLastStep(Step step) {
		return steps.indexOf(step) == steps.size() - 1;
	}

	/**
	 * Adds a new step with the given content component and a default header component to the stepper.
	 *
	 * @param stepContent the content to display when the corresponding step is reached.
	 */
	public Step addStep(Component stepContent) {
		return addStep("", stepContent);
	}

	/**
	 * Adds a new step with the given content component and a default header component with the given title to
	 * the stepper.
	 *
	 * @param stepTitle   the title to display in the default header component.
	 * @param stepContent the content to display when the corresponding step is reached.
	 */
	public Step addStep(String stepTitle, Component stepContent) {
		StepHeader stepHeader = new DefaultStepHeader(allSteps.size() + 1, stepTitle);
		return addStep(stepHeader, stepContent);
	}

	/**
	 * Adds a new step with the given header component and content component to the stepper.
	 *
	 * @param stepHeader  the header component of this step to display in the header of the stepper.
	 * @param stepContent the content to display when the corresponding step is reached.
	 */
	public Step addStep(Component stepHeader, Component stepContent) {
		Step step = new DefaultStep(stepHeader, stepContent);
		return addStep(step);
	}

	/**
	 * Adds the given step to the stepper.
	 *
	 * @param step the step to add to the stepper. Each step consists of a header and a content component.
	 */
	public Step addStep(Step step) throws IllegalArgumentException {
		checkStep(step);
		step.addValidationListener(event -> updateButtonEnabledState());
		step.addChangedListener(listener -> updateSteps());
		allSteps.add(step);
		updateSteps();
		if (currentStep == null && step.isVisible()) {
			showFirstStep(step);
		}
		
		return step;
	}

	private void checkStep(Step step) throws IllegalArgumentException {
		if (step.getHeader() == null) {
			throw new IllegalArgumentException("Step header can not be null.");
		}
		if (step.getContent() == null) {
			throw new IllegalArgumentException("Step content can not be null.");
		}
	}

	private void showFirstStep(Step step) {
		currentStep = step;
		currentStep.enter();
		next.setEnabled(currentStep.isValid());
		content.add(currentStep.getContent());
	}

	/**
	 * Sets the visibility of the cancel button in the footer. Also affects the layout of the footer.
	 *
	 * @param visible
	 */
	public void setCancelVisible(boolean visible) {
		footer.removeAll();
		cancel.setVisible(visible);
		if (visible) {
			footer.add(cancel, new HorizontalLayout(back, next, finish));
		} else {
			footer.add(back, new Div(), new HorizontalLayout(next, finish));
		}
	}

	public Registration addCancelListener(ComponentEventListener<ClickEvent<Button>> listener) {
		setCancelVisible(true);
		return cancel.addClickListener(listener);
	}

	public Registration addNextListener(ComponentEventListener<ClickEvent<Button>> listener) {
		return next.addClickListener(listener);
	}

	public Registration addBackListener(ComponentEventListener<ClickEvent<Button>> listener) {
		return back.addClickListener(listener);
	}

	public Registration addFinishListener(ComponentEventListener<ClickEvent<Button>> listener) {
		return finish.addClickListener(listener);
	}

	public void setCancelText(String text) {
		cancel.setText(text);
	}

	public void setNextText(String text) {
		next.setText(text);
	}

	public void setBackText(String text) {
		back.setText(text);
	}

	public void setFinishText(String text) {
		finish.setText(text);
	}

	public void setValidationMode(ValidationMode validationMode) {
		this.validationMode = validationMode;
		next.setEnabled(validationMode == ValidationMode.ON_NEXT || currentStep.isValid());
		finish.setEnabled(validationMode == ValidationMode.ON_NEXT || currentStep.isValid());
	}

}
