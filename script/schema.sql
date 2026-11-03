-- Java Chat System - PostgreSQL Database V1
-- PostgreSQL 14+; apply with psql -X -v ON_ERROR_STOP=1.
-- This script intentionally fails when applied over an existing V1 schema.

SET client_encoding = 'UTF8';
SET TIME ZONE 'UTC';

\ir schema/01_accounts.sql
\ir schema/02_social.sql
\ir schema/03_conversations.sql
\ir schema/04_messages.sql
\ir schema/05_moderation.sql
\ir schema/06_activity.sql
