-- Seed Data Script for Kanban System
-- This script adds default lists (To Do, In Progress, Done) and sample cards
-- 
-- HOW TO USE:
-- 1. Open pgAdmin
-- 2. Connect to your PostgreSQL server
-- 3. Right-click on "kanban_db" database → Query Tool
-- 4. First, find your user ID and board ID by running:
--    SELECT id, username, email FROM users;
--    SELECT id, name FROM boards;
-- 5. Replace USER_ID and BOARD_ID in the queries below with your actual IDs
-- 6. Copy and paste the queries below (one section at a time) and execute

-- ============================================
-- STEP 1: Get your IDs first
-- ============================================
-- Run these queries to find your user ID and board ID:
-- SELECT id, username, email FROM users WHERE is_deleted = false;
-- SELECT id, name FROM boards WHERE is_deleted = false;

-- ============================================
-- STEP 2: Insert Default Lists
-- ============================================
-- Replace BOARD_ID with your actual board ID (e.g., if your board ID is 2, change it below)

INSERT INTO lists (name, board_id, position, is_deleted, created_at, updated_at)
SELECT 
  'To Do',
  2,  -- CHANGE THIS: Replace 2 with your board ID
  0,
  false,
  NOW(),
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM lists WHERE board_id = 2 AND name = 'To Do' AND is_deleted = false
);

INSERT INTO lists (name, board_id, position, is_deleted, created_at, updated_at)
SELECT 
  'In Progress',
  2,  -- CHANGE THIS: Replace 2 with your board ID
  1,
  false,
  NOW(),
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM lists WHERE board_id = 2 AND name = 'In Progress' AND is_deleted = false
);

INSERT INTO lists (name, board_id, position, is_deleted, created_at, updated_at)
SELECT 
  'Done',
  2,  -- CHANGE THIS: Replace 2 with your board ID
  2,
  false,
  NOW(),
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM lists WHERE board_id = 2 AND name = 'Done' AND is_deleted = false
);

-- ============================================
-- STEP 3: Insert Sample Cards
-- ============================================
-- Replace USER_ID and BOARD_ID with your actual IDs
-- Cards for "To Do" list

INSERT INTO cards (title, description, list_id, position, created_by, is_deleted, created_at, updated_at)
SELECT 
  'Setup project repository',
  'Initialize Git repository and push to remote',
  l.id,
  0,
  1,  -- CHANGE THIS: Replace 1 with your user ID
  false,
  NOW(),
  NOW()
FROM lists l
WHERE l.board_id = 2  -- CHANGE THIS: Replace 2 with your board ID
  AND l.name = 'To Do'
  AND l.is_deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM cards WHERE list_id = l.id AND title = 'Setup project repository' AND is_deleted = false
  )
LIMIT 1;

INSERT INTO cards (title, description, list_id, position, created_by, is_deleted, created_at, updated_at)
SELECT 
  'Design database schema',
  'Create ER diagram and define tables',
  l.id,
  1,
  1,  -- CHANGE THIS: Replace 1 with your user ID
  false,
  NOW(),
  NOW()
FROM lists l
WHERE l.board_id = 2  -- CHANGE THIS: Replace 2 with your board ID
  AND l.name = 'To Do'
  AND l.is_deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM cards WHERE list_id = l.id AND title = 'Design database schema' AND is_deleted = false
  )
LIMIT 1;

INSERT INTO cards (title, description, list_id, position, created_by, is_deleted, created_at, updated_at)
SELECT 
  'Create API documentation',
  'Document all REST endpoints',
  l.id,
  2,
  1,  -- CHANGE THIS: Replace 1 with your user ID
  false,
  NOW(),
  NOW()
FROM lists l
WHERE l.board_id = 2  -- CHANGE THIS: Replace 2 with your board ID
  AND l.name = 'To Do'
  AND l.is_deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM cards WHERE list_id = l.id AND title = 'Create API documentation' AND is_deleted = false
  )
LIMIT 1;

-- Cards for "In Progress" list

INSERT INTO cards (title, description, list_id, position, created_by, is_deleted, created_at, updated_at)
SELECT 
  'Implement authentication',
  'JWT token generation and validation',
  l.id,
  0,
  1,  -- CHANGE THIS: Replace 1 with your user ID
  false,
  NOW(),
  NOW()
FROM lists l
WHERE l.board_id = 2  -- CHANGE THIS: Replace 2 with your board ID
  AND l.name = 'In Progress'
  AND l.is_deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM cards WHERE list_id = l.id AND title = 'Implement authentication' AND is_deleted = false
  )
LIMIT 1;

INSERT INTO cards (title, description, list_id, position, created_by, is_deleted, created_at, updated_at)
SELECT 
  'Add unit tests',
  'Write tests for services and controllers',
  l.id,
  1,
  1,  -- CHANGE THIS: Replace 1 with your user ID
  false,
  NOW(),
  NOW()
FROM lists l
WHERE l.board_id = 2  -- CHANGE THIS: Replace 2 with your board ID
  AND l.name = 'In Progress'
  AND l.is_deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM cards WHERE list_id = l.id AND title = 'Add unit tests' AND is_deleted = false
  )
LIMIT 1;

-- Cards for "Done" list

INSERT INTO cards (title, description, list_id, position, created_by, is_deleted, created_at, updated_at)
SELECT 
  'Setup development environment',
  'Install dependencies and configure IDE',
  l.id,
  0,
  1,  -- CHANGE THIS: Replace 1 with your user ID
  false,
  NOW(),
  NOW()
FROM lists l
WHERE l.board_id = 2  -- CHANGE THIS: Replace 2 with your board ID
  AND l.name = 'Done'
  AND l.is_deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM cards WHERE list_id = l.id AND title = 'Setup development environment' AND is_deleted = false
  )
LIMIT 1;

INSERT INTO cards (title, description, list_id, position, created_by, is_deleted, created_at, updated_at)
SELECT 
  'Create project structure',
  'Setup folder structure and basic configuration',
  l.id,
  1,
  1,  -- CHANGE THIS: Replace 1 with your user ID
  false,
  NOW(),
  NOW()
FROM lists l
WHERE l.board_id = 2  -- CHANGE THIS: Replace 2 with your board ID
  AND l.name = 'Done'
  AND l.is_deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM cards WHERE list_id = l.id AND title = 'Create project structure' AND is_deleted = false
  )
LIMIT 1;
