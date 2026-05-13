<?php
/**
 * Modèle pour OVH : à placer sur votre hébergement, ex. /api/desktop-payment-qr.php
 *
 * 1. Renommez ou copiez ce fichier vers le chemin réel utilisé par l'appli Java
 *    (voir payment.qr.api.path dans config.properties).
 * 2. Définissez la même clé que payment.qr.api.key côté appli (variable d'environnement
 *    ou constante ci-dessous — ne commitez JAMAIS la vraie clé dans un dépôt public).
 * 3. Phase test : renvoie un qrPayload fixe (URL) pour vérifier que le QR s'affiche.
 * 4. Ensuite : remplacez la logique par vos appels Orange Money / Wave (clés uniquement ici).
 */

header('Content-Type: application/json; charset=UTF-8');

// Même valeur que payment.qr.api.key dans %APPDATA%\GestionSalles\config.properties
// En production OVH : préférez getenv('DESKTOP_PAYMENT_KEY') dans la config du mutu.
define('DESKTOP_KEY_EXPECTED', 'AdminKayplay2025');

$provided = $_SERVER['HTTP_X_DESKTOP_KEY'] ?? '';
if (!hash_equals(DESKTOP_KEY_EXPECTED, $provided)) {
    http_response_code(401);
    echo json_encode(['error' => 'unauthorized']);
    exit;
}

$raw = file_get_contents('php://input');
$data = json_decode($raw, true);
if (!is_array($data)) {
    http_response_code(400);
    echo json_encode(['error' => 'invalid_json']);
    exit;
}

$method = $data['method'] ?? '';
$amount = isset($data['amount']) ? (int) $data['amount'] : 0;

if (!in_array($method, ['wave', 'orange_money'], true) || $amount <= 0) {
    http_response_code(400);
    echo json_encode(['error' => 'invalid_method_or_amount']);
    exit;
}

// --- Phase TEST : URL publique courte (ex. page d'accueil) encodée en QR pour valider le flux ---
// Remplacez par l'URL renvoyée par l'API Wave / Orange (paiement avec montant) quand vous l'aurez branchée.
$testPayload = 'https://www.kayplaygamingroom.com/';

echo json_encode([
    'qrPayload' => $testPayload,
    'debug_method' => $method,
    'debug_amount' => $amount,
], JSON_UNESCAPED_SLASHES);
