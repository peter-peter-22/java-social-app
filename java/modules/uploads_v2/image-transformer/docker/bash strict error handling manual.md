# Set -euo pipefail 

This command modifies how bash handles missing values and errors.

# The tags
3 tags are defined, e,u,o

## -e
Errors stop the execution.

```bash
set -e

false # In bash, false value is returned when an error happens, so this is considered an error, the execution will stop here
echo "Never executed"
```

## -u
The usage of undefined variables causes error.

```bash
set -u
echo "$UNDEFINED_VARIABLE" # This will create an error
```

## -o pipefail
The pipe returns the error status if any of the steps fail.
By default, the status of the last command is returned.

```bash
set -o pipefail

false | true
echo "$?" # Will return 1, non-zero status indicates error
```

# Usage in the code

The following script is used for a docker containers:
```bash
# Set strict error handling
set -euo pipefail

# Check if the variables are unset or empty
: "${HOST_UID:?HOST_UID is required}"
: "${HOST_GID:?HOST_GID is required}"

# Create the gradle home (this directory should be mounted to a volume)
mkdir -p "${GRADLE_USER_HOME:-/gradle-home}"
# Set the ownership of the gradle home to the user
chown "${HOST_UID}:${HOST_GID}" "${GRADLE_USER_HOME:-/gradle-home}"
```

Here, the `:` evaluates an expression without doing anything.

The `${HOST_UID:?HOST_UID is required}` checks if the left parameter is unset or empty, if yes, then the error message on the right is printed
and the execution is terminated. This is also known as bash parameter expansion.

**The missing value throws an error and stops the execution even without the strict error handling.**
It is for missing values and error those happen elsewhere, for example the mkdir and chown commands.

Examples:
```bash
x="defined"
echo ${x:?x is required} # Will print X
```

```bash
x="" # Or X is unset
echo ${x:?x is required} # Will cause an error with the message "x is required"
```


