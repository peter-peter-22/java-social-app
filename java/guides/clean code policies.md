# Clean code policies

The following concepts are used while designing the code.

- SOLID principles
- Gradle modules
- DRY
- Java packages

## Module rules

### Separation between modules

Large logic domains are separated into modules.
Modules only export business logic.

### Separation inside modules

Inside the modules the visibility of classes those 
aren't needed elsewhere in the module is limited
with java packages.
The default visibility modifier is prioritized.

### Exports

Typically, only interfaces and records are exported.

## Interfaces

### Data interfaces

#### References

For data class interfaces, the id of the
referenced entity is stored in the interface, 
not the object itself.

#### Ids

The type of the id is defined as a separate interface to make
it easier to change the id type.

### Data classes and DTOs

The usage of java records is prioritized.

The java.time.Instant is used as the date type.