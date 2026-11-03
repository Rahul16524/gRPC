# order-service

## REST API (port 8009)

Send JSON requests with `Content-Type: application/json`. The examples below use
`curl`; use `curl.exe` instead of `curl` in Windows PowerShell if it maps to
the PowerShell alias.

### Create an order

`POST http://localhost:8009/api/order` (also accepts `/api/order/` and
`/api/order/create`)

```json
{
  "customerId": 12,
  "items": [
    {
      "productId": 999,
      "quantity": 9,
      "unitPrice": 8
    }
  ]
}
```

```powershell
curl.exe -i -X POST http://localhost:8009/api/order/ `
  -H "Content-Type: application/json" `
  -d '{"customerId":12,"items":[{"productId":999,"quantity":9,"unitPrice":8}]}'
```

Success returns `201 Created` with an order ID and calculated total (`72.0` in
this example). The API calculates totals; any `totalAmount` supplied by the
caller is ignored.

Validation cases return `400 Bad Request`:

- Missing/non-positive `customerId`.
- Missing or empty `items`.
- Missing/non-positive `productId`, missing/non-positive `quantity`, or
  missing/negative `unitPrice`.
- An item or order total too large to represent as a finite number.
- Malformed JSON.

Retrieve an order and its items with `GET
http://localhost:8009/api/order/{orderId}`. An unknown order returns `404 Not
Found`.

### Create one standalone order item

`POST http://localhost:8009/api/order-item`

```powershell
curl.exe -i -X POST http://localhost:8009/api/order-item `
  -H "Content-Type: application/json" `
  -d '{"productId":999,"quantity":9,"unitPrice":8}'
```

Success returns `201 Created` with the calculated `totalAmount` (`72.0`).
Invalid fields, malformed JSON, or an overflowing total return `400`.

### Create standalone order items in bulk

`POST http://localhost:8009/api/order-item/list`

```powershell
curl.exe -i -X POST http://localhost:8009/api/order-item/list `
  -H "Content-Type: application/json" `
  -d '[{"productId":999,"quantity":9,"unitPrice":8},{"productId":1000,"quantity":2,"unitPrice":3.5}]'
```

Success returns `201 Created` with a JSON array of calculated items. An empty
array or any invalid item returns `400 Bad Request`.

### Create items attached to an order

Use `POST http://localhost:8009/api/order-item/create/order/{orderId}` for one
item, or `POST
http://localhost:8009/api/order-item/create-list/order/{orderId}` for a list.
Both accept the same item JSON shown above. The specified order must exist.

### Pay for an order

`POST http://localhost:8009/api/order/payment/{orderId}`

No request body is required. Create an order first and replace `{orderId}` with
its returned `id`:

```powershell
curl.exe -i -X POST http://localhost:8009/api/order/payment/1
```

Success returns `200 OK` with a transaction ID. An unknown order returns
`404 Not Found`; an unavailable payment service returns `503 Service
Unavailable`; a rejected payment returns `502 Bad Gateway`.

Unsupported HTTP methods return `405 Method Not Allowed`. Standalone order
items are not attached to an order and cannot be paid; use the create-order
endpoint for items that need payment.

## Testing

Run order endpoint tests from the repository root:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\mvnw.cmd -pl order-service test
```

The tests cover successful order creation (with and without a trailing slash),
payment success, unknown orders, payment-service unavailability, standalone
single/bulk item creation, validation failures, and unsupported methods.