package com.company.jmixonboarding.screen.step;

import com.company.jmixonboarding.entity.Step;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.StandardEditor;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

@UiController("Step.edit")
@UiDescriptor("step-edit.xml")
@EditedEntityContainer("stepDc")
public class StepEdit extends StandardEditor<Step> {
}