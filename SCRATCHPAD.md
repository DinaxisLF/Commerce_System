# Scratchpad

Running log of changes, newest first. One entry per branch: what changed, any API or DB impact, and test status.

## feature/transactions (2026-11-06)
- **Outcomes:** `POST /transactions/outcomes` with body `{category, amount, paymentMethod?, description}`.
  - Always saved as type OUT. `paymentMethod` defaults to CASH.
  - `SALE` is rejected, since sales come only from completing a sub-order.
  - New category `OTHER`, alongside SUPPLIES and WITHDRAWAL.
- **Listing:** `GET /transactions?from=YYYY-MM-DD&to=YYYY-MM-DD&type=IN|OUT`.
  - All parameters are optional. With no dates it returns today; with only `from` it returns that day. Both dates are inclusive.
  - Newest first. Sales include `subOrderId`.
- **Overselling protection:** stock is now changed with atomic DB updates (`ProductRepository.decrementStock` / `incrementStock`) instead of read-modify-save.
  - `decrementStock` only succeeds if `stock >= quantity`, so two cashiers can't both sell the last unit. The loser gets 400 "Product out of stock", and the whole order rolls back.
  - Restoring stock on cancel is also atomic, so it can't overwrite a concurrent sale.
  - A rare MySQL deadlock between two orders returns 409 "please retry" (new handler in `GlobalExceptionHandler`).
- **DB:** no schema changes.
- **Postman:** no Transactions collection yet.
- Tests: 21/21 pass. `OrderServiceTest` has 15 (3 new for atomic stock), and the new `TransactionServiceTest` has 6.

## feature/payments (2026-11-06)
- Paying a sub-order: marking it `COMPLETED` now requires a payment method (`CASH`, `CARD`, `TRANSFER`). CASH also requires `amountReceived` to be at least the subtotal, and the response includes `change`.
- Completing a sub-order saves a `MoneyTransaction` (IN / SALE) for the subtotal, linked to the sub-order. New `MoneyTransactionRepository`.
- An order can't be canceled once any sub-order is paid. Cancel the pending sub-orders and complete the order instead.
- **API change:** `PATCH /orders/{id}/suborders/{subOrderId}/status` now takes a JSON body `{status, paymentMethod, amountReceived}` instead of `?status=`.
- **DB:** new columns `sub_orders.payment_method` and `sub_orders.amount_received`, and in `money_transactions`, `payment_method` (non-null) and `sub_order_id`. Drop `money_transactions` if it already has rows.
- **Postman:** SubOrders collection updated. "Complete Sub-Order (Cash)", the new "Complete Sub-Order (Card)" and "Cancel Sub-Order" send the JSON body.
- Tests: 12/12 `OrderServiceTest` pass (5 new payment tests).

## fix/mysql-rollback (2026-11-06), merged in PR #2
- Reverted the Postgres migration back to MySQL (driver, `IDENTITY` ids, `Timestamp` dates).
- DB credentials come from a gitignored `.env` (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) via `spring.config.import`.

## feature/suborders-crud (2026-11-03), merged in PR #1
- Added `PATCH /orders/{id}/suborders/{subOrderId}/status` and `GET /orders/{id}` (full detail).
- Status rules: changes only from PENDING, and COMPLETED/CANCELED are final. An order completes only when all its active sub-orders are completed. Canceling restores stock.
- Missing records return 404.
- Item `notes`.
- Money is `BigDecimal`.
- Products are soft-deleted.
- Fixed a Lombok `@Data` recursion between entities.
- Tests: 7 `OrderServiceTest` pass.
