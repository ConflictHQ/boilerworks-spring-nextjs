-- Seed permissions
INSERT INTO permissions (id, codename, name, description) VALUES
    (gen_random_uuid(), 'products.view',     'View Products',     'Can view products'),
    (gen_random_uuid(), 'products.create',   'Create Products',   'Can create products'),
    (gen_random_uuid(), 'products.edit',     'Edit Products',     'Can edit products'),
    (gen_random_uuid(), 'products.delete',   'Delete Products',   'Can delete products'),
    (gen_random_uuid(), 'categories.view',   'View Categories',   'Can view categories'),
    (gen_random_uuid(), 'categories.create', 'Create Categories', 'Can create categories'),
    (gen_random_uuid(), 'categories.edit',   'Edit Categories',   'Can edit categories'),
    (gen_random_uuid(), 'categories.delete', 'Delete Categories', 'Can delete categories'),
    (gen_random_uuid(), 'forms.view',        'View Forms',        'Can view forms'),
    (gen_random_uuid(), 'forms.create',      'Create Forms',      'Can create forms'),
    (gen_random_uuid(), 'forms.edit',        'Edit Forms',        'Can edit forms'),
    (gen_random_uuid(), 'forms.delete',      'Delete Forms',      'Can delete forms'),
    (gen_random_uuid(), 'forms.submit',      'Submit Forms',      'Can submit forms'),
    (gen_random_uuid(), 'workflows.view',    'View Workflows',    'Can view workflows'),
    (gen_random_uuid(), 'workflows.create',  'Create Workflows',  'Can create workflows'),
    (gen_random_uuid(), 'workflows.edit',    'Edit Workflows',    'Can edit workflows'),
    (gen_random_uuid(), 'workflows.delete',  'Delete Workflows',  'Can delete workflows'),
    (gen_random_uuid(), 'workflows.execute', 'Execute Workflows', 'Can execute workflow transitions');

-- Seed user groups
INSERT INTO user_groups (id, name, description) VALUES
    (gen_random_uuid(), 'Administrators', 'Full access to all features'),
    (gen_random_uuid(), 'Editors',        'Can view and edit content'),
    (gen_random_uuid(), 'Viewers',        'Read-only access');

-- Grant all permissions to Administrators
INSERT INTO group_permissions (group_id, permission_id)
SELECT g.id, p.id FROM user_groups g, permissions p
WHERE g.name = 'Administrators';

-- Grant view + edit permissions to Editors
INSERT INTO group_permissions (group_id, permission_id)
SELECT g.id, p.id FROM user_groups g, permissions p
WHERE g.name = 'Editors'
  AND (p.codename LIKE '%.view' OR p.codename LIKE '%.edit' OR p.codename LIKE '%.create' OR p.codename = 'forms.submit' OR p.codename = 'workflows.execute');

-- Grant view permissions to Viewers
INSERT INTO group_permissions (group_id, permission_id)
SELECT g.id, p.id FROM user_groups g, permissions p
WHERE g.name = 'Viewers'
  AND p.codename LIKE '%.view';

-- Seed admin user (password: admin123)
INSERT INTO app_users (id, email, password, first_name, last_name, is_active, is_staff) VALUES
    (gen_random_uuid(), 'admin@boilerworks.dev', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', TRUE, TRUE);

-- Add admin to Administrators group
INSERT INTO user_group_membership (user_id, group_id)
SELECT u.id, g.id FROM app_users u, user_groups g
WHERE u.email = 'admin@boilerworks.dev' AND g.name = 'Administrators';

-- Seed demo user (password: demo123)
INSERT INTO app_users (id, email, password, first_name, last_name, is_active, is_staff) VALUES
    (gen_random_uuid(), 'demo@boilerworks.dev', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Demo', 'User', TRUE, FALSE);

-- Add demo to Editors group
INSERT INTO user_group_membership (user_id, group_id)
SELECT u.id, g.id FROM app_users u, user_groups g
WHERE u.email = 'demo@boilerworks.dev' AND g.name = 'Editors';

-- Seed categories
INSERT INTO categories (id, name, slug, description, sort_order) VALUES
    (gen_random_uuid(), 'Electronics', 'electronics', 'Electronic devices and gadgets', 1),
    (gen_random_uuid(), 'Clothing',    'clothing',    'Apparel and fashion items',       2),
    (gen_random_uuid(), 'Books',       'books',       'Books and publications',          3);

-- Seed products
INSERT INTO products (id, name, slug, description, price, sku, is_active, category_id)
SELECT gen_random_uuid(), 'Wireless Headphones', 'wireless-headphones', 'Premium noise-cancelling wireless headphones', 149.99, 'WH-001', TRUE, c.id
FROM categories c WHERE c.slug = 'electronics';

INSERT INTO products (id, name, slug, description, price, sku, is_active, category_id)
SELECT gen_random_uuid(), 'Mechanical Keyboard', 'mechanical-keyboard', 'Cherry MX Blue switches, RGB backlit', 89.99, 'MK-001', TRUE, c.id
FROM categories c WHERE c.slug = 'electronics';

INSERT INTO products (id, name, slug, description, price, sku, is_active, category_id)
SELECT gen_random_uuid(), 'Cotton T-Shirt', 'cotton-tshirt', '100% organic cotton, unisex fit', 24.99, 'CT-001', TRUE, c.id
FROM categories c WHERE c.slug = 'clothing';
