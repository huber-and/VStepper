package com.mlottmann.vstepper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;

import lombok.Getter;

/**
 * @author Matthias Lottmann
 *         <p>
 *         Vaadin addon for displaying a series of components one at a time.
 */
@Tag("v-stepper")
@JsModule("./v-stepper.js")
// TODO: make steps skippable
public class VStepper extends PolymerTemplate<TemplateModel> implements HasSize, HasStyle, HasComponents {

	private List<Step> steps;
	private final List<Step> allSteps;
	private Step currentStep;
	private ValidationMode validationMode = ValidationMode.ON_CHANGE;

	public enum OLERLAY_POSITION {
		LEFT, RIGHT
	};

	private final String ATTR_OVERLAY_POSITION = "op";

	private final Div header = new Div();
	private final Div content = new Div();
	private final Div overlayContent = new Div();
	private final Div footer = new Div();

	@Getter
	private Button cancel;
	@Getter
	private Button back;
	@Getter
	private Button next;
	@Getter
	private Button finish;

	public VStepper() {
		steps = Collections.emptyList();
		allSteps = new ArrayList<>();

		add(header);
		add(content);
		add(overlayContent);
		add(footer);

		initFooter();
		setFrameless(false);
		setSinglelayer(false);
		setOverlapping(true);
	}

	public void setOverlayPosition(final OLERLAY_POSITION position) {
		// this.orientation = orientation;
		if (position == OLERLAY_POSITION.LEFT) {
			getElement().setAttribute(ATTR_OVERLAY_POSITION, "l");
		} else {
			getElement().setAttribute(ATTR_OVERLAY_POSITION, "r");
		}

	}

	private void initLayout(final boolean frameless) {

		if (frameless) {
			header.getElement().setAttribute("slot", "overlay-header");
			content.getElement().setAttribute("slot", "content");
			overlayContent.getElement().setAttribute("slot", "overlay-content");
			footer.getElement().setAttribute("slot", "overlay-footer");

		} else {
			header.getElement().setAttribute("slot", "header");
			content.getElement().setAttribute("slot", "content");
			overlayContent.getElement().setAttribute("slot", "content");
			footer.getElement().setAttribute("slot", "footer");

		}

	}

	public void setSinglelayer(final boolean singlelayer) {
		if (singlelayer) {
			getElement().getThemeList().add("singlelayer");
			getElement().getThemeList().remove("multilayer");
		} else {
			getElement().getThemeList().remove("singlelayer");
			getElement().getThemeList().add("multilayer");
		}
	}

	public void setOverlapping(final boolean overlapping) {
		if (overlapping) {
			getElement().getThemeList().add("overlapping");
			getElement().getThemeList().remove("notoverlapping");
		} else {
			getElement().getThemeList().remove("overlapping");
			getElement().getThemeList().add("notoverlapping");
		}
	}

	public void setFrameless(final boolean frameless) {
		if (frameless) {
			getElement().getThemeList().add("frameless");
			getElement().getThemeList().remove("framefull");
		} else {
			getElement().getThemeList().remove("frameless");
			getElement().getThemeList().add("framefull");
		}
		initLayout(frameless);
	}

	/**
	 * @param components
	 *            the components to display in the the different stepper steps.
	 */
	public VStepper(final Component... components) {
		this();
		for (final Component component : components) {
			addStep(component);
		}
	}

	/**
	 * @param steps
	 *            the steps to display in this stepper. A step consists of a header and a content component.
	 */
	public VStepper(final Step... steps) {
		this();
		for (final Step step : steps) {
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

	public void showNextStep() {
		if (currentStep.isValid()) {
			currentStep.complete();
			updateSteps();
			final Step nextStep = getNextStep(currentStep);
			changeStep(nextStep);
		}
	}

	public void showPreviousStep() {
		currentStep.abort();
		updateSteps();
		final Step previousStep = getPreviousStep(currentStep);
		changeStep(previousStep);
	}

	public void changeStep(final Step newStep) {
		content.removeAll();
		currentStep = newStep;
		currentStep.enter();
		content.add(currentStep.getContent());
		overlayContent.removeAll();
		overlayContent.add(currentStep.getOverlayContent());
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
		finish.setVisible(!next.isVisible());
	}

	private void updateButtonEnabledState() {
		if (validationMode == ValidationMode.ON_CHANGE) {
			next.setEnabled(currentStep.isValid());
			finish.setEnabled(next.isEnabled());
		}
	}

	protected void updateSteps() {
		header.removeAll();
		steps = allSteps.stream().filter(p -> p.isVisible()).collect(Collectors.toList());
		steps.forEach(s -> header.add(s.getHeader()));
	}

	protected Step getNextStep(final Step step) {
		if (isLastStep(step)) {
			return step;
		}
		return steps.get(steps.indexOf(step) + 1);
	}

	protected Step getPreviousStep(final Step step) {
		if (isFirstStep(step)) {
			return step;
		}
		return steps.get(steps.indexOf(step) - 1);
	}

	protected boolean isFirstStep(final Step step) {
		return steps.indexOf(step) == 0;
	}

	protected boolean isLastStep(final Step step) {
		return steps.indexOf(step) == steps.size() - 1;
	}

	/**
	 * Adds a new step with the given content component and a default header component to the stepper.
	 *
	 * @param stepContent
	 *            the content to display when the corresponding step is reached.
	 */
	public Step addStep(final Component stepContent) {
		return addStep("", stepContent);
	}

	/**
	 * Adds a new step with the given content component and a default header component with the given title to the
	 * stepper.
	 *
	 * @param stepTitle
	 *            the title to display in the default header component.
	 * @param stepContent
	 *            the content to display when the corresponding step is reached.
	 */
	public Step addStep(final String stepTitle, final Component stepContent) {
		final StepHeader stepHeader = new DefaultStepHeader(allSteps.size() + 1, stepTitle);
		return addStep(stepHeader, stepContent);
	}

	/**
	 * Adds a new step with the given header component and content component to the stepper.
	 *
	 * @param stepHeader
	 *            the header component of this step to display in the header of the stepper.
	 * @param stepContent
	 *            the content to display when the corresponding step is reached.
	 */
	public Step addStep(final Component stepHeader, final Component stepContent) {
		final Step step = new DefaultStep(stepHeader, stepContent);
		return addStep(step);
	}

	/**
	 * Adds the given step to the stepper.
	 *
	 * @param step
	 *            the step to add to the stepper. Each step consists of a header and a content component.
	 */
	public Step addStep(final Step step) throws IllegalArgumentException {
		checkStep(step);
		step.addValidationListener(event -> updateButtonEnabledState());
		step.addChangedListener(listener -> updateSteps());
		allSteps.add(step);
		updateSteps();
		if (currentStep == null && step.isVisible()) {
			changeStep(step);
			return step;
		}
		updateButtonVisibility();
		return step;
	}

	private void checkStep(final Step step) throws IllegalArgumentException {
		if (step.getHeader() == null) {
			throw new IllegalArgumentException("Step header can not be null.");
		}
		if (step.getContent() == null) {
			throw new IllegalArgumentException("Step content can not be null.");
		}
	}

//	private void showFirstStep(final Step step) {
//		currentStep = step;
//		currentStep.enter();
//		content.add(currentStep.getContent());
//		overlayContent.add(currentStep.getOverlayContent());
//		updateButtons();
//	}

	/**
	 * Sets the visibility of the cancel button in the footer. Also affects the layout of the footer.
	 *
	 * @param visible
	 */
	public void setCancelVisible(final boolean visible) {
		footer.removeAll();
		cancel.setVisible(visible);
		if (visible) {
			footer.add(cancel, new HorizontalLayout(back, next, finish));
		} else {
			footer.add(back, new Div(), new HorizontalLayout(next, finish));
		}
	}

	public Registration addCancelListener(final ComponentEventListener<ClickEvent<Button>> listener) {
		setCancelVisible(true);
		return cancel.addClickListener(listener);
	}

	public Registration addNextListener(final ComponentEventListener<ClickEvent<Button>> listener) {
		return next.addClickListener(listener);
	}

	public Registration addBackListener(final ComponentEventListener<ClickEvent<Button>> listener) {
		return back.addClickListener(listener);
	}

	public Registration addFinishListener(final ComponentEventListener<ClickEvent<Button>> listener) {
		return finish.addClickListener(listener);
	}

	public void setCancelText(final String text) {
		cancel.setText(text);
	}

	public void setNextText(final String text) {
		next.setText(text);
	}

	public void setBackText(final String text) {
		back.setText(text);
	}

	public void setFinishText(final String text) {
		finish.setText(text);
	}

	public void setValidationMode(final ValidationMode validationMode) {
		this.validationMode = validationMode;
		next.setEnabled(validationMode == ValidationMode.ON_NEXT || currentStep.isValid());
		finish.setEnabled(validationMode == ValidationMode.ON_NEXT || currentStep.isValid());
	}

}
