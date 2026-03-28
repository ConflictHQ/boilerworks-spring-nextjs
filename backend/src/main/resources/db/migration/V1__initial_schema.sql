-- Permissions
CREATE TABLE permissions (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codename   VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    description TEXT
);

-- User groups
CREATE TABLE user_groups (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

-- Group-permission join table
CREATE TABLE group_permissions (
    group_id      UUID NOT NULL REFERENCES user_groups(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (group_id, permission_id)
);

-- Application users
CREATE TABLE app_users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    is_staff    BOOLEAN NOT NULL DEFAULT FALSE,
    last_login  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by  UUID,
    deleted_at  TIMESTAMPTZ,
    deleted_by  UUID
);

-- User-group join table
CREATE TABLE user_group_membership (
    user_id  UUID NOT NULL REFERENCES app_users(id),
    group_id UUID NOT NULL REFERENCES user_groups(id),
    PRIMARY KEY (user_id, group_id)
);

-- Categories
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by  UUID,
    deleted_at  TIMESTAMPTZ,
    deleted_by  UUID
);

-- Products
CREATE TABLE products (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    price       NUMERIC(12, 2) NOT NULL,
    sku         VARCHAR(255) NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    category_id UUID REFERENCES categories(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by  UUID,
    deleted_at  TIMESTAMPTZ,
    deleted_by  UUID
);

-- Form definitions
CREATE TABLE form_definitions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    schema_json JSONB,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    version     INTEGER NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by  UUID,
    deleted_at  TIMESTAMPTZ,
    deleted_by  UUID
);

-- Form submissions
CREATE TABLE form_submissions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    form_definition_id   UUID NOT NULL REFERENCES form_definitions(id),
    data_json            JSONB,
    status               VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by           UUID,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by           UUID,
    deleted_at           TIMESTAMPTZ,
    deleted_by           UUID
);

-- Workflow definitions
CREATE TABLE workflow_definitions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(255) NOT NULL,
    slug             VARCHAR(255) NOT NULL UNIQUE,
    description      TEXT,
    states_json      JSONB,
    transitions_json JSONB,
    initial_state    VARCHAR(255) NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    version          INTEGER NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by       UUID,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by       UUID,
    deleted_at       TIMESTAMPTZ,
    deleted_by       UUID
);

-- Workflow instances
CREATE TABLE workflow_instances (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_definition_id   UUID NOT NULL REFERENCES workflow_definitions(id),
    current_state            VARCHAR(255) NOT NULL,
    context_json             JSONB,
    status                   VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by               UUID,
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by               UUID,
    deleted_at               TIMESTAMPTZ,
    deleted_by               UUID
);

-- Workflow transition logs
CREATE TABLE workflow_transition_logs (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_instance_id  UUID NOT NULL REFERENCES workflow_instances(id),
    from_state            VARCHAR(255) NOT NULL,
    to_state              VARCHAR(255) NOT NULL,
    transition_name       VARCHAR(255) NOT NULL,
    triggered_by          UUID,
    triggered_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata_json         JSONB
);

-- Indexes
CREATE INDEX idx_app_users_email ON app_users(email);
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_form_definitions_slug ON form_definitions(slug);
CREATE INDEX idx_form_submissions_form ON form_submissions(form_definition_id);
CREATE INDEX idx_workflow_definitions_slug ON workflow_definitions(slug);
CREATE INDEX idx_workflow_instances_definition ON workflow_instances(workflow_definition_id);
CREATE INDEX idx_workflow_instances_status ON workflow_instances(status);
CREATE INDEX idx_workflow_transition_logs_instance ON workflow_transition_logs(workflow_instance_id);
