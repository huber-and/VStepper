import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import '@vaadin/vaadin-lumo-styles/color.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import './styles/stepper-styles.js'
import './styles/step-header-styles.js'

class VStepper extends ThemableMixin(PolymerElement) {
    static get template() {
        return html`
            <style include="stepper-styles step-header-styles lumo-color lumo-spacing"></style>
            <slot name="header"></slot>
            <slot name="content"></slot>
            <slot name="footer"></slot>
            <div class="overlay">
            	<slot name="overlay-header"></slot>
            	<slot name="overlay-content"></slot>
            	<slot name="overlay-footer"></slot>
            </div>
        `;
    }

    static get is() {
        return 'v-stepper';
    }
}

window.customElements.define('v-stepper', VStepper);