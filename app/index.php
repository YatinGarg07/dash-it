<?php
require 'vendor/autoload.php';
$stripe = new \Stripe\StripeClient('sk_test_51OmVbDSCwCNj3Fzj2Z0yfJNYC00XTOXMajmgIQuYZCZDWHxKIvUdQRpdTfyVUA0tazUclfsoTXULxXDbxRv63I4t001XQNAOX0');

// Use an existing Customer ID if this is a returning customer.
$customer = $stripe->customers->create(
[
    'name' => 'Yatin',
    'address' => [
    'line1' => 'Demo address',
    'postal_code' => '738933',
    'city' => 'Kerala',
    'state' => 'NY',
    'country' => 'IN'
    ]
]);
$ephemeralKey = $stripe->ephemeralKeys->create([
  'customer' => $customer->id,
], [
  'stripe_version' => '2023-10-16',
]);
$paymentIntent = $stripe->paymentIntents->create([
  'amount' => 176 * 100,
  'currency' => 'inr',
  'description' => 'Ride Booking',
  'customer' => $customer->id,
  'automatic_payment_methods' => [
    'enabled' => 'true',
  ],
]);

echo json_encode(
  [
    'paymentIntent' => $paymentIntent->client_secret,
    'ephemeralKey' => $ephemeralKey->secret,
    'customer' => $customer->id,
    'publishableKey' => 'pk_test_51OmVbDSCwCNj3FzjHeaPGQk77TKsEPZivGcogIRr9dIioEJcCx18QJsN4eImOI6UQch2UzYb2K4UA14UhDOxFPsz001JWPzTnm'
  ]
);
http_response_code(200);