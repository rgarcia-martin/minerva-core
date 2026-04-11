#!/usr/bin/env bash
# =============================================================================
# Minerva Core — Database Seed Script
# Populates the running instance with sample data via REST API.
# Creates >= 10 records per aggregate, plus a demo user (username/email: demo,
# password: demo).
#
# Usage: bash seed-data.sh
# Requires: a running server at http://localhost:8080 and python3.
# =============================================================================

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080/api/v1}"

if command -v python3 >/dev/null 2>&1; then
    PYTHON_BIN=python3
elif command -v python >/dev/null 2>&1; then
    PYTHON_BIN=python
else
    echo "python3 is required to parse JSON responses" >&2
    exit 1
fi

CREATED=0
FAILED=0

# --- HTTP helpers ------------------------------------------------------------

do_post() {
    local url="$1"
    local data="$2"
    curl -s -w "\n%{http_code}" -X POST "$url" \
        -H "Content-Type: application/json" -d "$data"
}

do_get() {
    curl -s -w "\n%{http_code}" -X GET "$1" -H "Accept: application/json"
}

parse_response() {
    local response="$1"
    BODY=$(echo "$response" | sed '$d')
    STATUS=$(echo "$response" | tail -1)
}

extract_id() {
    BODY_IN="$1" "$PYTHON_BIN" - <<'PY'
import json, os, sys
text = os.environ.get("BODY_IN", "")
try:
    data = json.loads(text)
    if isinstance(data, dict) and "id" in data:
        print(data["id"])
except Exception:
    pass
PY
}

extract_ids_from_list() {
    BODY_IN="$1" "$PYTHON_BIN" - <<'PY'
import json, os
text = os.environ.get("BODY_IN", "[]") or "[]"
try:
    data = json.loads(text)
    for entry in data:
        if isinstance(entry, dict) and "id" in entry:
            print(entry["id"])
except Exception:
    pass
PY
}

extract_available_item_ids() {
    BODY_IN="$1" "$PYTHON_BIN" - <<'PY'
import json, os
text = os.environ.get("BODY_IN", "[]") or "[]"
try:
    data = json.loads(text)
    for entry in data:
        if isinstance(entry, dict) and entry.get("status") == "AVAILABLE":
            print(entry["id"])
except Exception:
    pass
PY
}

post_or_die() {
    local label="$1"
    local url="$2"
    local data="$3"
    response=$(do_post "$url" "$data")
    parse_response "$response"
    if [ "$STATUS" = "201" ]; then
        CREATED=$((CREATED + 1))
        LAST_ID=$(extract_id "$BODY")
        echo "  [OK]   $label  ($LAST_ID)"
    else
        FAILED=$((FAILED + 1))
        LAST_ID=""
        echo "  [FAIL] $label  HTTP $STATUS  body=$BODY"
    fi
}

# --- Sanity check ------------------------------------------------------------

echo "Seeding Minerva Core via $BASE_URL"
echo ""
if ! curl -s -f -o /dev/null "$BASE_URL/taxes"; then
    echo "Cannot reach $BASE_URL/taxes — is the server running?" >&2
    exit 1
fi

# =============================================================================
# 1. TAXES
# =============================================================================
echo "=== TAXES ==="
TAX_IDS=()
TAX_ROWS=(
    'IVA General|21.0|5.2'
    'IVA Reducido|10.0|1.4'
    'IVA Superreducido|4.0|0.5'
    'IVA Exento|0.0|0.0'
    'IVA Importacion|21.0|7.5'
    'IVA Lujo|27.0|6.0'
    'IVA Servicios|21.0|5.2'
    'Tasa Municipal|3.5|0.0'
    'Tasa Provincial|2.0|0.0'
    'Recargo Equivalencia|5.2|0.0'
)
for row in "${TAX_ROWS[@]}"; do
    IFS='|' read -r desc rate sur <<<"$row"
    payload=$(printf '{"description":"%s","rate":%s,"surchargeRate":%s}' "$desc" "$rate" "$sur")
    post_or_die "tax: $desc" "$BASE_URL/taxes" "$payload"
    [ -n "$LAST_ID" ] && TAX_IDS+=("$LAST_ID")
done

if [ ${#TAX_IDS[@]} -eq 0 ]; then
    echo "No taxes were created — aborting." >&2
    exit 1
fi
DEFAULT_TAX_ID="${TAX_IDS[0]}"

# =============================================================================
# 2. LOCATIONS
# =============================================================================
echo ""
echo "=== LOCATIONS ==="
LOCATION_IDS=()
LOCATION_ROWS=(
    'Almacen Central|Bodega principal'
    'Almacen Norte|Sucursal norte'
    'Almacen Sur|Sucursal sur'
    'Tienda Centro|Punto de venta centro'
    'Tienda Mall|Punto de venta mall'
    'Deposito Externo|Almacenamiento tercerizado'
    'Showroom|Exhibicion de productos'
    'Bodega Refrigerada|Productos perecederos'
    'Oficina Administrativa|Suministros internos'
    'Zona de Devoluciones|Items en revision'
)
for row in "${LOCATION_ROWS[@]}"; do
    IFS='|' read -r name desc <<<"$row"
    payload=$(printf '{"name":"%s","description":"%s"}' "$name" "$desc")
    post_or_die "location: $name" "$BASE_URL/locations" "$payload"
    [ -n "$LAST_ID" ] && LOCATION_IDS+=("$LAST_ID")
done
DEFAULT_LOCATION_ID="${LOCATION_IDS[0]}"

# =============================================================================
# 3. PROVIDERS
# =============================================================================
echo ""
echo "=== PROVIDERS ==="
PROVIDER_IDS=()
PROVIDER_ROWS=(
    'Distribuidora Andina|B10000001|Av Industrial 100|+34 911 000 001|ventas@andina.es|false'
    'Importaciones del Sur|B10000002|Calle Puerto 5|+34 911 000 002|info@sur.es|true'
    'Mayorista Central|B10000003|Polig Norte 22|+34 911 000 003|contacto@central.es|false'
    'Tecnologia Global|B10000004|Av Innovacion 7|+34 911 000 004|sales@tecglobal.es|true'
    'Papelera Nacional|B10000005|Calle Bosque 14|+34 911 000 005|pedidos@papelera.es|false'
    'Suministros Express|B10000006|Av Rapida 33|+34 911 000 006|express@suministros.es|false'
    'Quimicos del Norte|B10000007|Carretera 4 km 12|+34 911 000 007|info@quimnorte.es|true'
    'Textiles Mediterraneo|B10000008|Polig Mar 8|+34 911 000 008|ventas@txmed.es|false'
    'Alimentos Premium|B10000009|Av Gourmet 19|+34 911 000 009|hola@premium.es|true'
    'Ferreteria Mayor|B10000010|Calle Hierro 27|+34 911 000 010|info@ferremayor.es|false'
)
for row in "${PROVIDER_ROWS[@]}"; do
    IFS='|' read -r name tid addr phone email surcharge <<<"$row"
    payload=$(printf '{"businessName":"%s","taxIdentifier":"%s","address":"%s","phone":"%s","email":"%s","appliesSurcharge":%s}' \
        "$name" "$tid" "$addr" "$phone" "$email" "$surcharge")
    post_or_die "provider: $name" "$BASE_URL/providers" "$payload"
    [ -n "$LAST_ID" ] && PROVIDER_IDS+=("$LAST_ID")
done
DEFAULT_PROVIDER_ID="${PROVIDER_IDS[0]}"

# =============================================================================
# 4. ARTICLES
# =============================================================================
echo ""
echo "=== ARTICLES ==="
ARTICLE_IDS=()
ARTICLE_ROWS=(
    'Boligrafo Azul|ART-001|7700000000001|Boligrafo de tinta azul|0.50|1.20'
    'Cuaderno A4|ART-002|7700000000002|Cuaderno A4 100 hojas|2.50|4.90'
    'Lapiz HB|ART-003|7700000000003|Lapiz grafito HB|0.30|0.80'
    'Goma de Borrar|ART-004|7700000000004|Goma blanca|0.20|0.60'
    'Resma A4|ART-005|7700000000005|Resma 500 hojas A4|3.80|6.50'
    'Carpeta Anillada|ART-006|7700000000006|Carpeta de 4 anillos|3.20|6.90'
    'Tijeras Escolares|ART-007|7700000000007|Tijeras 13cm|1.10|2.80'
    'Marcador Negro|ART-008|7700000000008|Marcador permanente|0.90|2.20'
    'Calculadora Basica|ART-009|7700000000009|Calculadora 8 digitos|4.50|9.90'
    'Mochila Escolar|ART-010|7700000000010|Mochila reforzada|12.00|24.90'
)
for row in "${ARTICLE_ROWS[@]}"; do
    IFS='|' read -r name code barcode desc base retail <<<"$row"
    payload=$(printf '{"name":"%s","code":"%s","barcode":"%s","description":"%s","taxId":"%s","basePrice":%s,"retailPrice":%s,"canHaveChildren":false}' \
        "$name" "$code" "$barcode" "$desc" "$DEFAULT_TAX_ID" "$base" "$retail")
    post_or_die "article: $name" "$BASE_URL/articles" "$payload"
    [ -n "$LAST_ID" ] && ARTICLE_IDS+=("$LAST_ID")
done

# =============================================================================
# 4b. PACKAGED ARTICLES (parent / child pairs for box-opening flow)
# Each pair = a child "unit" article + a parent "box" article that references
# it via canHaveChildren=true + numberOfChildren + childArticleId.
# =============================================================================
echo ""
echo "=== PACKAGED ARTICLES (parent/child) ==="
CHILD_ARTICLE_IDS=()
PARENT_ARTICLE_IDS=()
PARENT_NUM_CHILDREN=()

# Format: child_name|child_code|child_barcode|child_base|child_retail||parent_name|parent_code|parent_barcode|parent_base|parent_retail|num_children
PACKAGED_PAIRS=(
    'Boligrafo Unidad|ART-PEN-UNIT|8400000010001|0.80|1.50||Caja de Boligrafos 20u|ART-BOX-PEN-20|8400000010002|16.00|30.00|20'
    'Marcador Unidad|ART-MRK-UNIT|8400000020001|0.90|2.00||Caja de Marcadores 12u|ART-BOX-MRK-12|8400000020002|10.80|24.00|12'
    'Pila AA Unidad|ART-BAT-UNIT|8400000030001|0.50|1.20||Pack de Pilas AA 10u|ART-PACK-BAT-10|8400000030002|5.00|12.00|10'
)

for pair in "${PACKAGED_PAIRS[@]}"; do
    IFS='|' read -r c_name c_code c_barcode c_base c_retail _ p_name p_code p_barcode p_base p_retail p_num <<<"$pair"

    # 1) Create the child (unit) article first — must exist before the parent references it.
    payload=$(printf '{"name":"%s","code":"%s","barcode":"%s","description":"Unidad suelta","taxId":"%s","basePrice":%s,"retailPrice":%s,"canHaveChildren":false}' \
        "$c_name" "$c_code" "$c_barcode" "$DEFAULT_TAX_ID" "$c_base" "$c_retail")
    post_or_die "child article: $c_name" "$BASE_URL/articles" "$payload"
    if [ -z "$LAST_ID" ]; then
        continue
    fi
    child_id="$LAST_ID"
    CHILD_ARTICLE_IDS+=("$child_id")

    # 2) Create the parent (box) article referencing the child.
    payload=$(printf '{"name":"%s","code":"%s","barcode":"%s","description":"Caja contenedora","taxId":"%s","basePrice":%s,"retailPrice":%s,"canHaveChildren":true,"numberOfChildren":%s,"childArticleId":"%s"}' \
        "$p_name" "$p_code" "$p_barcode" "$DEFAULT_TAX_ID" "$p_base" "$p_retail" "$p_num" "$child_id")
    post_or_die "parent article: $p_name (x$p_num)" "$BASE_URL/articles" "$payload"
    if [ -n "$LAST_ID" ]; then
        PARENT_ARTICLE_IDS+=("$LAST_ID")
        PARENT_NUM_CHILDREN+=("$p_num")
    fi
done

# =============================================================================
# 5. USERS  (incl. demo/demo)
# =============================================================================
echo ""
echo "=== USERS ==="
USER_IDS=()
USER_ROWS=(
    'demo|Demo|demo|demo|Calle Demo 1|["READ","CREATE","EDIT","DELETE"]'
    'Ana|Garcia|ana.garcia@minerva.test|Passw0rd!|Av Mayor 10|["READ","CREATE"]'
    'Bruno|Lopez|bruno.lopez@minerva.test|Passw0rd!|Av Mayor 11|["READ","EDIT"]'
    'Carla|Martinez|carla.martinez@minerva.test|Passw0rd!|Av Mayor 12|["READ"]'
    'Diego|Sanchez|diego.sanchez@minerva.test|Passw0rd!|Av Mayor 13|["READ","CREATE","EDIT"]'
    'Elena|Romero|elena.romero@minerva.test|Passw0rd!|Av Mayor 14|["READ","DELETE"]'
    'Fabio|Torres|fabio.torres@minerva.test|Passw0rd!|Av Mayor 15|["READ","CREATE"]'
    'Gloria|Vega|gloria.vega@minerva.test|Passw0rd!|Av Mayor 16|["READ","EDIT","DELETE"]'
    'Hugo|Navarro|hugo.navarro@minerva.test|Passw0rd!|Av Mayor 17|["READ"]'
    'Ines|Castro|ines.castro@minerva.test|Passw0rd!|Av Mayor 18|["READ","CREATE","EDIT","DELETE"]'
)
for row in "${USER_ROWS[@]}"; do
    IFS='|' read -r name lastname email password addr roles <<<"$row"
    payload=$(printf '{"name":"%s","lastName":"%s","email":"%s","password":"%s","address":"%s","roles":%s}' \
        "$name" "$lastname" "$email" "$password" "$addr" "$roles")
    post_or_die "user: $email" "$BASE_URL/users" "$payload"
    [ -n "$LAST_ID" ] && USER_IDS+=("$LAST_ID")
done
DEMO_USER_ID="${USER_IDS[0]}"

# =============================================================================
# 6. PAYMENT METHODS
# =============================================================================
echo ""
echo "=== PAYMENT METHODS ==="
PAYMENT_METHOD_IDS=()
PAYMENT_ROWS=(
    'Efectivo Caja 1|CASH|'
    'Efectivo Caja 2|CASH|'
    'Visa Credito|CARD|terminal=POS-001'
    'Visa Debito|CARD|terminal=POS-002'
    'Mastercard Credito|CARD|terminal=POS-003'
    'Mastercard Debito|CARD|terminal=POS-004'
    'American Express|CARD|terminal=POS-005'
    'PayPal|GATEWAY|merchant=mrc_demo&secret=hidden'
    'Stripe|GATEWAY|pk=pk_demo&sk=sk_demo'
    'Mercado Pago|GATEWAY|client_id=demo&client_secret=demo'
)
for row in "${PAYMENT_ROWS[@]}"; do
    IFS='|' read -r name type config <<<"$row"
    if [ -z "$config" ]; then
        payload=$(printf '{"name":"%s","type":"%s"}' "$name" "$type")
    else
        payload=$(printf '{"name":"%s","type":"%s","configuration":"%s"}' "$name" "$type" "$config")
    fi
    post_or_die "payment: $name" "$BASE_URL/payment-methods" "$payload"
    [ -n "$LAST_ID" ] && PAYMENT_METHOD_IDS+=("$LAST_ID")
done
DEFAULT_PAYMENT_ID="${PAYMENT_METHOD_IDS[0]}"

# =============================================================================
# 7. FREE CONCEPTS
# =============================================================================
echo ""
echo "=== FREE CONCEPTS ==="
FREE_CONCEPT_IDS=()
FREE_ROWS=(
    'Envio Estandar|FC-001|3.50|Envio a domicilio'
    'Envio Express|FC-002|7.90|Envio en 24h'
    'Envoltorio Regalo|FC-003|1.50|Papel y lazo'
    'Fotocopia BN|FC-004|0.10|Fotocopia blanco y negro'
    'Fotocopia Color|FC-005|0.30|Fotocopia a color'
    'Impresion A4|FC-006|0.20|Impresion blanco y negro'
    'Impresion Color A4|FC-007|0.50|Impresion a color'
    'Plastificado A4|FC-008|1.20|Plastificado A4'
    'Encuadernado|FC-009|2.80|Encuadernado simple'
    'Recargo Manipulacion|FC-010|0.80|Manipulacion de pedido'
)
for row in "${FREE_ROWS[@]}"; do
    IFS='|' read -r name barcode price desc <<<"$row"
    payload=$(printf '{"name":"%s","barcode":"%s","price":%s,"taxId":"%s","description":"%s"}' \
        "$name" "$barcode" "$price" "$DEFAULT_TAX_ID" "$desc")
    post_or_die "free-concept: $name" "$BASE_URL/free-concepts" "$payload"
    [ -n "$LAST_ID" ] && FREE_CONCEPT_IDS+=("$LAST_ID")
done
DEFAULT_FREE_CONCEPT_ID="${FREE_CONCEPT_IDS[0]}"

# =============================================================================
# 8. PURCHASES (auto-generates inventory items)
# =============================================================================
echo ""
echo "=== PURCHASES ==="
PURCHASE_IDS=()
if [ ${#ARTICLE_IDS[@]} -lt 1 ] || [ ${#PROVIDER_IDS[@]} -lt 1 ] || [ ${#LOCATION_IDS[@]} -lt 1 ]; then
    echo "  [SKIP] missing prerequisites for purchases"
else
    for i in $(seq 0 9); do
        article_id="${ARTICLE_IDS[$((i % ${#ARTICLE_IDS[@]}))]}"
        provider_id="${PROVIDER_IDS[$((i % ${#PROVIDER_IDS[@]}))]}"
        location_id="${LOCATION_IDS[$((i % ${#LOCATION_IDS[@]}))]}"
        code=$(printf 'PUR-2026-%03d' "$((i + 1))")
        provider_code=$(printf 'PROV-INV-%03d' "$((i + 1))")
        quantity=$((3 + i))
        payload=$(printf '{"createdOn":"2026-04-%02dT10:00:00","state":"NEW","code":"%s","providerCode":"%s","providerId":"%s","locationId":"%s","deposit":false,"lines":[{"articleId":"%s","quantity":%d,"buyPrice":%s,"profitMargin":0.30,"taxId":"%s","itemStatus":"AVAILABLE","hasChildren":false}]}' \
            "$((i + 1))" "$code" "$provider_code" "$provider_id" "$location_id" \
            "$article_id" "$quantity" "$((i + 1)).50" "$DEFAULT_TAX_ID")
        post_or_die "purchase: $code (qty=$quantity)" "$BASE_URL/purchases" "$payload"
        [ -n "$LAST_ID" ] && PURCHASE_IDS+=("$LAST_ID")
    done
fi

# =============================================================================
# 8b. BOX-OPENING PURCHASES
# Each purchase buys 1 parent box in OPENED state with hasChildren=true.
# PurchaseService auto-generates 1 parent item + N child items at
# parent.cost / numberOfChildren each (HALF_UP, 2dp).
# =============================================================================
echo ""
echo "=== BOX-OPENING PURCHASES ==="
BOX_PURCHASE_IDS=()
if [ ${#PARENT_ARTICLE_IDS[@]} -eq 0 ] || [ ${#PROVIDER_IDS[@]} -eq 0 ] || [ ${#LOCATION_IDS[@]} -eq 0 ]; then
    echo "  [SKIP] missing prerequisites for box-opening purchases"
else
    BOX_BUY_PRICES=('16.00' '10.80' '5.00')
    for i in "${!PARENT_ARTICLE_IDS[@]}"; do
        parent_id="${PARENT_ARTICLE_IDS[$i]}"
        num_children="${PARENT_NUM_CHILDREN[$i]}"
        buy_price="${BOX_BUY_PRICES[$((i % ${#BOX_BUY_PRICES[@]}))]}"
        provider_id="${PROVIDER_IDS[$((i % ${#PROVIDER_IDS[@]}))]}"
        location_id="${LOCATION_IDS[$((i % ${#LOCATION_IDS[@]}))]}"
        code=$(printf 'PUR-BOX-2026-%03d' "$((i + 1))")
        provider_code=$(printf 'PROV-BOX-%03d' "$((i + 1))")
        payload=$(printf '{"createdOn":"2026-04-15T10:00:00","state":"NEW","code":"%s","providerCode":"%s","providerId":"%s","locationId":"%s","deposit":false,"lines":[{"articleId":"%s","quantity":1,"buyPrice":%s,"profitMargin":0.8750,"taxId":"%s","itemStatus":"OPENED","hasChildren":true}]}' \
            "$code" "$provider_code" "$provider_id" "$location_id" \
            "$parent_id" "$buy_price" "$DEFAULT_TAX_ID")
        post_or_die "box purchase: $code (1 box -> $num_children units)" "$BASE_URL/purchases" "$payload"
        [ -n "$LAST_ID" ] && BOX_PURCHASE_IDS+=("$LAST_ID")
    done
fi

# =============================================================================
# 9. SALES (mix of item lines and free-concept lines)
# =============================================================================
echo ""
echo "=== SALES ==="
SALE_IDS=()

# Fetch the available items generated by the purchases above.
response=$(do_get "$BASE_URL/items")
parse_response "$response"
if [ "$STATUS" != "200" ]; then
    echo "  [SKIP] could not fetch items list (HTTP $STATUS)"
    AVAILABLE_ITEM_IDS=()
else
    mapfile -t AVAILABLE_ITEM_IDS < <(extract_available_item_ids "$BODY")
    echo "  found ${#AVAILABLE_ITEM_IDS[@]} AVAILABLE items"
fi

if [ ${#USER_IDS[@]} -lt 1 ] || [ ${#PAYMENT_METHOD_IDS[@]} -lt 1 ]; then
    echo "  [SKIP] missing prerequisites for sales"
else
    for i in $(seq 0 9); do
        employee_id="${USER_IDS[$((i % ${#USER_IDS[@]}))]}"
        payment_id="${PAYMENT_METHOD_IDS[$((i % ${#PAYMENT_METHOD_IDS[@]}))]}"
        code=$(printf 'SAL-2026-%03d' "$((i + 1))")
        # Alternate: even i -> item line (if available), odd i -> free concept line
        if [ $((i % 2)) -eq 0 ] && [ ${#AVAILABLE_ITEM_IDS[@]} -gt 0 ]; then
            item_id="${AVAILABLE_ITEM_IDS[0]}"
            AVAILABLE_ITEM_IDS=("${AVAILABLE_ITEM_IDS[@]:1}")
            line=$(printf '{"itemId":"%s","unitPrice":%s,"taxId":"%s"}' \
                "$item_id" "$((i + 2)).90" "$DEFAULT_TAX_ID")
        else
            free_id="${FREE_CONCEPT_IDS[$((i % ${#FREE_CONCEPT_IDS[@]}))]}"
            line=$(printf '{"freeConceptId":"%s","quantity":%d,"unitPrice":%s,"taxId":"%s"}' \
                "$free_id" "$((1 + (i % 3)))" "$((i + 1)).50" "$DEFAULT_TAX_ID")
        fi
        payload=$(printf '{"code":"%s","employeeId":"%s","paymentMethodId":"%s","lines":[%s]}' \
            "$code" "$employee_id" "$payment_id" "$line")
        post_or_die "sale: $code" "$BASE_URL/sales" "$payload"
        [ -n "$LAST_ID" ] && SALE_IDS+=("$LAST_ID")
    done
fi

# =============================================================================
# Summary
# =============================================================================
echo ""
echo "============================================================"
echo "Seed complete:  $CREATED created,  $FAILED failed"
TOTAL_ARTICLES=$(( ${#ARTICLE_IDS[@]} + ${#CHILD_ARTICLE_IDS[@]} + ${#PARENT_ARTICLE_IDS[@]} ))
TOTAL_PURCHASES=$(( ${#PURCHASE_IDS[@]} + ${#BOX_PURCHASE_IDS[@]} ))
echo "  taxes             ${#TAX_IDS[@]}"
echo "  locations         ${#LOCATION_IDS[@]}"
echo "  providers         ${#PROVIDER_IDS[@]}"
echo "  articles          $TOTAL_ARTICLES   (${#ARTICLE_IDS[@]} simple + ${#CHILD_ARTICLE_IDS[@]} child + ${#PARENT_ARTICLE_IDS[@]} parent/box)"
echo "  users             ${#USER_IDS[@]}   (demo user id: $DEMO_USER_ID)"
echo "  payment methods   ${#PAYMENT_METHOD_IDS[@]}"
echo "  free concepts     ${#FREE_CONCEPT_IDS[@]}"
echo "  purchases         $TOTAL_PURCHASES   (${#PURCHASE_IDS[@]} regular + ${#BOX_PURCHASE_IDS[@]} box-opening)"
echo "  sales             ${#SALE_IDS[@]}"
echo "============================================================"
echo "Demo credentials -> email: demo   password: demo"

if [ "$FAILED" -gt 0 ]; then
    exit 1
fi
