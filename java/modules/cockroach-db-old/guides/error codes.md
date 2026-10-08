| SQLSTATE | Meaning               | Tested |
|----------|-----------------------|--------|
| 23505    | unique_violation      | no     |
| 23503    | foreign_key_violation | yes    |
| 23502    | not_null_violation    | no     |
| 23514    | check_violation       | no     |

The error codes are accessed from the SQLErrorCodes class of the cockroachdb module.