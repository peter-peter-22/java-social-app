# Liquibase guide and rules

The master changelog is a yaml file, the individual changelogs are sql files.

The changelogs are grouped by table.

## Changelog rollbacks

The rollback must be a single line.

Valid rollback:
```
--rollback drop table posts;
```
Invalid rollback (will always execute):
```
--rollback 
drop table posts;
```

## Useful links

- [include all for yaml master changelog](https://docs.liquibase.com/secure/reference-guide-5-1/changelog-attributes/includeall)
- [sql changelogs](https://docs.liquibase.com/secure/user-guide-5-1-1/sql-changelog-example)
- [yaml changelog example](https://docs.liquibase.com/secure/user-guide-5-1-1/yaml-changelog-example)