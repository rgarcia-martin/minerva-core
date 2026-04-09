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
# 9. DELETE operations (cascade / cleanup)
# =============================================================================
echo ""
echo "=== DELETE ==="

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
# 10. INVALID JSON / METHOD
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
