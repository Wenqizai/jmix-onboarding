package com.company.jmixonboarding.screen.user;

import com.company.jmixonboarding.entity.User;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.FileStorageResource;
import io.jmix.ui.component.Image;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.StandardLookup;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("User.browse")
@UiDescriptor("user-browse.xml")
@LookupComponent("usersTable")
@Route("users")
public class UserBrowse extends StandardLookup<User> {
    @Autowired
    private UiComponents uiComponents;

    @Install(to = "usersTable.picture", subject = "columnGenerator")
    private Component usersTablePictureColumnGenerator(User user) {
        if (user.getPicture() != null) {
            Image image = uiComponents.create(Image.class);
            image.setScaleMode(Image.ScaleMode.CONTAIN);
            image.setSource(FileStorageResource.class).setFileReference(user.getPicture());
            image.setWidth("30px");
            image.setHeight("30px");
            return image;
        }
        return null;
    }
}