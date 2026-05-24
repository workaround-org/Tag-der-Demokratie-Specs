# Fundraising Tool – Spezifikation

## Überblick

Ein leichtgewichtiges Spendensammler-Tool, ähnlich dem Wikipedia-Spendenbannermodell. Projektverantwortliche legen eine Kampagne mit Zielbeträgen an und verteilen diese über QR-Codes und Links. Spender können über PayPal, Wero oder Kreditkarte (Stripe) spenden und optional eine Nachricht hinterlassen. Der Fortschritt wird in Echtzeit angezeigt. Mehrere Kampagnen können parallel aktiv sein.

---

## Technologie-Stack

- **Backend:** [Quarkus](https://quarkus.io/) mit [Renarde](https://quarkiverse.github.io/quarkiverse-docs/quarkus-renarde/dev/) (MVC-Framework) und [Qute](https://quarkus.io/guides/qute) (Templating)
- **E-Mail:** Quarkus Mailer (`io.quarkus:quarkus-mailer`) via SMTP mit Qute-Templates für alle ausgehenden Mails
- **Datenbank:** relationale DB via Hibernate ORM with Panache (PostgreSQL empfohlen)
- **QR-Code-Generierung:** serverseitig, z. B. via ZXing

---

## Kernkonzepte

### Projekt (Campaign)

Ein Projekt repräsentiert eine einzelne Spendensammlung.

| Feld | Typ | Beschreibung |
|---|---|---|
| `id` | UUID | Eindeutige ID |
| `slug` | string | URL-freundlicher Kurzname (z. B. `tag-der-demokratie-2026`) |
| `title` | string | Öffentlicher Titel der Kampagne |
| `description` | string (Markdown) | Beschreibung, warum gesammelt wird |
| `goal_amount` | integer (Cent) | Zielbetrag in Cent |
| `currency` | string | ISO-4217-Code, z. B. `EUR` |
| `deadline` | datetime \| null | Optionaler Endtermin |
| `created_at` | datetime | Erstellungszeitpunkt |
| `status` | enum | `active` \| `paused` \| `completed` \| `archived` |
| `cover_image_url` | string \| null | Optionales Headerbild |

### Admin-Benutzer (AdminUser)

Lokale Benutzerverwaltung, verwaltet im Adminbereich.

| Feld | Typ | Beschreibung |
|---|---|---|
| `id` | UUID | Eindeutige ID |
| `username` | string | Eindeutiger Benutzername |
| `password_hash` | string | Bcrypt-Hash des Passworts |
| `display_name` | string | Anzeigename |
| `created_at` | datetime | Erstellungszeitpunkt |
| `last_login_at` | datetime \| null | Letzter Login |

Login via Renarde-Formular-Authentifizierung (`SecurityIdentity`). Passwort-Reset durch einen anderen Admin möglich. Mindestens ein Admin-Account muss immer vorhanden sein (Löschsperre).

### Organisationsdaten (OrganizationSettings)

Einmalige systemweite Konfiguration, im Adminbereich pflegbar.

| Feld | Typ | Beschreibung |
|---|---|---|
| `org_name` | string | Name der Organisation |
| `org_street` | string | Straße und Hausnummer |
| `org_zip` | string | Postleitzahl |
| `org_city` | string | Stadt |
| `org_tax_id` | string | Steuernummer oder Freistellungsbescheid-Nummer |
| `org_issuing_authority` | string | Zuständiges Finanzamt |
| `org_exemption_date` | date | Datum des letzten Freistellungsbescheids |
| `org_purpose` | string | Satzungsmäßiger Zweck (für Quittungstext) |
| `smtp_host` | string | SMTP-Server |
| `smtp_port` | integer | SMTP-Port |
| `smtp_user` | string | SMTP-Benutzername |
| `smtp_password` | string (verschlüsselt) | SMTP-Passwort |
| `smtp_from` | string | Absenderadresse |
| `admin_notification_email` | string \| null | Empfänger für Admin-Benachrichtigungen |

### Spende (Donation)

| Feld | Typ | Beschreibung |
|---|---|---|
| `id` | UUID | Eindeutige ID |
| `campaign_id` | UUID | Referenz auf das Projekt |
| `amount` | integer (Cent) | Betrag in Cent |
| `currency` | string | ISO-4217-Code |
| `payment_method` | enum | `paypal` \| `wero` \| `stripe` |
| `payment_provider_ref` | string | Transaktions-ID des Zahlungsanbieters |
| `status` | enum | `pending` \| `confirmed` \| `failed` \| `refunded` |
| `message` | string \| null | Optionale öffentliche Nachricht des Spenders |
| `donor_name` | string \| null | Optionaler Anzeigename (kann „Anonym" sein) |
| `donor_email` | string \| null | E-Mail-Adresse des Spenders (für Bestätigung und Quittung) |
| `created_at` | datetime | Zeitpunkt der Spende |
| `confirmed_at` | datetime \| null | Zeitpunkt der Zahlungsbestätigung |
| `receipt_sent_at` | datetime \| null | Zeitpunkt des Quittungsversands (null = keine Quittung nötig oder noch nicht gesendet) |

---

## Funktionen

### Kampagnenverwaltung (Admin)

- Kampagne anlegen mit Titel, Beschreibung, Zielbetrag und optionalem Enddatum
- Kampagne pausieren, abschließen oder archivieren
- QR-Code zur Kampagne generieren (PNG + SVG)
- Direktlink zur Spendenpage kopieren
- Übersicht aller aktiven und archivierten Kampagnen
- Dashboard pro Kampagne: Gesamtbetrag, Anzahl Spenden, Fortschrittsbalken, Spenderliste mit Nachrichten
- Webhook-Logs zur Fehlerdiagnose einsehen
- **Buchhaltungsbereich:** Alle Spenden tabellarisch mit Betrag, Datum, Zahlungsart, Spendername, Transaktions-ID; Export als CSV
- **Spendenquittungen:** Liste aller ausgestellten Quittungen (≥ 100 €) mit Status (versendet / ausstehend); manuelle Nachsendemöglichkeit
- **Organisationsdaten:** Hinterlegung der Angaben für Spendenquittungen (siehe unten)

### Spendenflow (öffentlich)

1. Spender öffnet Link oder scannt QR-Code
2. Spendenpage zeigt: Titel, Beschreibung, Fortschrittsbalken (gesammelter Betrag / Ziel), Anzahl Spender, letzte Nachrichten
3. Spender wählt Betrag (Schnellauswahl + Freifeld, **Minimum 5,00 €**) und Zahlungsart
4. Optional: Anzeigename, E-Mail-Adresse und Nachricht eingeben
5. Weiterleitung zum Zahlungsanbieter / Zahlung im Overlay
6. Nach Bestätigung: Dankesseite mit aktuellem Fortschritt
7. **Bestätigungs-E-Mail** wird an Spender gesendet (sofern E-Mail-Adresse angegeben)
8. **Spendenquittung** wird automatisch per E-Mail versandt, wenn Betrag ≥ 100,00 €

### Fortschrittsanzeige

- Thermometer- oder Balkendiagramm (wie Wikipedia)
- Echtzeit-Update via WebSocket oder Polling (5 s Intervall als Fallback)
- Anzeige: gesammelter Betrag, Zielbetrag, Prozentzahl, Anzahl Spenden
- Optionaler Konfetti-Effekt beim Erreichen des Ziels

---

## Zahlungsanbindung

### PayPal

- Integration via PayPal Orders API v2
- Webhook: `PAYMENT.CAPTURE.COMPLETED` → Spende auf `confirmed` setzen
- Rückgabe-URL nach erfolgreicher Zahlung zur Dankesseite

### Wero (EPI/SEPA-Sofortüberweisung)

- Integration über den Wero-API-Gateway (EPI Group)
- QR-Code-basierte Zahlung oder Deep Link in die Wero-App
- Webhook zur Bestätigung eingehender Zahlungen
- Fallback: manuelle Bestätigung durch Admin, falls Webhook fehlt

### Kreditkarte (Stripe)

- Stripe Payment Intents API
- Stripe Elements oder Stripe Checkout für das Zahlungsformular
- Webhook: `payment_intent.succeeded` → Spende bestätigen
- 3-D-Secure-Support

### Allgemeine Webhook-Behandlung

- Alle Webhooks werden signaturgeprüft (HMAC / Stripe-Signatur)
- Idempotenz: doppelte `payment_provider_ref` wird ignoriert
- Fehlgeschlagene Webhooks werden bis zu 3× mit Backoff wiederholt

---

## E-Mail-Kommunikation

Alle Mails werden über den Quarkus Mailer mit Qute-Templates versandt.

### Spendenbestätigung (an Spender)

- Trigger: Zahlungsstatus wechselt auf `confirmed` und E-Mail-Adresse vorhanden
- Inhalt: Dankestext, Kampagnenname, gespendeter Betrag, Datum, Transaktionsreferenz

### Spendenquittung (an Spender)

- Trigger: Bestätigung einer Spende ≥ 100,00 € **und** E-Mail-Adresse vorhanden
- Inhalt: Formal korrekte Zuwendungsbestätigung gemäß deutschem Gemeinnützigkeitsrecht, befüllt mit den hinterlegten Organisationsdaten
- Anhang: Quittung als PDF (serverseitig generiert, z. B. via iText oder Apache PDFBox)
- `receipt_sent_at` wird nach Versand gesetzt

### Benachrichtigung (an Admin)

- Optional konfigurierbar: E-Mail bei neuer Spende und/oder beim Erreichen des Kampagnenziels

---

## Verteilung (QR-Codes & Links)

- Jede Kampagne hat eine eindeutige öffentliche URL: `/donate/{slug}`
- QR-Code wird serverseitig generiert und als PNG/SVG zum Download angeboten
- Optionale UTM-Parameter für Tracking (z. B. Flyer vs. Social Media)
- Shortlink-Unterstützung: `/d/{slug}` als Alias

---

## API-Endpunkte (REST)

### Admin (authentifiziert)

| Methode | Pfad | Beschreibung |
|---|---|---|
| `POST` | `/api/campaigns` | Kampagne anlegen |
| `GET` | `/api/campaigns` | Alle Kampagnen auflisten |
| `GET` | `/api/campaigns/{id}` | Kampagne abrufen |
| `PATCH` | `/api/campaigns/{id}` | Kampagne bearbeiten |
| `GET` | `/api/campaigns/{id}/donations` | Spenden einer Kampagne |
| `GET` | `/api/campaigns/{id}/qrcode` | QR-Code generieren |
| `GET` | `/api/campaigns/{id}/stats` | Aggregierte Statistiken |

### Öffentlich (unauthentifiziert)

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/public/campaigns/{slug}` | Kampagnendaten für die Spendenpage |
| `GET` | `/api/public/campaigns/{slug}/progress` | Aktueller Fortschritt (polling-fähig) |
| `POST` | `/api/public/campaigns/{slug}/donate` | Spende initiieren, gibt Payment-URL zurück |

### Webhooks (Zahlungsanbieter)

| Methode | Pfad | Anbieter |
|---|---|---|
| `POST` | `/webhooks/paypal` | PayPal |
| `POST` | `/webhooks/wero` | Wero |
| `POST` | `/webhooks/stripe` | Stripe |

---

## Nicht-funktionale Anforderungen

- **Sicherheit:** Webhook-Signaturprüfung, CSRF-Schutz auf Formularen, keine Speicherung vollständiger Kartendaten
- **Datenschutz:** DSGVO-konform; Spenderdaten nur mit expliziter Einwilligung öffentlich sichtbar; IP-Adressen nicht gespeichert
- **Verfügbarkeit:** Spendenpage muss auch bei Admin-Ausfall erreichbar bleiben
- **Skalierung:** Echtzeit-Updates sollen bis 500 gleichzeitige Besucher ohne Degradierung funktionieren

---

