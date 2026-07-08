### AWS IAM Policy documents schema

[Documentation](https://docs.min.io/enterprise/aistor-object-store/administration/iam/access/)

Syntax:
```
{
  "Version": "2012-10-17",          // Must be this exact value
  "Statement": [                    // Array of rules (one or more)
    {
      "Sid": "OptionalUniqueId",    // Statement ID for readability
      "Effect": "Allow",            // or "Deny"
      "Principal": {                // Who the rule applies to
        "AWS": ["*"]                // "*" = everyone (anonymous)
      },
      "Action": [                   // What operations are allowed/denied
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [                 // What resources (bucket or objects)
        "arn:aws:s3:::my-bucket",
        "arn:aws:s3:::my-bucket/*"
      ],
      "Condition": {                // Optional constraints (e.g. IP, time, headers)
        "StringEquals": {
          "s3:prefix": ["public/"]
        }
      }
    }
  ]
}
```