package com.company.jmixonboarding.screen.myonboarding;

import com.company.jmixonboarding.entity.User;
import com.company.jmixonboarding.entity.UserStep;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.CloseAction;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardOutcome;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@UiController("MyOnboardingScreen")
@UiDescriptor("my-onboarding-screen.xml")
public class MyOnboardingScreen extends Screen {
    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private CollectionLoader<UserStep> userStepsDl;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Label<Object> completedStepsLabel;
    @Autowired
    private Label<Object> overdueStepsLabel;
    @Autowired
    private Label<Object> totalStepsLabel;
    @Autowired
    private CollectionContainer<UserStep> userStepsDc;
    @Autowired
    private DataContext dataContext;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        User user = (User) currentAuthentication.getUser();
        userStepsDl.setParameter("user", user);
        userStepsDl.load();

        updateLabels();
    }

    @Subscribe(id = "userStepsDc", target = Target.DATA_CONTAINER)
    public void onUserStepsDcCollectionChange(final CollectionContainer.CollectionChangeEvent<UserStep> event) {
        updateLabels();
    }

    @Install(to = "userStepsTable.completed", subject = "columnGenerator")
    private Component userStepsTableCompletedColumnGenerator(final UserStep userStep) {
        CheckBox checkBox = uiComponents.create(CheckBox.class);
        checkBox.setValue(userStep.getCompletedDate() != null);
        checkBox.addValueChangeListener(e -> {
            if (userStep.getCompletedDate() == null) {
                userStep.setCompletedDate(LocalDate.now());
            } else {
                userStep.setCompletedDate(null);
            }
        });
        updateLabels();
        return checkBox;
    }

    private void updateLabels() {
        totalStepsLabel.setValue("Total steps: " + userStepsDc.getItems().size());

        long completedCount = userStepsDc.getItems().stream()
                .filter(us -> us.getCompletedDate() != null)
                .count();
        completedStepsLabel.setValue("Completed steps: " + completedCount);

        long overdueCount = userStepsDc.getItems().stream()
                .filter(this::isOverdue)
                .count();
        overdueStepsLabel.setValue("Overdue steps: " + overdueCount);
    }

    private boolean isOverdue(UserStep us) {
        return us.getCompletedDate() == null
                && us.getDueDate() != null
                && us.getDueDate().isBefore(LocalDate.now());
    }

    @Install(to = "userStepsTable", subject = "styleProvider")
    private String userStepsTableStyleProvider(final UserStep entity, final String property) {
        if ("dueDate".equals(property) && isOverdue(entity)) {
            return "overdue-step";
        }
        return null;
    }

    @Subscribe("saveButton")
    public void onSaveButtonClick(final Button.ClickEvent event) {
        dataContext.commit();
        close(StandardOutcome.COMMIT);
    }

    @Subscribe("discardButton")
    public void onDiscardButtonClick(final Button.ClickEvent event) {
        close(StandardOutcome.DISCARD);
    }

}