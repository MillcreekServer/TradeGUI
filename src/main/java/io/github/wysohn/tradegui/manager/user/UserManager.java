package io.github.wysohn.tradegui.manager.user;

import io.github.wysohn.rapidframework2.bukkit.manager.user.AbstractUserManager;
import io.github.wysohn.rapidframework2.core.database.Database;

import java.util.UUID;

public class UserManager extends AbstractUserManager<User> {
    public UserManager(int loadPriority) {
        super(loadPriority);
    }

    @Override
    protected Database.DatabaseFactory<User> createDatabaseFactory() {
        return getDatabaseFactory(User.class, "user");
    }

    @Override
    protected User newInstance(UUID uuid) {
        return new User(uuid);
    }
}
