package pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import static io.github.encryptorcode.pluralize.Pluralize.pluralize;

/**
 * Physical naming strategy that converts schema, column and sequence names to snake_case
 * and table names to pluralized snake_case.
 */
public class SnakeCaseWithPluralizedTablePhysicalNamingStrategy implements PhysicalNamingStrategy {

    /**
     * Physical catalog naming transformation (no catalog mapping applied).
     */
    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return null;
    }

    /**
     * Physical schema naming transformation: converts schema identifiers to snake_case.
     */
    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Physical table naming transformation: pluralizes and converts table names to snake_case.
     */
    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(this.toPlural(identifier));
    }

    /**
     * Physical sequence naming transformation: converts sequence identifiers to snake_case.
     */
    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Physical column naming transformation: converts column identifiers to snake_case.
     */
    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    private Identifier toSnakeCase(final Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        final String newName = identifier.getText()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
        return Identifier.toIdentifier(newName);
    }

    private Identifier toPlural(final Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        return Identifier.toIdentifier(pluralize(identifier.getText()));
    }
}
