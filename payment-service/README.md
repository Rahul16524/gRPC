# payment-service

## gRPC API

The service listens for gRPC on port `9090`; it does not expose a REST endpoint.
The RPC is `payment.PaymentService/ProcessPayment`, defined in
`configuration-service/src/main/resources/proto/user_service.proto`.

Example request:

```json
{
  "orderId": "1",
  "amount": 72.0,
  "currency": "USD"
}
```

For a local manual call, install `grpcurl` and run this from the repository
root:

```powershell
grpcurl -plaintext `
  -import-path configuration-service/src/main/resources/proto `
  -proto user_service.proto `
  -d '{"orderId":"1","amount":72.0,"currency":"USD"}' `
  localhost:9090 payment.PaymentService/ProcessPayment
```

A valid request returns `success: true` and a transaction ID. An empty order
ID/currency or a non-positive/non-finite amount returns `success: false` with
an error message.

Start this service before requesting payment through order-service. Order
creation and item endpoints work independently; payment requests return
`503 Service Unavailable` if this service cannot be reached.