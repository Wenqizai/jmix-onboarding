package com.company.jmixonboarding.screen.user;

import com.company.jmixonboarding.entity.OnboardingStatus;
import com.company.jmixonboarding.entity.Step;
import com.company.jmixonboarding.entity.User;
import com.company.jmixonboarding.entity.UserStep;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.security.event.SingleUserPasswordChangeEvent;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

@UiController("User.edit")
@UiDescriptor("user-edit.xml")
@EditedEntityContainer("userDc")
@Route(value = "users/edit", parentPrefix = "users")
public class UserEdit extends StandardEditor<User> {

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordField passwordField;

    @Autowired
    private TextField<String> usernameField;

    @Autowired
    private PasswordField confirmPasswordField;

    @Autowired
    private Notifications notifications;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private ComboBox<String> timeZoneField;

    @Autowired
    private DataContext dataContext;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private CollectionPropertyContainer<UserStep> stepsDc;

    @Autowired
    private UiComponents uiComponents;

    private boolean isNewEntity;

    @Subscribe
    public void onInit(InitEvent event) {
        timeZoneField.setOptionsList(Arrays.asList(TimeZone.getAvailableIDs()));
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<User> event) {
        usernameField.setEditable(true);
        passwordField.setVisible(true);
        confirmPasswordField.setVisible(true);
        isNewEntity = true;

        User entity = event.getEntity();
        entity.setOnboardingStatus(OnboardingStatus.NOT_STARTED);
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            usernameField.focus();
        }
    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            if (!Objects.equals(passwordField.getValue(), confirmPasswordField.getValue())) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(messageBundle.getMessage("passwordsDoNotMatch"))
                        .show();
                event.preventCommit();
            }
            getEditedEntity().setPassword(passwordEncoder.encode(passwordField.getValue()));
        }
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPostCommit(DataContext.PostCommitEvent event) {
        if (isNewEntity) {
            getApplicationContext().publishEvent(new SingleUserPasswordChangeEvent(getEditedEntity().getUsername(), passwordField.getValue()));
        }
    }

    @Subscribe("generateButton")
    public void onGenerateButtonClick(final Button.ClickEvent event) {
        // 自动生成当前用户的入职步骤
        User user = getEditedEntity();
        if (user.getJoiningDate() == null) {
            notifications.create().withCaption("Cannot generate steps for user without 'Joining date'").show();
            return;
        }

        List<Step> steps = dataManager.load(Step.class)
                .query("select s from Step s order by s.sortValue asc ")
                .list();

        for (Step step : steps) {
            if (stepsDc.getItems().stream().noneMatch(userStep -> userStep.getStep().equals(step))) {
                UserStep userStep = dataContext.create(UserStep.class);
                userStep.setUser(user);
                userStep.setStep(step);
                userStep.setDueDate(user.getJoiningDate().plusDays(step.getDuration()));
                userStep.setSortValue(step.getSortValue());
                stepsDc.getMutableItems().add(userStep);
            }
        }

    }

    @Subscribe("resetButton")
    public void onResetButtonClick(final Button.ClickEvent event) {
        // 清空生成的入职步骤表单, 方便重新生成
        User user = getEditedEntity();
        if (stepsDc.getItems().stream().noneMatch(userStep -> Objects.equals(userStep.getUser().getId(), user.getId()))) {
            return;
        }
        // 清空当前用户视图
        stepsDc.setItems(stepsDc.getItems().stream().filter(userStep -> !Objects.equals(userStep.getUser().getId(), user.getId())).collect(Collectors.toList()));
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
        return checkBox;
    }

    @Subscribe(id = "stepsDc", target = Target.DATA_CONTAINER)
    public void onStepsDcCollectionChange(final CollectionContainer.CollectionChangeEvent<UserStep> event) {
        updateOnboardingStatus();
    }

    @Subscribe(id = "stepsDc", target = Target.DATA_CONTAINER)
    public void onStepsDcItemChange(final InstanceContainer.ItemChangeEvent<UserStep> event) {
        updateOnboardingStatus();
    }

    private void updateOnboardingStatus() {
        User user = getEditedEntity();
        long completedCount = user.getSteps() == null ? 0
                : user.getSteps().stream().filter(userStep -> userStep.getCompletedDate() != null).count();
        if (completedCount == 0) {
            user.setOnboardingStatus(OnboardingStatus.NOT_STARTED);
        } else if (completedCount == user.getSteps().size()) {
            user.setOnboardingStatus(OnboardingStatus.COMPLETED);
        } else {
            user.setOnboardingStatus(OnboardingStatus.IN_PROGRESS);
        }
    }
}