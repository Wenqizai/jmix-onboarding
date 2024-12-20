package com.company.jmixonboarding.screen.department;

import com.company.jmixonboarding.entity.Department;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.StandardEditor;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

@UiController("Department.edit")
@UiDescriptor("department-edit.xml")
@EditedEntityContainer("departmentDc")
public class DepartmentEdit extends StandardEditor<Department> {
    @Subscribe
    public void onInitEntity(final InitEntityEvent<Department> event) {
        Department entity = event.getEntity();
        entity.setName("Please enter department name");
    }
}