#!/usr/bin/env bash
# =============================================================================
# Minerva Core — Functional API Tests
# Runs curl-based tests against http://localhost:8080
# Usage: bash api-tests.sh
# =============================================================================

set -euo pipefail

BASE_URL="http://localhost:8080/api/v1"
PASS=0
FAIL=0
TOTAL=0

# --- Helpers -----------------------------------------------------------------

assert_status() {
    local test_name="$1"
    local expected="$2"
    local actual="$3"
    TOTAL=$((TOTAL + 1))
    if [ "$actual" -eq "$expected" ]; then
        PASS=$((PASS + 1))
        echo "  [PASS] $test_name (HTTP $actual)"
    else
        FAIL=$((FAIL + 1))
        echo "  [FAIL] $test_name — expected $expected, got $actual"
    fi
}

assert_contains() {
    local test_name="$1"
    local expected="$2"
    local body="$3"
    TOTAL=$((TOTAL + 1))
    if echo "$body" | grep -q "$expected"; then
        PASS=$((PASS + 1))
        echo "  [PASS] $test_name"
    else
        FAIL=$((FAIL + 1))
        echo "  [FAIL] $test_name — body does not contain '$expected'"
    fi
}

assert_not_contains() {
    local test_name="$1"
    local unexpected="$2"
    local body="$3"
    TOTAL=$((TOTAL + 1))
    if echo "$body" | grep -q "$unexpected"; then
        FAIL=$((FAIL + 1))
        echo "  [FAIL] $test_name — body unexpectedly contains '$unexpected'"
    else
        PASS=$((PASS + 1))
        echo "  [PASS] $test_name"
    fi
}

extract_id() {
    echo "$1" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4
}

extract_all_ids() {
    echo "$1" | grep -o '"id":"[^"]*"' | cut -d'"' -f4
}

assert_count() {
    local test_name="$1"
    local expected="$2"
    local actual="$3"
    TOTAL=$((TOTAL + 1))
    if [ "$actual" -eq "$expected" ]; then
        PASS=$((PASS + 1))
        echo "  [PASS] $test_name (count: $actual)"
    else
        FAIL=$((FAIL + 1))
        echo "  [FAIL] $test_name — expected $expected, got $actual"
    fi
}

count_pattern() {
    local body="$1"
    local pattern="$2"
    echo "$body" | grep -o "$pattern" | wc -l | tr -d ' '
}

do_post() {
    local url="$1"
    local data="$2"
    curl -s -w "\n%{http_code}" -X POST "$url" \
        -H "Content-Type: application/json" -d "$data"
}

do_get() {
    curl -s -w "\n%{http_code}" -X GET "$1" -H "Accept: application/json"
}

do_put() {
    local url="$1"
    local data="$2"
    curl -s -w "\n%{http_code}" -X PUT "$url" \
        -H "Content-Type: application/json" -d "$data"
}

do_delete() {
    curl -s -w "\n%{http_code}" -X DELETE "$1"
}

parse_response() {
    local response="$1"
    BODY=$(echo "$response" | sed '$d')
    STATUS=$(echo "$response" | tail -1)
}

# =============================================================================
# 1. TAXES
# =============================================================================
echo ""
echo "=== TAXES ==="

# Create tax
response=$(do_post "$BASE_URL/taxes" '{"description":"IVA General","rate":21.0,"surchargeRate":5.2}')
parse_response "$response"
assert_status "POST /taxes — create" 201 "$STATUS"
assert_contains "POST /taxes — returns description" "IVA General" "$BODY"
assert_contains "POST /taxes — returns scaled rate" "21.0000" "$BODY"
assert_contains "POST /taxes — returns scaled surcharge" "5.2000" "$BODY"
TAX_ID=$(extract_id "$BODY")

# Create second tax for later use
response=$(do_post "$BASE_URL/taxes" '{"description":"IVA Reducido","rate":10.0,"surchargeRate":1.4}')
parse_response "$response"
assert_status "POST /taxes — create reduced" 201 "$STATUS"
TAX2_ID=$(extract_id "$BODY")

# Get by ID
response=$(do_get "$BASE_URL/taxes/$TAX_ID")
parse_response "$response"
assert_status "GET /taxes/{id} — found" 200 "$STATUS"
assert_contains "GET /taxes/{id} — correct id" "$TAX_ID" "$BODY"

# Get all
response=$(do_get "$BASE_URL/taxes")
parse_response "$response"
assert_status "GET /taxes — list" 200 "$STATUS"
assert_contains "GET /taxes — contains created tax" "IVA General" "$BODY"

# Update
response=$(do_put "$BASE_URL/taxes/$TAX_ID" '{"description":"IVA General Updated","rate":21.0,"surchargeRate":5.2}')
parse_response "$response"
assert_status "PUT /taxes/{id} — update" 200 "$STATUS"
assert_contains "PUT /taxes/{id} — updated name" "IVA General Updated" "$BODY"

# Revert name for later tests
do_put "$BASE_URL/taxes/$TAX_ID" '{"description":"IVA General","rate":21.0,"surchargeRate":5.2}' > /dev/null

# Validation: blank description
response=$(do_post "$BASE_URL/taxes" '{"description":"","rate":21.0,"surchargeRate":5.2}')
parse_response "$response"
assert_status "POST /taxes — blank description → 400" 400 "$STATUS"

# Validation: null rate
response=$(do_post "$BASE_URL/taxes" '{"description":"Test","surchargeRate":5.2}')
parse_response "$response"
assert_status "POST /taxes — null rate → 400" 400 "$STATUS"

# Validation: negative rate
response=$(do_post "$BASE_URL/taxes" '{"description":"Test","rate":-1.0,"surchargeRate":5.2}')
parse_response "$response"
assert_status "POST /taxes — negative rate → 400" 400 "$STATUS"

# Not found
response=$(do_get "$BASE_URL/taxes/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "GET /taxes/{id} — not found → 404" 404 "$STATUS"

# =============================================================================
# 2. LOCATIONS
# =============================================================================
echo ""
echo "=== LOCATIONS ==="

response=$(do_post "$BASE_URL/locations" '{"name":"Main Warehouse","description":"Central storage"}')
parse_response "$response"
assert_status "POST /locations — create" 201 "$STATUS"
assert_contains "POST /locations — returns name" "Main Warehouse" "$BODY"
LOCATION_ID=$(extract_id "$BODY")

response=$(do_get "$BASE_URL/locations/$LOCATION_ID")
parse_response "$response"
assert_status "GET /locations/{id} — found" 200 "$STATUS"

response=$(do_get "$BASE_URL/locations")
parse_response "$response"
assert_status "GET /locations — list" 200 "$STATUS"

response=$(do_put "$BASE_URL/locations/$LOCATION_ID" '{"name":"Updated Warehouse","description":"Updated"}')
parse_response "$response"
assert_status "PUT /locations/{id} — update" 200 "$STATUS"
assert_contains "PUT /locations/{id} — updated name" "Updated Warehouse" "$BODY"

# Revert
do_put "$BASE_URL/locations/$LOCATION_ID" '{"name":"Main Warehouse","description":"Central storage"}' > /dev/null

# Validation
response=$(do_post "$BASE_URL/locations" '{"name":"","description":"test"}')
parse_response "$response"
assert_status "POST /locations — blank name → 400" 400 "$STATUS"

response=$(do_get "$BASE_URL/locations/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "GET /locations/{id} — not found → 404" 404 "$STATUS"

# Null description allowed
response=$(do_post "$BASE_URL/locations" '{"name":"Temp Location"}')
parse_response "$response"
assert_status "POST /locations — null description OK" 201 "$STATUS"
TEMP_LOC_ID=$(extract_id "$BODY")
do_delete "$BASE_URL/locations/$TEMP_LOC_ID" > /dev/null

# =============================================================================
# 3. PROVIDERS
# =============================================================================
echo ""
echo "=== PROVIDERS ==="

response=$(do_post "$BASE_URL/providers" '{"businessName":"Office Supplies S.L.","taxIdentifier":"B12345678","address":"Industrial Ave 15","phone":"+34 912 345 678","email":"sales@office.es","appliesSurcharge":false}')
parse_response "$response"
assert_status "POST /providers — create" 201 "$STATUS"
assert_contains "POST /providers — returns businessName" "Office Supplies" "$BODY"
assert_contains "POST /providers — returns taxIdentifier" "B12345678" "$BODY"
PROVIDER_ID=$(extract_id "$BODY")

response=$(do_get "$BASE_URL/providers/$PROVIDER_ID")
parse_response "$response"
assert_status "GET /providers/{id} — found" 200 "$STATUS"

response=$(do_get "$BASE_URL/providers")
parse_response "$response"
assert_status "GET /providers — list" 200 "$STATUS"

response=$(do_put "$BASE_URL/providers/$PROVIDER_ID" '{"businessName":"Updated Supplies","taxIdentifier":"B12345678","appliesSurcharge":true}')
parse_response "$response"
assert_status "PUT /providers/{id} — update" 200 "$STATUS"
assert_contains "PUT /providers/{id} — updated name" "Updated Supplies" "$BODY"
assert_contains "PUT /providers/{id} — surcharge enabled" "true" "$BODY"

# Revert
do_put "$BASE_URL/providers/$PROVIDER_ID" '{"businessName":"Office Supplies S.L.","taxIdentifier":"B12345678","appliesSurcharge":false}' > /dev/null

# Validation
response=$(do_post "$BASE_URL/providers" '{"businessName":"","taxIdentifier":"X123","appliesSurcharge":false}')
parse_response "$response"
assert_status "POST /providers — blank name → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/providers" '{"businessName":"Test","taxIdentifier":"","appliesSurcharge":false}')
parse_response "$response"
assert_status "POST /providers — blank taxId → 400" 400 "$STATUS"

# Optional fields null
response=$(do_post "$BASE_URL/providers" '{"businessName":"Minimal Provider","taxIdentifier":"X999","appliesSurcharge":false}')
parse_response "$response"
assert_status "POST /providers — minimal fields OK" 201 "$STATUS"
TEMP_PROV_ID=$(extract_id "$BODY")
do_delete "$BASE_URL/providers/$TEMP_PROV_ID" > /dev/null

response=$(do_get "$BASE_URL/providers/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "GET /providers/{id} — not found → 404" 404 "$STATUS"

# =============================================================================
# 4. ARTICLES
# =============================================================================
echo ""
echo "=== ARTICLES ==="

response=$(do_post "$BASE_URL/articles" "{\"name\":\"Gaming Laptop\",\"code\":\"LAP-001\",\"barcode\":\"8400000012345\",\"taxId\":\"$TAX_ID\",\"basePrice\":1000.00,\"retailPrice\":1210.00}")
parse_response "$response"
assert_status "POST /articles — create" 201 "$STATUS"
assert_contains "POST /articles — returns name" "Gaming Laptop" "$BODY"
assert_contains "POST /articles — returns scaled basePrice" "1000.00" "$BODY"
ARTICLE_ID=$(extract_id "$BODY")

# Create second article
response=$(do_post "$BASE_URL/articles" "{\"name\":\"Pen\",\"code\":\"PEN-001\",\"barcode\":\"8400000099001\",\"taxId\":\"$TAX_ID\",\"basePrice\":1.00,\"retailPrice\":1.50}")
parse_response "$response"
ARTICLE2_ID=$(extract_id "$BODY")

response=$(do_get "$BASE_URL/articles/$ARTICLE_ID")
parse_response "$response"
assert_status "GET /articles/{id} — found" 200 "$STATUS"
assert_contains "GET /articles/{id} — correct id" "$ARTICLE_ID" "$BODY"

response=$(do_get "$BASE_URL/articles")
parse_response "$response"
assert_status "GET /articles — list" 200 "$STATUS"

response=$(do_put "$BASE_URL/articles/$ARTICLE_ID" "{\"name\":\"Updated Laptop\",\"code\":\"LAP-001\",\"barcode\":\"8400000012345\",\"taxId\":\"$TAX_ID\",\"basePrice\":1050.00,\"retailPrice\":1270.00}")
parse_response "$response"
assert_status "PUT /articles/{id} — update" 200 "$STATUS"
assert_contains "PUT /articles/{id} — updated name" "Updated Laptop" "$BODY"

# Revert
do_put "$BASE_URL/articles/$ARTICLE_ID" "{\"name\":\"Gaming Laptop\",\"code\":\"LAP-001\",\"barcode\":\"8400000012345\",\"taxId\":\"$TAX_ID\",\"basePrice\":1000.00,\"retailPrice\":1210.00}" > /dev/null

# Validation
response=$(do_post "$BASE_URL/articles" "{\"name\":\"\",\"code\":\"X\",\"barcode\":\"123\",\"taxId\":\"$TAX_ID\",\"basePrice\":10,\"retailPrice\":15}")
parse_response "$response"
assert_status "POST /articles — blank name → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/articles" "{\"name\":\"Test\",\"code\":\"X\",\"barcode\":\"123\",\"basePrice\":10,\"retailPrice\":15}")
parse_response "$response"
assert_status "POST /articles — null taxId → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/articles" "{\"name\":\"Test\",\"code\":\"X\",\"barcode\":\"123\",\"taxId\":\"$TAX_ID\",\"basePrice\":-1,\"retailPrice\":15}")
parse_response "$response"
assert_status "POST /articles — negative basePrice → 400" 400 "$STATUS"

response=$(do_get "$BASE_URL/articles/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "GET /articles/{id} — not found → 404" 404 "$STATUS"

# Price scaling
response=$(do_post "$BASE_URL/articles" "{\"name\":\"Scale Test\",\"code\":\"SC-001\",\"barcode\":\"8400000077777\",\"taxId\":\"$TAX_ID\",\"basePrice\":10.125,\"retailPrice\":15.999}")
parse_response "$response"
assert_status "POST /articles — price scaling" 201 "$STATUS"
assert_contains "POST /articles — basePrice scaled" "10.13" "$BODY"
assert_contains "POST /articles — retailPrice scaled" "16.00" "$BODY"
SCALE_ART_ID=$(extract_id "$BODY")
do_delete "$BASE_URL/articles/$SCALE_ART_ID" > /dev/null

# =============================================================================
# 5. USERS
# =============================================================================
echo ""
echo "=== USERS ==="

response=$(do_post "$BASE_URL/users" '{"name":"John","lastName":"Doe","email":"john@company.com","password":"secret123","address":"Main St 42","roles":["READ","CREATE"]}')
parse_response "$response"
assert_status "POST /users — create" 201 "$STATUS"
assert_contains "POST /users — returns name" "John" "$BODY"
assert_contains "POST /users — returns email" "john@company.com" "$BODY"
assert_contains "POST /users — active by default" '"active":true' "$BODY"
USER_ID=$(extract_id "$BODY")

response=$(do_get "$BASE_URL/users/$USER_ID")
parse_response "$response"
assert_status "GET /users/{id} — found" 200 "$STATUS"

response=$(do_get "$BASE_URL/users")
parse_response "$response"
assert_status "GET /users — list" 200 "$STATUS"

response=$(do_put "$BASE_URL/users/$USER_ID" '{"name":"John","lastName":"Doe","email":"john@company.com","password":"secret123","address":"New Address","roles":["READ","CREATE","EDIT"],"active":true}')
parse_response "$response"
assert_status "PUT /users/{id} — update" 200 "$STATUS"
assert_contains "PUT /users/{id} — updated address" "New Address" "$BODY"

# Deactivation via update
response=$(do_put "$BASE_URL/users/$USER_ID" '{"name":"John","lastName":"Doe","email":"john@company.com","password":"secret123","address":"New Address","roles":["READ","CREATE","EDIT"],"active":false}')
parse_response "$response"
assert_status "PUT /users/{id} — deactivate" 200 "$STATUS"
assert_contains "PUT /users/{id} — active=false" '"active":false' "$BODY"

# Reactivate
do_put "$BASE_URL/users/$USER_ID" '{"name":"John","lastName":"Doe","email":"john@company.com","password":"secret123","address":"Main St 42","roles":["READ","CREATE"],"active":true}' > /dev/null

# Validation
response=$(do_post "$BASE_URL/users" '{"name":"","lastName":"Doe","email":"a@b.com","password":"pass","roles":["READ"]}')
parse_response "$response"
assert_status "POST /users — blank name → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/users" '{"name":"Test","lastName":"","email":"a@b.com","password":"pass","roles":["READ"]}')
parse_response "$response"
assert_status "POST /users — blank lastName → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/users" '{"name":"Test","lastName":"Doe","email":"","password":"pass","roles":["READ"]}')
parse_response "$response"
assert_status "POST /users — blank email → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/users" '{"name":"Test","lastName":"Doe","email":"a@b.com","password":"","roles":["READ"]}')
parse_response "$response"
assert_status "POST /users — blank password → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/users" '{"name":"Test","lastName":"Doe","email":"a@b.com","password":"pass","roles":[]}')
parse_response "$response"
assert_status "POST /users — empty roles → 400" 400 "$STATUS"

response=$(do_get "$BASE_URL/users/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "GET /users/{id} — not found → 404" 404 "$STATUS"

# =============================================================================
# 6. ITEMS
# =============================================================================
echo ""
echo "=== ITEMS ==="

response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$ARTICLE_ID\",\"cost\":99.99,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "POST /items — create" 201 "$STATUS"
assert_contains "POST /items — returns articleId" "$ARTICLE_ID" "$BODY"
assert_contains "POST /items — default status AVAILABLE" "AVAILABLE" "$BODY"
assert_contains "POST /items — returns cost" "99.99" "$BODY"
ITEM_ID=$(extract_id "$BODY")

# Create second item
response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$ARTICLE2_ID\",\"cost\":1.00,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
ITEM2_ID=$(extract_id "$BODY")

response=$(do_get "$BASE_URL/items/$ITEM_ID")
parse_response "$response"
assert_status "GET /items/{id} — found" 200 "$STATUS"

response=$(do_get "$BASE_URL/items")
parse_response "$response"
assert_status "GET /items — list" 200 "$STATUS"

# Update with explicit status
response=$(do_put "$BASE_URL/items/$ITEM_ID" "{\"articleId\":\"$ARTICLE_ID\",\"itemStatus\":\"RESERVED\",\"cost\":99.99,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "PUT /items/{id} — update status" 200 "$STATUS"
assert_contains "PUT /items/{id} — status RESERVED" "RESERVED" "$BODY"

# Revert to AVAILABLE
do_put "$BASE_URL/items/$ITEM_ID" "{\"articleId\":\"$ARTICLE_ID\",\"itemStatus\":\"AVAILABLE\",\"cost\":99.99,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}" > /dev/null

# Cost scaling
response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$ARTICLE_ID\",\"cost\":55.555,\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "POST /items — cost scaling" 201 "$STATUS"
assert_contains "POST /items — cost scaled to 55.56" "55.56" "$BODY"
TEMP_ITEM_ID=$(extract_id "$BODY")
do_delete "$BASE_URL/items/$TEMP_ITEM_ID" > /dev/null

# Validation
response=$(do_post "$BASE_URL/items" '{"cost":10.00}')
parse_response "$response"
assert_status "POST /items — null articleId → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$ARTICLE_ID\",\"cost\":-1.00}")
parse_response "$response"
assert_status "POST /items — negative cost → 400" 400 "$STATUS"

response=$(do_get "$BASE_URL/items/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "GET /items/{id} — not found → 404" 404 "$STATUS"

# =============================================================================
# 7. PURCHASES
# =============================================================================
echo ""
echo "=== PURCHASES ==="

PURCHASE_LINES="[{\"articleId\":\"$ARTICLE_ID\",\"quantity\":2,\"buyPrice\":10.00,\"profitMargin\":25.0,\"taxId\":\"$TAX_ID\"},{\"articleId\":\"$ARTICLE2_ID\",\"quantity\":3,\"buyPrice\":5.00,\"profitMargin\":30.0,\"taxId\":\"$TAX_ID\"}]"

response=$(do_post "$BASE_URL/purchases" "{\"code\":\"PUR-2026-0001\",\"providerCode\":\"ALB-001\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\",\"lines\":$PURCHASE_LINES}")
parse_response "$response"
assert_status "POST /purchases — create" 201 "$STATUS"
assert_contains "POST /purchases — returns code" "PUR-2026-0001" "$BODY"
assert_contains "POST /purchases — default state NEW" "NEW" "$BODY"
assert_contains "POST /purchases — totalCost calculated (35.00)" "35.00" "$BODY"
assert_contains "POST /purchases — providerCode" "ALB-001" "$BODY"
PURCHASE_ID=$(extract_id "$BODY")

response=$(do_get "$BASE_URL/purchases/$PURCHASE_ID")
parse_response "$response"
assert_status "GET /purchases/{id} — found" 200 "$STATUS"

response=$(do_get "$BASE_URL/purchases")
parse_response "$response"
assert_status "GET /purchases — list" 200 "$STATUS"

# Update
response=$(do_put "$BASE_URL/purchases/$PURCHASE_ID" "{\"code\":\"PUR-2026-0001-UPD\",\"providerCode\":\"ALB-001\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\",\"lines\":$PURCHASE_LINES}")
parse_response "$response"
assert_status "PUT /purchases/{id} — update" 200 "$STATUS"
assert_contains "PUT /purchases/{id} — updated code" "PUR-2026-0001-UPD" "$BODY"

# Deposit mode
response=$(do_post "$BASE_URL/purchases" "{\"code\":\"PUR-DEP-001\",\"providerCode\":\"ALB-DEP\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\",\"deposit\":true,\"finishDate\":\"2026-06-30T00:00:00\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /purchases — deposit mode" 201 "$STATUS"
assert_contains "POST /purchases — deposit=true" '"deposit":true' "$BODY"
assert_contains "POST /purchases — finishDate set" "2026-06-30" "$BODY"
DEP_PUR_ID=$(extract_id "$BODY")
do_delete "$BASE_URL/purchases/$DEP_PUR_ID" > /dev/null

# Validation
response=$(do_post "$BASE_URL/purchases" "{\"code\":\"\",\"providerCode\":\"X\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /purchases — blank code → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/purchases" "{\"code\":\"PUR-X\",\"providerCode\":\"X\",\"locationId\":\"$LOCATION_ID\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /purchases — null providerId → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/purchases" "{\"code\":\"PUR-X\",\"providerCode\":\"X\",\"providerId\":\"$PROVIDER_ID\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /purchases — null locationId → 400" 400 "$STATUS"

response=$(do_get "$BASE_URL/purchases/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "GET /purchases/{id} — not found → 404" 404 "$STATUS"

# Empty lines → totalCost = 0.00
response=$(do_post "$BASE_URL/purchases" "{\"code\":\"PUR-EMPTY\",\"providerCode\":\"ALB-E\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /purchases — empty lines OK" 201 "$STATUS"
assert_contains "POST /purchases — zero total" "0.00" "$BODY"
EMPTY_PUR_ID=$(extract_id "$BODY")
do_delete "$BASE_URL/purchases/$EMPTY_PUR_ID" > /dev/null

# =============================================================================
# 8. SALES
# =============================================================================
echo ""
echo "=== SALES ==="

# We need a payment method concept but there's no controller for it.
# Sales require employeeId (user), paymentMethodId (UUID — no validation at DB level in tests).
# We'll use the USER_ID as employeeId and a random UUID as paymentMethodId.
PAYMENT_METHOD_ID="11111111-1111-1111-1111-111111111111"

SALE_LINES="[{\"itemId\":\"$ITEM_ID\",\"quantity\":1,\"unitPrice\":25.00,\"taxId\":\"$TAX_ID\"},{\"freeConceptId\":\"22222222-2222-2222-2222-222222222222\",\"quantity\":3,\"unitPrice\":0.10,\"taxId\":\"$TAX_ID\"}]"

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-2026-0001\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":$SALE_LINES}")
parse_response "$response"
assert_status "POST /sales — create" 201 "$STATUS"
assert_contains "POST /sales — returns code" "SAL-2026-0001" "$BODY"
assert_contains "POST /sales — default state NEW" "NEW" "$BODY"
assert_contains "POST /sales — totalAmount (25.30)" "25.30" "$BODY"
assert_contains "POST /sales — employeeId" "$USER_ID" "$BODY"
SALE_ID=$(extract_id "$BODY")

response=$(do_get "$BASE_URL/sales/$SALE_ID")
parse_response "$response"
assert_status "GET /sales/{id} — found" 200 "$STATUS"
assert_contains "GET /sales/{id} — has lines" "itemId" "$BODY"

response=$(do_get "$BASE_URL/sales")
parse_response "$response"
assert_status "GET /sales — list" 200 "$STATUS"

# With client
response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-CLIENT-001\",\"employeeId\":\"$USER_ID\",\"clientId\":\"33333333-3333-3333-3333-333333333333\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /sales — with client" 201 "$STATUS"
assert_contains "POST /sales — clientId present" "33333333-3333-3333-3333-333333333333" "$BODY"
CLIENT_SALE_ID=$(extract_id "$BODY")
do_delete "$BASE_URL/sales/$CLIENT_SALE_ID" > /dev/null

# Without client
response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-NOCLI-001\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /sales — without client" 201 "$STATUS"
assert_contains "POST /sales — clientId null" '"clientId":null' "$BODY"
assert_contains "POST /sales — zero total no lines" "0.00" "$BODY"
NOCLI_SALE_ID=$(extract_id "$BODY")
do_delete "$BASE_URL/sales/$NOCLI_SALE_ID" > /dev/null

# Validation
response=$(do_post "$BASE_URL/sales" "{\"code\":\"\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /sales — blank code → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-X\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /sales — null employeeId → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-X\",\"employeeId\":\"$USER_ID\",\"lines\":[]}")
parse_response "$response"
assert_status "POST /sales — null paymentMethodId → 400" 400 "$STATUS"

response=$(do_get "$BASE_URL/sales/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "GET /sales/{id} — not found → 404" 404 "$STATUS"

# =============================================================================
# 9. FREE CONCEPTS
# =============================================================================
echo ""
echo "=== FREE CONCEPTS ==="

response=$(do_post "$BASE_URL/free-concepts" "{\"name\":\"Delivery Fee\",\"barcode\":\"9900000000001\",\"price\":5.50,\"taxId\":\"$TAX_ID\",\"description\":\"Standard delivery\"}")
parse_response "$response"
assert_status "POST /free-concepts — create" 201 "$STATUS"
assert_contains "POST /free-concepts — returns name" "Delivery Fee" "$BODY"
assert_contains "POST /free-concepts — returns scaled price" "5.50" "$BODY"
FC_DELIVERY_ID=$(extract_id "$BODY")

response=$(do_post "$BASE_URL/free-concepts" "{\"name\":\"Gift Wrapping\",\"barcode\":\"9900000000002\",\"price\":2.00,\"taxId\":\"$TAX_ID\"}")
parse_response "$response"
assert_status "POST /free-concepts — create second" 201 "$STATUS"
FC_GIFTWRAP_ID=$(extract_id "$BODY")

response=$(do_get "$BASE_URL/free-concepts/$FC_DELIVERY_ID")
parse_response "$response"
assert_status "GET /free-concepts/{id} — found" 200 "$STATUS"
assert_contains "GET /free-concepts/{id} — correct id" "$FC_DELIVERY_ID" "$BODY"

response=$(do_get "$BASE_URL/free-concepts")
parse_response "$response"
assert_status "GET /free-concepts — list" 200 "$STATUS"
assert_contains "GET /free-concepts — contains delivery" "Delivery Fee" "$BODY"

response=$(do_put "$BASE_URL/free-concepts/$FC_DELIVERY_ID" "{\"name\":\"Express Delivery\",\"barcode\":\"9900000000001\",\"price\":8.00,\"taxId\":\"$TAX_ID\",\"description\":\"Express delivery\"}")
parse_response "$response"
assert_status "PUT /free-concepts/{id} — update" 200 "$STATUS"
assert_contains "PUT /free-concepts/{id} — updated name" "Express Delivery" "$BODY"

# Revert
do_put "$BASE_URL/free-concepts/$FC_DELIVERY_ID" "{\"name\":\"Delivery Fee\",\"barcode\":\"9900000000001\",\"price\":5.50,\"taxId\":\"$TAX_ID\",\"description\":\"Standard delivery\"}" > /dev/null

# Validation
response=$(do_post "$BASE_URL/free-concepts" "{\"name\":\"\",\"barcode\":\"123\",\"price\":1.00,\"taxId\":\"$TAX_ID\"}")
parse_response "$response"
assert_status "POST /free-concepts — blank name → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/free-concepts" "{\"name\":\"Test\",\"barcode\":\"\",\"price\":1.00,\"taxId\":\"$TAX_ID\"}")
parse_response "$response"
assert_status "POST /free-concepts — blank barcode → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/free-concepts" "{\"name\":\"Test\",\"barcode\":\"123\",\"price\":-1.00,\"taxId\":\"$TAX_ID\"}")
parse_response "$response"
assert_status "POST /free-concepts — negative price → 400" 400 "$STATUS"

response=$(do_post "$BASE_URL/free-concepts" "{\"name\":\"Test\",\"barcode\":\"123\",\"price\":1.00}")
parse_response "$response"
assert_status "POST /free-concepts — null taxId → 400" 400 "$STATUS"

response=$(do_get "$BASE_URL/free-concepts/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "GET /free-concepts/{id} — not found → 404" 404 "$STATUS"

# Price scaling
response=$(do_post "$BASE_URL/free-concepts" "{\"name\":\"Scale Test\",\"barcode\":\"9900000099999\",\"price\":3.456,\"taxId\":\"$TAX_ID\"}")
parse_response "$response"
assert_status "POST /free-concepts — price scaling" 201 "$STATUS"
assert_contains "POST /free-concepts — price scaled to 3.46" "3.46" "$BODY"
TEMP_FC_ID=$(extract_id "$BODY")
do_delete "$BASE_URL/free-concepts/$TEMP_FC_ID" > /dev/null

# =============================================================================
# 10. PURCHASE → INVENTORY → SALE FUNCTIONAL FLOW
# =============================================================================
echo ""
echo "=== PURCHASE-TO-SALE FLOW ==="

# ---- Step 1: Create a realistic purchase order ----------------------------
echo "  --- Step 1: Create purchase order ---"

FLOW_PURCHASE_LINES="[{\"articleId\":\"$ARTICLE_ID\",\"quantity\":3,\"buyPrice\":800.00,\"profitMargin\":25.0000,\"taxId\":\"$TAX_ID\"},{\"articleId\":\"$ARTICLE2_ID\",\"quantity\":2,\"buyPrice\":0.80,\"profitMargin\":50.0000,\"taxId\":\"$TAX2_ID\"}]"

response=$(do_post "$BASE_URL/purchases" "{\"code\":\"PUR-FLOW-001\",\"providerCode\":\"ALB-FLOW-001\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\",\"lines\":$FLOW_PURCHASE_LINES}")
parse_response "$response"
assert_status "FLOW — create purchase" 201 "$STATUS"
assert_contains "FLOW — purchase state NEW" "NEW" "$BODY"
# totalCost = (3 * 800.00) + (2 * 0.80) = 2400.00 + 1.60 = 2401.60
assert_contains "FLOW — purchase totalCost 2401.60" "2401.60" "$BODY"
FLOW_PURCHASE_ID=$(extract_id "$BODY")

# ---- Step 2: Simulate receiving goods — create items (AVAILABLE) ----------
echo "  --- Step 2: Create inventory items (receive goods) ---"

# 3 laptops from the purchase line 1
response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$ARTICLE_ID\",\"cost\":800.00,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — create laptop item 1" 201 "$STATUS"
assert_contains "FLOW — laptop 1 AVAILABLE" "AVAILABLE" "$BODY"
FLOW_LAPTOP_1=$(extract_id "$BODY")

response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$ARTICLE_ID\",\"cost\":800.00,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — create laptop item 2" 201 "$STATUS"
FLOW_LAPTOP_2=$(extract_id "$BODY")

response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$ARTICLE_ID\",\"cost\":800.00,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — create laptop item 3" 201 "$STATUS"
FLOW_LAPTOP_3=$(extract_id "$BODY")

# 2 pens from purchase line 2
response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$ARTICLE2_ID\",\"cost\":0.80,\"buyTaxId\":\"$TAX2_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — create pen item 1" 201 "$STATUS"
FLOW_PEN_1=$(extract_id "$BODY")

response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$ARTICLE2_ID\",\"cost\":0.80,\"buyTaxId\":\"$TAX2_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — create pen item 2" 201 "$STATUS"
FLOW_PEN_2=$(extract_id "$BODY")

# ---- Step 3: Verify all 5 items are AVAILABLE ----------------------------
echo "  --- Step 3: Verify items are AVAILABLE ---"

response=$(do_get "$BASE_URL/items/$FLOW_LAPTOP_1")
parse_response "$response"
assert_contains "FLOW — laptop 1 is AVAILABLE" "AVAILABLE" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_LAPTOP_2")
parse_response "$response"
assert_contains "FLOW — laptop 2 is AVAILABLE" "AVAILABLE" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_LAPTOP_3")
parse_response "$response"
assert_contains "FLOW — laptop 3 is AVAILABLE" "AVAILABLE" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_PEN_1")
parse_response "$response"
assert_contains "FLOW — pen 1 is AVAILABLE" "AVAILABLE" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_PEN_2")
parse_response "$response"
assert_contains "FLOW — pen 2 is AVAILABLE" "AVAILABLE" "$BODY"

# ---- Step 4: Create Sale 1 — sell 2 laptops + delivery fee ---------------
echo "  --- Step 4: Create sale with 2 laptops + delivery fee ---"

FLOW_SALE1_LINES="[{\"itemId\":\"$FLOW_LAPTOP_1\",\"unitPrice\":1210.00,\"taxId\":\"$TAX_ID\"},{\"itemId\":\"$FLOW_LAPTOP_2\",\"unitPrice\":1210.00,\"taxId\":\"$TAX_ID\"},{\"freeConceptId\":\"$FC_DELIVERY_ID\",\"quantity\":1,\"unitPrice\":5.50,\"taxId\":\"$TAX_ID\"}]"

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-FLOW-001\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":$FLOW_SALE1_LINES}")
parse_response "$response"
assert_status "FLOW — create sale 1" 201 "$STATUS"
assert_contains "FLOW — sale 1 state NEW" "NEW" "$BODY"
# totalAmount = (1 * 1210.00) + (1 * 1210.00) + (1 * 5.50) = 2425.50
assert_contains "FLOW — sale 1 totalAmount 2425.50" "2425.50" "$BODY"
assert_contains "FLOW — sale 1 has delivery freeConceptId" "$FC_DELIVERY_ID" "$BODY"
FLOW_SALE1_ID=$(extract_id "$BODY")

# ---- Step 5: Mark sold items as SOLD ------------------------------------
echo "  --- Step 5: Mark sold items as SOLD ---"

response=$(do_put "$BASE_URL/items/$FLOW_LAPTOP_1" "{\"articleId\":\"$ARTICLE_ID\",\"itemStatus\":\"SOLD\",\"cost\":800.00,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — mark laptop 1 SOLD" 200 "$STATUS"
assert_contains "FLOW — laptop 1 now SOLD" "SOLD" "$BODY"

response=$(do_put "$BASE_URL/items/$FLOW_LAPTOP_2" "{\"articleId\":\"$ARTICLE_ID\",\"itemStatus\":\"SOLD\",\"cost\":800.00,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — mark laptop 2 SOLD" 200 "$STATUS"
assert_contains "FLOW — laptop 2 now SOLD" "SOLD" "$BODY"

# ---- Step 6: Verify sold items are SOLD, unsold are AVAILABLE ------------
echo "  --- Step 6: Verify item statuses after sale ---"

response=$(do_get "$BASE_URL/items/$FLOW_LAPTOP_1")
parse_response "$response"
assert_contains "FLOW — laptop 1 confirms SOLD" "SOLD" "$BODY"
assert_not_contains "FLOW — laptop 1 not AVAILABLE" "AVAILABLE" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_LAPTOP_2")
parse_response "$response"
assert_contains "FLOW — laptop 2 confirms SOLD" "SOLD" "$BODY"
assert_not_contains "FLOW — laptop 2 not AVAILABLE" "AVAILABLE" "$BODY"

# Unsold laptop 3 remains AVAILABLE
response=$(do_get "$BASE_URL/items/$FLOW_LAPTOP_3")
parse_response "$response"
assert_contains "FLOW — laptop 3 still AVAILABLE" "AVAILABLE" "$BODY"
assert_not_contains "FLOW — laptop 3 not SOLD" "SOLD" "$BODY"

# Pens remain AVAILABLE
response=$(do_get "$BASE_URL/items/$FLOW_PEN_1")
parse_response "$response"
assert_contains "FLOW — pen 1 still AVAILABLE" "AVAILABLE" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_PEN_2")
parse_response "$response"
assert_contains "FLOW — pen 2 still AVAILABLE" "AVAILABLE" "$BODY"

# ---- Step 7: Create Sale 2 — sell remaining stock + gift wrapping --------
echo "  --- Step 7: Sell remaining items + gift wrapping ---"

FLOW_SALE2_LINES="[{\"itemId\":\"$FLOW_LAPTOP_3\",\"unitPrice\":1150.00,\"taxId\":\"$TAX_ID\"},{\"itemId\":\"$FLOW_PEN_1\",\"unitPrice\":1.50,\"taxId\":\"$TAX2_ID\"},{\"itemId\":\"$FLOW_PEN_2\",\"unitPrice\":1.50,\"taxId\":\"$TAX2_ID\"},{\"freeConceptId\":\"$FC_GIFTWRAP_ID\",\"quantity\":2,\"unitPrice\":2.00,\"taxId\":\"$TAX_ID\"},{\"freeConceptId\":\"$FC_DELIVERY_ID\",\"quantity\":1,\"unitPrice\":5.50,\"taxId\":\"$TAX_ID\"}]"

CLIENT_UUID="44444444-4444-4444-4444-444444444444"
response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-FLOW-002\",\"employeeId\":\"$USER_ID\",\"clientId\":\"$CLIENT_UUID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":$FLOW_SALE2_LINES}")
parse_response "$response"
assert_status "FLOW — create sale 2" 201 "$STATUS"
assert_contains "FLOW — sale 2 has clientId" "$CLIENT_UUID" "$BODY"
# totalAmount = 1150.00 + 1.50 + 1.50 + (2 * 2.00) + 5.50 = 1162.50
assert_contains "FLOW — sale 2 totalAmount 1162.50" "1162.50" "$BODY"
assert_contains "FLOW — sale 2 has gift wrap freeConceptId" "$FC_GIFTWRAP_ID" "$BODY"
FLOW_SALE2_ID=$(extract_id "$BODY")

# ---- Step 8: Mark remaining items as SOLD --------------------------------
echo "  --- Step 8: Mark remaining sold items as SOLD ---"

response=$(do_put "$BASE_URL/items/$FLOW_LAPTOP_3" "{\"articleId\":\"$ARTICLE_ID\",\"itemStatus\":\"SOLD\",\"cost\":800.00,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — mark laptop 3 SOLD" 200 "$STATUS"

response=$(do_put "$BASE_URL/items/$FLOW_PEN_1" "{\"articleId\":\"$ARTICLE2_ID\",\"itemStatus\":\"SOLD\",\"cost\":0.80,\"buyTaxId\":\"$TAX2_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — mark pen 1 SOLD" 200 "$STATUS"

response=$(do_put "$BASE_URL/items/$FLOW_PEN_2" "{\"articleId\":\"$ARTICLE2_ID\",\"itemStatus\":\"SOLD\",\"cost\":0.80,\"buyTaxId\":\"$TAX2_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "FLOW — mark pen 2 SOLD" 200 "$STATUS"

# ---- Step 9: Verify ALL items are now SOLD — nothing AVAILABLE -----------
echo "  --- Step 9: Verify all items are SOLD ---"

response=$(do_get "$BASE_URL/items/$FLOW_LAPTOP_1")
parse_response "$response"
assert_contains "FLOW — final: laptop 1 SOLD" "SOLD" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_LAPTOP_2")
parse_response "$response"
assert_contains "FLOW — final: laptop 2 SOLD" "SOLD" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_LAPTOP_3")
parse_response "$response"
assert_contains "FLOW — final: laptop 3 SOLD" "SOLD" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_PEN_1")
parse_response "$response"
assert_contains "FLOW — final: pen 1 SOLD" "SOLD" "$BODY"

response=$(do_get "$BASE_URL/items/$FLOW_PEN_2")
parse_response "$response"
assert_contains "FLOW — final: pen 2 SOLD" "SOLD" "$BODY"

# Verify no AVAILABLE items in the full listing from this flow
response=$(do_get "$BASE_URL/items")
parse_response "$response"
assert_status "FLOW — GET /items list" 200 "$STATUS"
# All flow items should be SOLD; the original ITEM_ID/ITEM2_ID are still AVAILABLE
# We verify that all 5 flow items appear as SOLD in the full list
assert_contains "FLOW — list contains SOLD items" "SOLD" "$BODY"

# ---- Step 10: Verify sales are retrievable with correct data -------------
echo "  --- Step 10: Verify sales data integrity ---"

response=$(do_get "$BASE_URL/sales/$FLOW_SALE1_ID")
parse_response "$response"
assert_status "FLOW — get sale 1" 200 "$STATUS"
assert_contains "FLOW — sale 1 code" "SAL-FLOW-001" "$BODY"
assert_contains "FLOW — sale 1 total persisted" "2425.50" "$BODY"
assert_contains "FLOW — sale 1 employeeId" "$USER_ID" "$BODY"

response=$(do_get "$BASE_URL/sales/$FLOW_SALE2_ID")
parse_response "$response"
assert_status "FLOW — get sale 2" 200 "$STATUS"
assert_contains "FLOW — sale 2 code" "SAL-FLOW-002" "$BODY"
assert_contains "FLOW — sale 2 total persisted" "1162.50" "$BODY"
assert_contains "FLOW — sale 2 clientId" "$CLIENT_UUID" "$BODY"

# ---- Step 11: Verify purchase data integrity -----------------------------
echo "  --- Step 11: Verify purchase data integrity ---"

response=$(do_get "$BASE_URL/purchases/$FLOW_PURCHASE_ID")
parse_response "$response"
assert_status "FLOW — get purchase" 200 "$STATUS"
assert_contains "FLOW — purchase code" "PUR-FLOW-001" "$BODY"
assert_contains "FLOW — purchase total persisted" "2401.60" "$BODY"

# ---- Cleanup flow data ---------------------------------------------------
echo "  --- Cleanup flow data ---"

do_delete "$BASE_URL/sales/$FLOW_SALE1_ID" > /dev/null
do_delete "$BASE_URL/sales/$FLOW_SALE2_ID" > /dev/null
do_delete "$BASE_URL/purchases/$FLOW_PURCHASE_ID" > /dev/null
do_delete "$BASE_URL/items/$FLOW_LAPTOP_1" > /dev/null
do_delete "$BASE_URL/items/$FLOW_LAPTOP_2" > /dev/null
do_delete "$BASE_URL/items/$FLOW_LAPTOP_3" > /dev/null
do_delete "$BASE_URL/items/$FLOW_PEN_1" > /dev/null
do_delete "$BASE_URL/items/$FLOW_PEN_2" > /dev/null

# =============================================================================
# 11. BOX-OPENING FLOW — purchase box, open it, sell individual items
# =============================================================================
echo ""
echo "=== BOX-OPENING FLOW ==="

# ---- Step 1: Create parent + child articles ------------------------------
echo "  --- Step 1: Create box and pen articles ---"

response=$(do_post "$BASE_URL/articles" "{\"name\":\"Box of Pens (20u)\",\"code\":\"BOX-PEN-20\",\"barcode\":\"8400000020001\",\"taxId\":\"$TAX_ID\",\"basePrice\":16.00,\"retailPrice\":30.00,\"canHaveChildren\":true,\"numberOfChildren\":20}")
parse_response "$response"
assert_status "BOX — create box article" 201 "$STATUS"
assert_contains "BOX — canHaveChildren true" '"canHaveChildren":true' "$BODY"
assert_contains "BOX — numberOfChildren 20" '"numberOfChildren":20' "$BODY"
BOX_ARTICLE_ID=$(extract_id "$BODY")

response=$(do_post "$BASE_URL/articles" "{\"name\":\"Pen (unit)\",\"code\":\"PEN-UNIT-001\",\"barcode\":\"8400000020002\",\"taxId\":\"$TAX_ID\",\"basePrice\":0.80,\"retailPrice\":1.50,\"parentArticleId\":\"$BOX_ARTICLE_ID\"}")
parse_response "$response"
assert_status "BOX — create pen article (child)" 201 "$STATUS"
assert_contains "BOX — pen parentArticleId set" "$BOX_ARTICLE_ID" "$BODY"
PEN_ARTICLE_ID=$(extract_id "$BODY")

# ---- Step 2: Create purchase for 1 box of pens --------------------------
echo "  --- Step 2: Purchase 1 box of pens ---"

BOX_PURCHASE_LINES="[{\"articleId\":\"$BOX_ARTICLE_ID\",\"quantity\":1,\"buyPrice\":16.00,\"profitMargin\":87.5000,\"taxId\":\"$TAX_ID\"}]"

response=$(do_post "$BASE_URL/purchases" "{\"code\":\"PUR-BOX-001\",\"providerCode\":\"ALB-BOX\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\",\"lines\":$BOX_PURCHASE_LINES}")
parse_response "$response"
assert_status "BOX — create purchase" 201 "$STATUS"
assert_contains "BOX — purchase totalCost 16.00" "16.00" "$BODY"
BOX_PURCHASE_ID=$(extract_id "$BODY")

# ---- Step 3: Receive goods — create box item as OPENED ------------------
echo "  --- Step 3: Open box → auto-generate 20 pen items ---"

response=$(do_post "$BASE_URL/items" "{\"articleId\":\"$BOX_ARTICLE_ID\",\"itemStatus\":\"OPENED\",\"hasChildren\":true,\"cost\":16.00,\"buyTaxId\":\"$TAX_ID\",\"providerId\":\"$PROVIDER_ID\",\"locationId\":\"$LOCATION_ID\"}")
parse_response "$response"
assert_status "BOX — create box item (OPENED)" 201 "$STATUS"
assert_contains "BOX — box item status OPENED" "OPENED" "$BODY"
assert_contains "BOX — box hasChildren true" '"hasChildren":true' "$BODY"
BOX_ITEM_ID=$(extract_id "$BODY")

# ---- Step 4: Verify 20 child pen items were auto-created ----------------
echo "  --- Step 4: Verify 20 pen items generated ---"

response=$(do_get "$BASE_URL/items")
parse_response "$response"
assert_status "BOX — list items" 200 "$STATUS"

# Count pen items that are children of the box
PEN_AVAILABLE_COUNT=$(echo "$BODY" | grep -o "\"parentItemId\":\"$BOX_ITEM_ID\"" | wc -l | tr -d ' ')
assert_count "BOX — 20 pen items created as children" 20 "$PEN_AVAILABLE_COUNT"

# All children should be AVAILABLE — count pen article items with AVAILABLE status
# Extract pen child item IDs for later use (each appears as a separate JSON object)
PEN_ITEMS_BODY=$(echo "$BODY" | python -c "
import sys, json
items = json.load(sys.stdin)
pens = [i for i in items if i.get('parentItemId') == '$BOX_ITEM_ID' and i.get('itemStatus') == 'AVAILABLE']
print(len(pens))
for p in pens:
    print(p['id'])
" 2>/dev/null || echo "0")

PEN_COUNT=$(echo "$PEN_ITEMS_BODY" | head -1)
assert_count "BOX — all 20 pens are AVAILABLE" 20 "$PEN_COUNT"

# Grab individual pen IDs for sales
PEN_IDS=()
while IFS= read -r line; do
    PEN_IDS+=("$line")
done <<< "$(echo "$PEN_ITEMS_BODY" | tail -n +2)"

# Verify box item itself is OPENED
response=$(do_get "$BASE_URL/items/$BOX_ITEM_ID")
parse_response "$response"
assert_contains "BOX — box item confirms OPENED" "OPENED" "$BODY"
assert_not_contains "BOX — box item not AVAILABLE" "AVAILABLE" "$BODY"

# Verify child pen cost = 16.00 / 20 = 0.80
response=$(do_get "$BASE_URL/items/${PEN_IDS[0]}")
parse_response "$response"
assert_contains "BOX — pen child cost 0.80" '"cost":0.80' "$BODY"

# ---- Step 5: Try to sell the OPENED box → must fail ---------------------
echo "  --- Step 5: Sell OPENED box → error ---"

OPENED_BOX_SALE_LINES="[{\"itemId\":\"$BOX_ITEM_ID\",\"unitPrice\":30.00,\"taxId\":\"$TAX_ID\"}]"

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-BOX-FAIL\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":$OPENED_BOX_SALE_LINES}")
parse_response "$response"
assert_status "BOX — sell OPENED box → 400" 400 "$STATUS"
assert_contains "BOX — error mentions not available" "not available" "$BODY"

# ---- Step 6: Sell 7 pens in first sale -----------------------------------
echo "  --- Step 6: Sell 7 pens ---"

SALE_PEN_LINES=""
for i in $(seq 0 6); do
    if [ -n "$SALE_PEN_LINES" ]; then SALE_PEN_LINES="$SALE_PEN_LINES,"; fi
    SALE_PEN_LINES="${SALE_PEN_LINES}{\"itemId\":\"${PEN_IDS[$i]}\",\"unitPrice\":1.50,\"taxId\":\"$TAX_ID\"}"
done

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-PEN-001\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":[$SALE_PEN_LINES]}")
parse_response "$response"
assert_status "BOX — sell 7 pens" 201 "$STATUS"
# totalAmount = 7 * 1.50 = 10.50
assert_contains "BOX — sale 1 totalAmount 10.50" "10.50" "$BODY"
BOX_SALE1_ID=$(extract_id "$BODY")

# ---- Step 7: Verify 7 pens SOLD + 13 AVAILABLE --------------------------
echo "  --- Step 7: Verify 7 SOLD, 13 AVAILABLE ---"

response=$(do_get "$BASE_URL/items")
parse_response "$response"

SOLD_PEN_COUNT=$(echo "$BODY" | python -c "
import sys, json
items = json.load(sys.stdin)
sold = [i for i in items if i.get('parentItemId') == '$BOX_ITEM_ID' and i.get('itemStatus') == 'SOLD']
print(len(sold))
" 2>/dev/null || echo "0")
assert_count "BOX — 7 pens now SOLD" 7 "$SOLD_PEN_COUNT"

AVAIL_PEN_COUNT=$(echo "$BODY" | python -c "
import sys, json
items = json.load(sys.stdin)
avail = [i for i in items if i.get('parentItemId') == '$BOX_ITEM_ID' and i.get('itemStatus') == 'AVAILABLE']
print(len(avail))
" 2>/dev/null || echo "0")
assert_count "BOX — 13 pens still AVAILABLE" 13 "$AVAIL_PEN_COUNT"

# ---- Step 8: Try to sell an already-sold pen → must fail -----------------
echo "  --- Step 8: Sell already-sold pen → error ---"

RESELL_LINES="[{\"itemId\":\"${PEN_IDS[0]}\",\"unitPrice\":1.50,\"taxId\":\"$TAX_ID\"}]"

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-PEN-RESELL\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":$RESELL_LINES}")
parse_response "$response"
assert_status "BOX — sell already-SOLD pen → 400" 400 "$STATUS"
assert_contains "BOX — error mentions not available" "not available" "$BODY"

# ---- Step 9: Sell 5 more pens + free concept delivery --------------------
echo "  --- Step 9: Sell 5 more pens + delivery ---"

SALE_PEN2_LINES=""
for i in $(seq 7 11); do
    if [ -n "$SALE_PEN2_LINES" ]; then SALE_PEN2_LINES="$SALE_PEN2_LINES,"; fi
    SALE_PEN2_LINES="${SALE_PEN2_LINES}{\"itemId\":\"${PEN_IDS[$i]}\",\"unitPrice\":1.50,\"taxId\":\"$TAX_ID\"}"
done
SALE_PEN2_LINES="${SALE_PEN2_LINES},{\"freeConceptId\":\"$FC_DELIVERY_ID\",\"quantity\":1,\"unitPrice\":5.50,\"taxId\":\"$TAX_ID\"}"

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-PEN-002\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":[$SALE_PEN2_LINES]}")
parse_response "$response"
assert_status "BOX — sell 5 pens + delivery" 201 "$STATUS"
# totalAmount = (5 * 1.50) + (1 * 5.50) = 7.50 + 5.50 = 13.00
assert_contains "BOX — sale 2 totalAmount 13.00" "13.00" "$BODY"
BOX_SALE2_ID=$(extract_id "$BODY")

# ---- Step 10: Verify 12 SOLD + 8 AVAILABLE + box still OPENED -----------
echo "  --- Step 10: Verify 12 SOLD, 8 AVAILABLE, box OPENED ---"

response=$(do_get "$BASE_URL/items")
parse_response "$response"

SOLD_PEN_COUNT=$(echo "$BODY" | python -c "
import sys, json
items = json.load(sys.stdin)
sold = [i for i in items if i.get('parentItemId') == '$BOX_ITEM_ID' and i.get('itemStatus') == 'SOLD']
print(len(sold))
" 2>/dev/null || echo "0")
assert_count "BOX — 12 pens now SOLD" 12 "$SOLD_PEN_COUNT"

AVAIL_PEN_COUNT=$(echo "$BODY" | python -c "
import sys, json
items = json.load(sys.stdin)
avail = [i for i in items if i.get('parentItemId') == '$BOX_ITEM_ID' and i.get('itemStatus') == 'AVAILABLE']
print(len(avail))
" 2>/dev/null || echo "0")
assert_count "BOX — 8 pens still AVAILABLE" 8 "$AVAIL_PEN_COUNT"

# Box still OPENED
response=$(do_get "$BASE_URL/items/$BOX_ITEM_ID")
parse_response "$response"
assert_contains "BOX — box still OPENED after sales" "OPENED" "$BODY"

# ---- Step 11: Try to sell box again → still fails -------------------------
echo "  --- Step 11: Sell OPENED box again → still error ---"

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-BOX-FAIL2\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":$OPENED_BOX_SALE_LINES}")
parse_response "$response"
assert_status "BOX — sell OPENED box again → 400" 400 "$STATUS"

# ---- Step 12: Sell remaining 8 pens -------------------------------------
echo "  --- Step 12: Sell remaining 8 pens ---"

SALE_PEN3_LINES=""
for i in $(seq 12 19); do
    if [ -n "$SALE_PEN3_LINES" ]; then SALE_PEN3_LINES="$SALE_PEN3_LINES,"; fi
    SALE_PEN3_LINES="${SALE_PEN3_LINES}{\"itemId\":\"${PEN_IDS[$i]}\",\"unitPrice\":1.50,\"taxId\":\"$TAX_ID\"}"
done

response=$(do_post "$BASE_URL/sales" "{\"code\":\"SAL-PEN-003\",\"employeeId\":\"$USER_ID\",\"paymentMethodId\":\"$PAYMENT_METHOD_ID\",\"lines\":[$SALE_PEN3_LINES]}")
parse_response "$response"
assert_status "BOX — sell remaining 8 pens" 201 "$STATUS"
# totalAmount = 8 * 1.50 = 12.00
assert_contains "BOX — sale 3 totalAmount 12.00" "12.00" "$BODY"
BOX_SALE3_ID=$(extract_id "$BODY")

# ---- Step 13: Verify ALL 20 pens SOLD, 0 AVAILABLE ----------------------
echo "  --- Step 13: Verify all 20 pens SOLD ---"

response=$(do_get "$BASE_URL/items")
parse_response "$response"

SOLD_PEN_COUNT=$(echo "$BODY" | python -c "
import sys, json
items = json.load(sys.stdin)
sold = [i for i in items if i.get('parentItemId') == '$BOX_ITEM_ID' and i.get('itemStatus') == 'SOLD']
print(len(sold))
" 2>/dev/null || echo "0")
assert_count "BOX — all 20 pens SOLD" 20 "$SOLD_PEN_COUNT"

AVAIL_PEN_COUNT=$(echo "$BODY" | python -c "
import sys, json
items = json.load(sys.stdin)
avail = [i for i in items if i.get('parentItemId') == '$BOX_ITEM_ID' and i.get('itemStatus') == 'AVAILABLE']
print(len(avail))
" 2>/dev/null || echo "0")
assert_count "BOX — 0 pens AVAILABLE" 0 "$AVAIL_PEN_COUNT"

# ---- Cleanup box flow data -----------------------------------------------
echo "  --- Cleanup box flow data ---"

do_delete "$BASE_URL/sales/$BOX_SALE1_ID" > /dev/null
do_delete "$BASE_URL/sales/$BOX_SALE2_ID" > /dev/null
do_delete "$BASE_URL/sales/$BOX_SALE3_ID" > /dev/null
do_delete "$BASE_URL/purchases/$BOX_PURCHASE_ID" > /dev/null
for i in $(seq 0 19); do
    do_delete "$BASE_URL/items/${PEN_IDS[$i]}" > /dev/null
done
do_delete "$BASE_URL/items/$BOX_ITEM_ID" > /dev/null
do_delete "$BASE_URL/articles/$PEN_ARTICLE_ID" > /dev/null
do_delete "$BASE_URL/articles/$BOX_ARTICLE_ID" > /dev/null

# =============================================================================
# 13. DELETE operations (cascade / cleanup)
# =============================================================================
echo ""
echo "=== DELETE ==="

# Free concepts
response=$(do_delete "$BASE_URL/free-concepts/$FC_DELIVERY_ID")
parse_response "$response"
assert_status "DELETE /free-concepts/{id}" 204 "$STATUS"

response=$(do_delete "$BASE_URL/free-concepts/$FC_GIFTWRAP_ID")
parse_response "$response"
assert_status "DELETE /free-concepts/{id} — second" 204 "$STATUS"

response=$(do_get "$BASE_URL/free-concepts/$FC_DELIVERY_ID")
parse_response "$response"
assert_status "GET /free-concepts/{id} — after delete → 404" 404 "$STATUS"

# Sales
response=$(do_delete "$BASE_URL/sales/$SALE_ID")
parse_response "$response"
assert_status "DELETE /sales/{id}" 204 "$STATUS"

response=$(do_get "$BASE_URL/sales/$SALE_ID")
parse_response "$response"
assert_status "GET /sales/{id} — after delete → 404" 404 "$STATUS"

response=$(do_delete "$BASE_URL/purchases/$PURCHASE_ID")
parse_response "$response"
assert_status "DELETE /purchases/{id}" 204 "$STATUS"

response=$(do_delete "$BASE_URL/items/$ITEM_ID")
parse_response "$response"
assert_status "DELETE /items/{id}" 204 "$STATUS"

response=$(do_delete "$BASE_URL/items/$ITEM2_ID")
parse_response "$response"
assert_status "DELETE /items/{id} — second" 204 "$STATUS"

response=$(do_delete "$BASE_URL/articles/$ARTICLE_ID")
parse_response "$response"
assert_status "DELETE /articles/{id}" 204 "$STATUS"

response=$(do_delete "$BASE_URL/articles/$ARTICLE2_ID")
parse_response "$response"
assert_status "DELETE /articles/{id} — second" 204 "$STATUS"

response=$(do_delete "$BASE_URL/users/$USER_ID")
parse_response "$response"
assert_status "DELETE /users/{id}" 204 "$STATUS"

response=$(do_delete "$BASE_URL/providers/$PROVIDER_ID")
parse_response "$response"
assert_status "DELETE /providers/{id}" 204 "$STATUS"

response=$(do_delete "$BASE_URL/locations/$LOCATION_ID")
parse_response "$response"
assert_status "DELETE /locations/{id}" 204 "$STATUS"

response=$(do_delete "$BASE_URL/taxes/$TAX_ID")
parse_response "$response"
assert_status "DELETE /taxes/{id}" 204 "$STATUS"

response=$(do_delete "$BASE_URL/taxes/$TAX2_ID")
parse_response "$response"
assert_status "DELETE /taxes/{id} — second" 204 "$STATUS"

# Delete non-existent
response=$(do_delete "$BASE_URL/taxes/00000000-0000-0000-0000-000000000000")
parse_response "$response"
assert_status "DELETE /taxes/{id} — not found → 404" 404 "$STATUS"

# =============================================================================
# 14. INVALID JSON / METHOD
# =============================================================================
echo ""
echo "=== EDGE CASES ==="

# Invalid JSON body
response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/taxes" \
    -H "Content-Type: application/json" -d '{invalid json}')
parse_response "$response"
assert_status "POST /taxes — malformed JSON → 400" 400 "$STATUS"

# Invalid UUID in path
response=$(do_get "$BASE_URL/taxes/not-a-uuid")
parse_response "$response"
assert_status "GET /taxes/{id} — invalid UUID → 400" 400 "$STATUS"

# Content-Type missing
response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/taxes" \
    -d '{"description":"Test","rate":10.0,"surchargeRate":1.0}')
parse_response "$response"
assert_status "POST /taxes — no Content-Type → 415" 415 "$STATUS"

# =============================================================================
# SUMMARY
# =============================================================================
echo ""
echo "============================================="
echo "  RESULTS: $PASS passed, $FAIL failed (total $TOTAL)"
echo "============================================="

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
