# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in Boilerworks, please report it responsibly.

**Do not open a public issue.**

Instead, email **security@weareconflict.com** with:

- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

We will acknowledge your report within 48 hours and aim to release a fix within 7 days for critical issues.

## Supported Versions

| Version | Supported |
| ------- | --------- |
| latest  | Yes       |

## Security Best Practices

When deploying Boilerworks:

- Change all default credentials: the database user/password, the seeded
  `admin@boilerworks.dev` / `demo@boilerworks.dev` accounts, and MinIO if you
  use the `storage` Docker profile
- Use HTTPS in production and serve the session cookie with the `Secure` flag
- Set `NODE_ENV=production` for the Next.js frontend
- Update the allowed CORS origins in
  `backend/src/main/java/com/boilerworks/api/config/SecurityConfig.java`
  (`corsConfigurationSource()`) to your domain only — they are hardcoded, not
  read from an environment variable
