package com.example.media.contract.object;

/**
 * How long an object stays in the store.
 * The adapter maps these to its object tags; the tag names stay inside the adapter.
 */
public enum ObjectLifetime {
    /**
     * The object expires unless it becomes permanent before its lifespan ends.
     * Everything an upload writes starts here, so a failed upload cleans itself up.
     */
    TEMPORARY,
    /** The object no longer expires. Set only after the registry transaction commits. */
    PERMANENT
}