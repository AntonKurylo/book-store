package mate.academy.config;

import org.testcontainers.containers.MySQLContainer;

public class CustomMySqlContainer extends MySQLContainer<CustomMySqlContainer> {
    private static final String DB_IMAGE = "mysql:8";

    private static CustomMySqlContainer mysqlContainer;

    private CustomMySqlContainer() {
        super(DB_IMAGE);
    }

    public static synchronized CustomMySqlContainer getInstance() {
        if (mysqlContainer == null) {
            mysqlContainer = new CustomMySqlContainer()
                    .withDatabaseName("book_store")
                    .withUsername("user")
                    .withPassword("password");
        }
        return mysqlContainer;
    }

    @Override
    public void stop() {
    }
}
